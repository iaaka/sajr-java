package ann;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import rc.ReadCounter;

import htsjdk.samtools.SAMRecord;

import util.*;
import util.bio.ChrAnnotation;
import util.bio.Gene;
import util.bio.Intron;
import util.bio.Seg;


public class ChrCoverage {
	final int[] peCov; //expoic coverage on plus strand
	final int[] meCov; //expoic coverage on minus strand
	final int[] piCov; //intronic coverage on plus strand
	final int[] miCov; //intronic coverage on minus strand
	final int[] ueCov; //unstranded exonic coverage
	final String chr_id;
	HashMap<Intron,Intron> introns = new HashMap<>();
	String seq;
	String name;
	HashMap<String,Integer> site2strand;
	long readLen=0;
	long readCnt=0;
	int minOverhang;
	

	public ChrCoverage(String chr_id,String seq) throws IOException{
		this.seq = seq.toUpperCase();
		peCov = new int[seq.length()+1];
		piCov = new int[seq.length()+1];
		meCov = new int[seq.length()+1];
		miCov = new int[seq.length()+1];
		ueCov = new int[seq.length()+1];
		this.chr_id = chr_id;
		site2strand = new HashMap<>();
		site2strand.put("GTAG", 1);
		site2strand.put("GCAG", 1);
		site2strand.put("ATAC", 1);
		site2strand.put("CTAC", -1);
		site2strand.put("CTGC", -1);
		site2strand.put("GTAT", -1);
		minOverhang = Settings.S().getInt(Settings.JUNC_OVERHANG);
	}
	
	private int getTotalCov(int[] c,int f,int t){
		int r = 0;
		int from = Math.max(0,Math.min(f, t));
		int to = Math.min(c.length-1,Math.max(f, t));
		for(int i=from;i<=to;i++)
			r += c[i];
		return r;
	}

	private Integer getStrandByIntronSites(String s){
		Integer r = site2strand.get(s);
		if(r==null){
			for(String ss : site2strand.keySet()){
				if(getNotNDist(s,ss)==0){
					Integer strand = site2strand.get(ss);
					if(r == null)
						r = strand;
					else if(!r.equals(strand))
						return null;
				}
			}
		}
		return r;
	}
	
	private int getNotNDist(String a,String b){
		int r = 0;
		for(int i =0;i<a.length();i++){
			if(a.charAt(i) != 'N' && b.charAt(i) != 'N' && a.charAt(i) != b.charAt(i))
				r++;
		}
		return r;
	}
	
	public Set<Intron> getIntrons(){
		return introns.keySet();
	}
	
	private void addCov(int[] ecov,int[] icov,int[] inters,Intron[] addedIntrons){
		for(int i=0;i<inters.length;i+=2){
			for(int j=inters[i];j<=inters[i+1];j++)
				ecov[j]++;
			if(i != 0 && addedIntrons[i/2-1] != null){//do not add intronic coverage if read was not used for for junctions due to low overhang
				for(int j=inters[i-1]+1;j<=inters[i]-1;j++)
					icov[j]++;
			}
		}
	}
		
	
	private String getSeq(int start,int stop){
		return seq.substring(start-1,stop);
	}
	
	
	public void fillCovInNs(){
		//load all sites
		ArrayList<Integer> sites = new ArrayList<>();
		for(Intron i : introns.keySet()){
			sites.add(i.start-1);
			sites.add(i.stop);
		}
		Collections.sort(sites);
		int rl =(int) (readLen/Math.max(readCnt,1));
		int start = -1;
		int gap = 0;
		for(int i =0;i<seq.length();i++){
			if(seq.charAt(i)=='N'){
				gap = 0;
				if(start == -1)
					start = i;
			}else if(start != -1){
				gap++;
				if(gap>= rl || i == seq.length()-1){
					//fill coverage
					int stop = i - gap;
					fillNs(peCov,start,stop,rl,sites);
					fillNs(meCov,start,stop,rl,sites);
					fillNs(ueCov,start,stop,rl,sites);
					start = -1;
					gap=0;
				}
			}
		}
	}
	
