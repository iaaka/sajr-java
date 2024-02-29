package ann;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import util.Log;
import util.Settings;
import util.bio.GFFException;
import util.bio.GFFeature;
import util.bio.GFFParser;
import util.bio.Gene;
import util.bio.Intron;
import util.bio.Seg;

public class Gff2SAJR {

	public  static void gff2sajr() throws IOException,GFFException {
		Gff2SAJR t = new Gff2SAJR();
		t.gff2sajr(Settings.S().getString(Settings.ANN_FOREIGN));
	}
	
	public  static void gff32sajr() throws IOException,GFFException {
		Gff2SAJR t = new Gff2SAJR();
		t.gff32sajr(Settings.S().getString(Settings.ANN_FOREIGN));
	}
	
	private void gff2sajr(String in) throws IOException, GFFException {
		GFFPrinter out = new GFFPrinter(new PrintStream(Settings.S().getString(Settings.ANN_OUT)));
		out.printCuff2SAJRHeader();
		String[] black_list = Settings.S().getString(Settings.GENE_BLACK_LIST).split("@");
		Arrays.sort(black_list);
		
		// load whole gtf
		GFFParser gffp = new GFFParser(in);
		ArrayList<GFFeature> gffa = new ArrayList<GFFeature>();
		for(GFFeature f=gffp.next();f != null;f=gffp.next())
			gffa.add(f);
		gffp.close();
		//sort
		Collections.sort(gffa,new Comparator<GFFeature>() {
			public int compare(GFFeature o1, GFFeature o2) {
					return o1.seqname.compareTo(o2.seqname);
			}
		});
		
		// convert
		ArrayList<GFFeature> gff = new ArrayList<>();
		boolean has_transc_feature = true;
		boolean has_exon_no_feature = true;
		int i=1;
		String chr = null;
		HashSet<String> chr_ids = new HashSet<>();
		
		for(GFFeature f : gffa){
			if(chr == null) {
				chr = f.seqname;
				chr_ids.add(chr);
			}
			
			if(f == null || !chr.equals(f.seqname)){
				gff2sajr_chr(gff,has_transc_feature,has_exon_no_feature,black_list,out);
				gff.clear();
				System.gc();
				if(f == null)
					break;
				has_transc_feature = true;
				has_exon_no_feature = true;
				chr = f.seqname;
				if(!chr_ids.add(chr))
					Log.closeWithError("GTF file have to be sorted by seqname! It isn't true for line " + i, new RuntimeException());
			}

			if(f.feature.equals("exon") || f.feature.equals("transcript"))
			gff.add(f);
			if(f.feature.equals("exon")){
				has_transc_feature = has_transc_feature && f.getAttr("transcript_id") != null;
				has_exon_no_feature = has_transc_feature && f.getAttr("exon_number") != null;
			}
			i++;
		}
		out.close();
	}
	