	private void fillNs(int[] cov,int from,int to,int mar,ArrayList<Integer> sites){
		int leftCov=0,rightCov = 0;
		//look for closest splice site
		//for start
		int finx = Collections.binarySearch(sites,from);
		if(finx<0) finx = -(finx+1);
		if(finx != 0) finx=sites.get(finx-1); 
		//for end
		int tinx = Collections.binarySearch(sites,to);
		if(tinx<0) tinx = -(tinx+1);
		if(tinx == sites.size()) 
			tinx = cov.length-1;
		else
			tinx=sites.get(tinx);
		
		int b = Math.max(finx,from-mar);
		int e = Math.min(tinx,to+mar);
				
		for(int j=b;j<to;j++) leftCov = Math.max(leftCov,cov[j]);
		for(int j=to+1;j<=e;j++) rightCov = Math.max(rightCov,cov[j]);
		double d = ((double)(rightCov-leftCov))/(e-b+1);
		Log.println("fill Ns: "+chr_id+":"+e+" ("+from+")"+"-"+b+" ("+to+")"+"\tcov="+leftCov+"-"+rightCov);
		for(int j=b;j<=e;j++)
			cov[j] = Math.max(cov[j],(int)(rightCov + d*(j-from)));
	}

	public void read(SAMRecord r) {
		if(!ReadCounter.accept(r))
			return;
		readLen += r.getReadLength();
		readCnt++;
		Log.addStat(Log.USED_READS, 1);
		int[] inters = Util.getMapIntervals(r);
		int strand = Settings.S().getInt(Settings.STRANDED)*(r.getReadNegativeStrandFlag()?-1:1);
		if(Settings.S().getInt(Settings.PAIRED) == 1 && r.getReadPairedFlag() && !r.getFirstOfPairFlag())
			strand = -strand;
		//parse junctions, if exist
		Intron[] cintrons = new Intron[inters.length/2-1];
		if(inters.length>2){
			Log.addStat(Log.JUNCTIONS_CNT, 1);
			int len = 0;
			for(int i =1;i<inters.length-1;i+=2){
				String ss = getSeq(inters[i]+1,inters[i]+2)+getSeq(inters[i+1]-2,inters[i+1]-1);
				Integer cstrand = getStrandByIntronSites(ss);
				if(cstrand == null || (strand != 0 && strand != cstrand)){
					Log.addStat(Log.BAD_JUNCTIONS_CNT, 1);
					Log.throwUncrucialExc("Read "+r.getReadName()+" has wrong splice sites.\n"+
							"Strand="+strand+". Sites (intron="+(inters[i]+1)+"-"+(inters[i+1]-1)+")="+ss);
					return;
				}
				if(strand == 0)
					strand = cstrand;
				len += inters[i] - inters[i-1] +1;
				int oh = Math.min(len, r.getReadLength()-len);
				if(oh >= minOverhang){//reads with low overhang are simply not used
					cintrons[(i-1)/2] = new Intron(inters[i]+1, inters[i+1]-1, strand);
					cintrons[(i-1)/2].addRead(r.getReadLength(),len, inters[0],r.getAttribute("NH") == null || r.getIntegerAttribute("NH") == 1);
				}
			}
			for(Intron i : cintrons){
				if(i == null)
					continue;
				Intron j = introns.get(i);
				if(j == null)
					introns.put(i,i);
				else{
					j.addReads(i);
				}
				
			}
		}
		int rstrand = Settings.S().getInt(Settings.STRANDED)*(r.getReadNegativeStrandFlag()?-1:1);
		//add coverage
		switch(strand){
		case 1:
			addCov(rstrand==0?ueCov:peCov,piCov,inters,cintrons);
			break;
		case -1:
			addCov(rstrand==0?ueCov:meCov,miCov,inters,cintrons);
			break;
		case 0:
			addCov(ueCov,null,inters,cintrons);
		}
	}
	
	public ArrayList<Gene> findGenes(){
		ArrayList<Gene> r = findGenes(1);
		r.addAll(findGenes(-1));
		
		//stranded single exon genes shouldn't overlap multiexon genes, but could overlap each other on opposite strands
		ArrayList<Gene> sg = findSingleExonGenes(peCov,ueCov,piCov,r, 1);
		sg.addAll(findSingleExonGenes(meCov,ueCov,miCov,r,-1));
		
		r.addAll(sg);
		r.addAll(findSingleExonGenes(ueCov,null,util.Util.sum(piCov,miCov),r,0));
		return r;
	}
	
	private void set2zero(int[] c,int f,int t){
		for(int i=f;i<=t;i++) c[i]=0;
	}
	
	/**
	 * Predicts single exon genes as continuous coverage regions.
	 * Genes are defined as covered region with gaps in coverage not longer than max_cov_gap,
	 * length >= min_single_exon_gene_length. If found gene has average coverage lower than min_cov it is rejected.
	 * @param cov
	 * @param genes used to remove known exons
	 * @param strand for strand specific data, if data isn't strand specific 0 should be used.
	 * @return
	 */
	public ArrayList<Gene> findSingleExonGenes(int[] ecov,int[] uecov,int[] icov, ArrayList<Gene> genes, int strand){
		ecov = ecov.clone();
		if(uecov != null)
			uecov = uecov.clone();
		// removes coverage in known exons
		for(Gene g : genes){
			for(int i=0;i<g.getSegCount();i++){
				Seg s = g.getSeg(i);
				if(uecov != null)
					set2zero(uecov,s.start,s.stop);
				if(strand == 0 || strand == g.strand)
					set2zero(ecov,s.start,s.stop);
			}
		}
		
		ArrayList<Gene> r = new ArrayList<>();
		int start=-1;
		int gap = 0;
		for(int i =1;i<ecov.length;i++){
			if(ecov[i] > 0 && i != ecov.length-1){
				gap = 0;
				if(start == -1)
					start = i;
			}else{
				if(start != -1){
					gap++;
					if(gap > Settings.S().getInt(Settings.MAX_COV_GAP) || i == ecov.length-1){
						int stop = i - gap;
						// extend gene using unstranded coverage (if current coverage is stranded)
						if(strand != 0 && stop-start+1 >= Settings.S().getInt(Settings.MIN_SINGLE_EXON_GENE_LENGTH)){
							//look backward
							gap = 0;
							for(;start>0 && gap <= Settings.S().getInt(Settings.MAX_COV_GAP) ;start--)
								if(uecov[start] + ecov[start]==0)
									gap++;
								else
									gap = 0;
							start += gap;
							start = Math.max(1, start);
							//look forward
							gap = 0;
							for(;stop<ecov.length && gap <= Settings.S().getInt(Settings.MAX_COV_GAP) ;stop++)
								if(uecov[stop] + ecov[stop]==0)
									gap++;
								else
									gap = 0;
							stop -= gap +1;
							stop = Math.min(stop, ecov.length-1);
						}
						// count coverage
						double egcov = 0,gcov=0;
						for(int j=start;j<=stop;j++){
							egcov += icov[j];
							if(strand != 0)
								gcov += uecov[j] + ecov[j];
							else
								gcov += ecov[j];
						}
						// add gene if it is ok
						if(stop-start+1 >= Settings.S().getInt(Settings.MIN_SINGLE_EXON_GENE_LENGTH) &&
						   gcov/(stop-start+1) >=  Settings.S().getDouble(Settings.MIN_SINGLE_EXON_GENE_COV) &&
						   gcov/(gcov+egcov) >= Settings.S().getDouble(Settings.SITE_USAGE_FREQ) &&
						   //probably 0.5 should be a parameter...
						   getNproportion(start,stop) < 0.5){
							Gene g = new Gene(start, stop, strand, chr_id);
							g.addSeg(new Seg(start,stop,strand,Seg.segType.EXN,Seg.segPos.ONLY));
							r.add(g);
						}	
						gap = 0;
						gcov = 0;
						start = -1;	
						i = Math.max(i,stop);
					}
				}
			}
		}
		return r;
	}
	
	private double getNproportion(int f,int t){
		double r = 0;
		for(int i=f-1;i<t;i++)
			if(seq.charAt(i)=='N')
				r++;
		return r/(t-f+1);
	}
	