	/**
	 * converts gff (gtf, cufflincs output for example) into sajr format for single chr
	 * @param gff
	 * @throws FileNotFoundException 
	 */
	private void gff2sajr_chr(ArrayList<GFFeature> gff,boolean has_transc_feature,final boolean has_exon_no_feature,String[] black_list,GFFPrinter out) throws GFFException, FileNotFoundException{
		HashMap<String,Gene> genes = new HashMap<>();
		// sort it if possible
		if(has_transc_feature){
			Collections.sort(gff,new Comparator<GFFeature>() {
				public int compare(GFFeature o1, GFFeature o2) {
					int transc_comp = o1.getAttr("transcript_id").compareTo(o2.getAttr("transcript_id"));
					if(transc_comp != 0) 
						return transc_comp;
					if(o1.feature.equals("transcript") && o2.feature.equals("transcript"))
						Log.closeWithError("There are two transcripts with the same id: '"+o1.getAttr("transcript_id")+"'",new RuntimeException());
					else if(o1.feature.equals("transcript") && o2.feature.equals("exon"))
						return -1;
					else if(o1.feature.equals("exon") && o2.feature.equals("transcript"))
						return 1;
					else if(has_exon_no_feature)
						return o1.getAttrInt("exon_number") - o2.getAttrInt("exon_number");
					else
						return o1.start - o2.start;
					throw new RuntimeException("something went crazy in gff sorting!"); 
				}
			});
		}
		// convert
		int line = 1;
		GFFeature prev = null;
		for(int j=0;j<gff.size();j++) {
			GFFeature curr = gff.set(j, null);
			line++;
			if(curr.feature.equals("transcript"))
				prev = null;
			if(!curr.feature.equals("exon")) 
				continue;
			if(curr.getAttr("exon_number") != null && curr.getAttrInt("exon_number") == 1)
				prev = null;
			if(curr.getAttr("transcript_id") != null && prev !=null && !curr.getAttr("transcript_id").equals(prev.getAttr("transcript_id")))
				prev = null;
			String gid = curr.getAttr("gene_id");
			Gene g = genes.get(gid);
			if(g == null) {
				if(Arrays.binarySearch(black_list,gid)>=0)
					continue;
				g = new Gene(0, Integer.MAX_VALUE, curr.strand, curr.seqname,gid);
				genes.put(gid, g);
			}
			if(!g.chr_id.equals(curr.seqname))
				Log.closeWithError("Gene "+g.getId()+" has exons on different chromosomes. Please check whether gene_id is unique gene identifier.",  new RuntimeException());
			g.addSeg(new Seg(curr.start, curr.stop, curr.strand));
			if(prev != null) {
				if(!curr.getAttr("gene_id").equals(prev.getAttr("gene_id")))
					Log.closeWithError("Sequential exons are from different genes at line "+line, new RuntimeException());
				if(curr.start < prev.stop)
					if(curr.strand == 1)
						throw new GFFException("Sequential exons do not follow each other in genomic сoordinates at line "+line, curr.toString());
					else
						g.addIntron(new Intron(curr.stop+1, prev.start-1, curr.strand));
				else
					g.addIntron(new Intron(prev.stop+1, curr.start-1, curr.strand));
			}
			prev=curr;
		}
		save(out,genes);
	}
	
	private void gff32sajr(String in) throws IOException, GFFException {
		String[] black_list = Settings.S().getString(Settings.GENE_BLACK_LIST).split("@");
		Arrays.sort(black_list);
		HashMap<String,Gene> genes = new HashMap<>();
		GFFParser p = new GFFParser(in);
		int line = 1;
		GFFeature prev = null;
		HashMap<String,Gene> mrna2gene = new HashMap<>();
		for(GFFeature curr = p.next();curr != null;curr = p.next()) {
			line++;
			switch(curr.feature) {
			case "transcript":
			case "mRNA":
				Gene g = genes.get(curr.getAttr("Parent"));
				if(g == null) {
					g = new Gene(0, Integer.MAX_VALUE, curr.strand, curr.seqname,curr.getAttr("Parent"));
					genes.put(g.getId(), g);
				}
				prev = null;
				mrna2gene.put(curr.getAttr("ID"), g);
				break;
			case "exon":
				g =  mrna2gene.get(curr.getAttr("Parent"));
				if(g == null)
					continue;
				g.addSeg(new Seg(curr.start, curr.stop, curr.strand));
				if(prev != null) {
					if(!curr.getAttr("Parent").equals(prev.getAttr("Parent")))
						throw new GFFException("Sequential exons are from different mRNAa at line "+line, curr.toString());
					if(curr.start <= prev.stop)
						if(curr.strand == 1)
							throw new GFFException("Sequential exons do not follow each other in genomic coordinates at line "+line, curr.toString());
						else
							g.addIntron(new Intron(curr.stop+1, prev.start-1, curr.strand));
					else
						g.addIntron(new Intron(prev.stop+1, curr.start-1, curr.strand));
				}
				prev=curr;
				break;
			default:
				break;
			}
		}
		p.close();
		save(genes);
	}
	
	private void save(HashMap<String,Gene> genes) throws FileNotFoundException {
		GFFPrinter p = new GFFPrinter(new PrintStream(Settings.S().getString(Settings.ANN_OUT)));
		p.printCuff2SAJRHeader();
		save(p,genes);
		p.close();
	}
	
	private void save(GFFPrinter p,HashMap<String,Gene> genes) throws FileNotFoundException {
		ArrayList<Gene> gs = new ArrayList<>(genes.values());
		for(int i=0;i<gs.size();i++) {
			gs.set(i, gs.get(i).exon2seg());
		}
		Collections.sort(gs,new Comparator<Gene>() {

			public int compare(Gene o1, Gene o2) {
				if(o1.chr_id.equals(o2.chr_id))
					return o1.compareTo(o2);
				return o1.chr_id.compareTo(o2.chr_id);
			}
		});
		for(Gene g : gs) {
			p.printGene(g);
		}
	}
}