	public ArrayList<Gene> findGenes(final int strand){
		int[] cov =  util.Util.sum(peCov,ueCov);
		if(strand == -1)
			cov = util.Util.sum(meCov,ueCov);
		//coordinates of nt before splice site
		Graph<Integer,Interval> splgraph = new Graph<>();
		//1 - left, 2-right, 3 - both
		HashMap<Integer,Integer> site2type = new HashMap<>();
		//add nodes (splice sites) and intron edges
		for(Intron i : introns.keySet()){
			if(i.strand != strand)
				continue;
			splgraph.add(i.start-1);
			splgraph.add(i.stop);
			splgraph.addEdge(i.start-1, i.stop,i);
			Integer v = site2type.get(i.start-1);
			site2type.put(i.start-1,v==null?1:(v==1?1:3));
			v = site2type.get(i.stop);
			site2type.put(i.stop,v==null?2:(v==2?2:3));
		}
		ArrayList<Integer> sites = new ArrayList<>(splgraph.getAllNodes());
		Collections.sort(sites);	
		int cutPos = -1;
		//create segments, add nodes TSS and polyA sites and segment edges
		for(int i=0;i<sites.size();i++){
			//make for left first exon
			if(i == 0 || (cutPos != -1 && site2type.get(sites.get(i)) != 2)){
				int p = (i == 0)?checkSegEnd(cov,sites.get(i),1):cutPos;
				if(p > sites.get(i)) // to evade segments with zero length
					p=sites.get(i);
				splgraph.add(p);
				splgraph.addEdge(sites.get(i),p,new Seg(p,sites.get(i),strand,Seg.segType.NA,strand==1?Seg.segPos.FIRST:Seg.segPos.LAST));
			}
			//look for last[est]
			if(i == sites.size() -1){
				int seg_end = checkSegEnd(cov,sites.get(i)+1,cov.length-1);
				if(seg_end < sites.get(i)+1) // to evade segments with zero length
					seg_end = sites.get(i)+1;
				splgraph.add(seg_end);
				splgraph.addEdge(sites.get(i),seg_end,new Seg(sites.get(i)+1,seg_end,strand,Seg.segType.NA,strand==-1?Seg.segPos.FIRST:Seg.segPos.LAST));
			//look for internal (and last)
			}else{
				int seg_end = checkSegEnd(cov,sites.get(i)+1,sites.get(i+1));
				if(seg_end == sites.get(i+1)){
					cutPos = -1;
					splgraph.addEdge(sites.get(i),sites.get(i+1),new Seg(sites.get(i)+1,sites.get(i+1),strand,Seg.segType.NA,Seg.segPos.INTERNAL));
				}else{
					cutPos = checkSegEnd(cov,sites.get(i+1),sites.get(i)+1);
					if(cutPos <= seg_end){
						cutPos = (cutPos+seg_end)/2+1;
						seg_end = cutPos - 1;
					}
					if(site2type.get(sites.get(i)) != 1){
						if(seg_end < sites.get(i)+1) // to evade segments with zero length
							seg_end = sites.get(i)+1;
						splgraph.add(seg_end);
						splgraph.addEdge(sites.get(i),seg_end,new Seg(sites.get(i)+1,seg_end,strand,Seg.segType.NA,strand==-1?Seg.segPos.FIRST:Seg.segPos.LAST));
					}
				}
			}
		}
		//make genes
		ArrayList<Gene> res = makeGenes(splgraph, strand, chr_id);
		return res;
	}
	
	public static ArrayList<Gene> makeGenes(Graph<Integer, Interval> splgraph,int strand,String chr_id){
		HashMap<Integer, Integer> genes = splgraph.connectedComponents();
		HashMap<Integer, int[]> geneCoors = new HashMap<>();
		HashMap<Integer, HashSet<Interval>> geneEdges = new HashMap<>();
		for(int i : genes.keySet()){
			int gid = genes.get(i);
			int[] coors = geneCoors.get(gid);
			if(coors == null){
				coors = new int[]{i,i};
				geneCoors.put(gid, coors);
			}
			coors[0] = Math.min(coors[0], i);
			coors[1] = Math.max(coors[1], i);
			//extract segs and ints
			HashSet<Interval> edges = geneEdges.get(gid);
			if(edges == null) {
				edges = new HashSet<>();
				geneEdges.put(gid, edges);
			}
			edges.addAll(splgraph.getEdges(i));
		}
		ArrayList<Gene> res = new ArrayList<>(geneCoors.size());
		for(int gid : geneCoors.keySet()) {
			int[] c = geneCoors.get(gid);
			Gene g = new Gene(c[0], c[1], strand, chr_id);
			res.add(g);
			HashSet<Interval> edges = geneEdges.get(gid);
			for(Interval i : edges) {
				if(i instanceof Seg) 
					g.addSeg((Seg)i);
				else 
					g.addIntron((Intron)i);
			}
		}
		Collections.sort(res);
		for(Gene gn : res)
			gn.setSegTypes();
		return res;
	}
		
	/**
	 * tests whether segment exists (according to settings):
	 * max_cov_gap and min_cov
	 * @param cov
	 * @param from if from > to than it means that we should go back (i/e/ we are looking for first exon)
	 * @param to included
	 * @return end of segment, if it coincides with to, then the segment exists
	 */
	private int checkSegEnd(int[] cov, final int from, final int to){
		double min_cov = 0;
		double sum = 0;
		int gap = 0;
		int win = Settings.S().getInt(Settings.COV_WIN_LEN);
		int dir = from<=to?1:-1;
		//curr,next
		Integer avg_cov_end = null;
		double[] winCov = null;
		
		double max_win_cov=0, min_win_cov=0;
		for(int i=from;;i+=dir){
			sum+=cov[i];
			//find first position where average coverage become to low (if total average cov is low, this position will be returned)
			if(avg_cov_end == null && sum/(dir*(i-from)+1) < Settings.S().getDouble(Settings.MIN_COV))
				avg_cov_end = i;
			if(cov[i] <= min_cov)
				gap++;
			else
				gap = 0;
			//cut by gap
			if(gap>Settings.S().getInt(Settings.MAX_COV_GAP))
				return i-dir*gap;
			
			//set smoothed coverage
			if(dir*(i-from)+1 >= win){
				if(dir*(i-from)+1 == win){
					winCov = new double[dir*(to-from)+2-win];
					winCov[0] = getTotalCov(cov, from, from+dir*(win-1)); 
					max_win_cov = min_win_cov = winCov[0];
				}else{
					double v = winCov[dir*(i-from)+1-win] = winCov[dir*(i-from)-win] +cov[i] - cov[i-dir*win];
					max_win_cov = Math.max(max_win_cov, v);
					if(min_win_cov > v){
						min_win_cov = v;
					}
					if(max_win_cov/min_win_cov > Settings.S().getDouble(Settings.MAX_COV_STEP))
						return i - dir*(win/2+1);
				}
			}
			if(i == to) break; //because i don't want to check dir..
		}
		if(sum/(dir*(to-from)+1) >= Settings.S().getDouble(Settings.MIN_COV))
			return to;
		return avg_cov_end;
	}
	
	/**
	 * replace previous intron set with new one. do not filter introns after that.
	 * @param ints
	 */
	public void setIntrons(ArrayList<Intron> ints){
		introns = new HashMap<>(ints==null?0:ints.size());
		if(ints != null)
			for(Intron i : ints)
				introns.put(i, i);
	}
	
	/**
	 * adds all junctions and exons (as continuous coverage >= min_single_exon_gene_cov)
	 * @param a
	 */
	protected void addForeighAnnotation(ChrAnnotation a){
		if(a == null)
			return;
		for(Intron i : a.getIntrons()){
			Intron tmp = introns.get(i);
			if(tmp == null)
				tmp = i;
			
			tmp.setMaxOverhang(Settings.S().getInt(Settings.JUNC_OVERHANG) );
			tmp.setPosNo(Settings.S().getInt(Settings.INDEP_POS));
			tmp.setCov(tmp.getCov()+Settings.S().getDouble(Settings.FOREIGN_JUNC_COV));
			introns.put(tmp, tmp);
		}
		
		double siteFreq = Settings.S().getDouble(Settings.SITE_USAGE_FREQ); //I do not want to skip exons from external annotation due it low coverage 
		siteFreq /= (1-siteFreq);
		int minCov = (int) Settings.S().getDouble(Settings.MIN_SINGLE_EXON_GENE_COV) + 1;
		for(Gene g : a.getGenes()){
			for(int i =0;i<g.getSegCount();i++){
				Seg s = g.getSeg(i);
				if(s.getId() == null)
					continue;
				for(int j=s.start;j<=s.stop;j++){
					switch(g.strand){
					case -1:
						meCov[j] += (int)(miCov[j]*siteFreq+1)+minCov;
						break;
					case 0:
						ueCov[j] += (int)((miCov[j]+piCov[j])*siteFreq+1)+minCov;
						break;
					case 1:
						peCov[j] += (int)(piCov[j]*siteFreq+1)+minCov;
						break;
					}
				}
			}
		}
	}
	
	/**removes introns according to settings:
	 * overhang, indep_pos, site_skip_freq
	 */
	public void filterIntrons(){
		HashSet<Intron> tmp = new HashSet<>(introns.keySet());
		for(Intron i : tmp){
			if(i.getMaxOverhang() < Settings.S().getInt(Settings.JUNC_OVERHANG) || //since now i do not use reads with low overhang, it could be removed. not it is here just for hystorical reasons
					i.getPosNo() < Settings.S().getInt(Settings.INDEP_POS)){
				introns.remove(i);
				continue;
			}
			
			double f1 = i.getCov()/((i.strand==1?peCov:meCov)[i.start]+(i.strand==1?piCov:miCov)[i.start] + ueCov[i.start]);
			double f2 = i.getCov()/((i.strand==1?peCov:meCov)[i.stop ]+(i.strand==1?piCov:miCov)[i.stop ] + ueCov[i.stop ]);
			System.out.println("filter inton ("+i+"): "+i.getCov()+", "+f1 +" ("+(i.strand==1?peCov:meCov)[i.start]+", "+(i.strand==1?piCov:miCov)[i.start] + ", "+ ueCov[i.start]+")"+
						", "+f2 +" ("+(i.strand==1?peCov:meCov)[i.stop ]+", "+(i.strand==1?piCov:miCov)[i.stop ]+", " + ueCov[i.stop ]+")");
			if(f1< Settings.S().getDouble(Settings.SITE_USAGE_FREQ) || f2 < Settings.S().getDouble(Settings.SITE_USAGE_FREQ) )
				introns.remove(i);
		}
	}
}
