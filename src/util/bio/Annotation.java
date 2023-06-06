package util.bio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import util.Log;
import util.Util;

public class Annotation {
	HashMap<String,ChrAnnotation> chrs;
	HashMap<String,Gene> genes;

	public Annotation(String gff) throws IOException, GFFException {
		load(gff);
	}
	
	public static void compare(String a1, String a2, String out) throws IOException, GFFException{
		Annotation an1 = new Annotation(a1);
		Annotation an2 = new Annotation(a2);
		printAnnotationComparison(an1.compare(an2),an1,an2,out);
	}
	
	public Set<String> getChrIDs(){
		return chrs.keySet();
	}
	
	public ChrAnnotation getChrAnnotation(String chr_id){
		return chrs.get(chr_id);
	}
	
	/**
	 * Finds all pairs of overlapping genes
	 * 
	 * @param a
	 * @return gene_id1|gene_id2 -> class
	 * = same junction set
	 * c subset
	 * j share some
	 * e exonic overlap
	 * i within intron
	 * a antisense within intron
	 * x antisense overlap
	 */
	public HashMap<String, String> compare(Annotation a){
		HashMap<String, String> r = new HashMap<>();
		for(Gene g1 : genes.values()){
			// look by junctions
			HashSet<Intron> ints1 = g1.getIntronHash();
			if(a.chrs.get(g1.chr_id) == null)
				continue;
			HashSet<Gene> jg = a.chrs.get(g1.chr_id).getAllGenesByIntrons(ints1);
			for(Gene g2 : jg){
				HashSet<Intron> intUnion = new HashSet<>(ints1);
				intUnion.addAll(g2.getIntronHash());
				if(intUnion.size() == g1.getIntronCount() && intUnion.size() == g2.getIntronCount())
					r.put(g1.getId()+"|"+g2.getId(), "=");
				else if(intUnion.size() == g1.getIntronCount() || intUnion.size() == g2.getIntronCount())
					r.put(g1.getId()+"|"+g2.getId(), "c");
				else
					r.put(g1.getId()+"|"+g2.getId(), "j");
			}
			// look at overlaps
			HashSet<Gene> og = a.chrs.get(g1.chr_id).getGenesByOverlap(g1.start, g1.stop, 1);
			og.addAll(a.chrs.get(g1.chr_id).getGenesByOverlap(g1.start, g1.stop, -1));
			og.addAll(a.chrs.get(g1.chr_id).getGenesByOverlap(g1.start, g1.stop, 0));
			og = Util.diff(og,jg);
			for(Gene g2 : og){
				boolean g1OverExn = hasExon(g2.getSegForRead(new int[]{g1.start,g1.stop}));
				boolean g2OverExn = hasExon(g1.getSegForRead(new int[]{g2.start,g2.stop}));
				if(g1OverExn && g2OverExn){
					if(g1.strand == g2.strand || g1.strand == 0 || g2.strand == 0)
						r.put(g1.getId()+"|"+g2.getId(), "e");
					else
						r.put(g1.getId()+"|"+g2.getId(), "x");
				}else if(g1OverExn || g2OverExn){
					if(g1.strand == g2.strand || g1.strand == 0 || g2.strand == 0)
						r.put(g1.getId()+"|"+g2.getId(), "i");
					else
						r.put(g1.getId()+"|"+g2.getId(), "a");
				}else
					throw new RuntimeException("Genes '"+g1.getId()+"' and '"+g2.getId()+"' overlap in a very unexpected way!");
			}
		}
		return r;
	}
	
	public static void printAnnotationComparison(HashMap<String, String> c, Annotation a1, Annotation a2,String outf) throws FileNotFoundException{
		PrintWriter o = new PrintWriter(outf);
		o.println("#gene_id1\tgene_id2\tclass\tintCount1,intCount2,commonIntCount");
		HashSet<String> gid1 = new HashSet<String>();
		HashSet<String> gid2 = new HashSet<String>();
		for(String gs : c.keySet()){
			String[] gids = gs.split("\\|");
			Gene g1 = a1.genes.get(gids[0]);
			Gene g2 = a2.genes.get(gids[1]);
			gid1.add(gids[0]);
			gid2.add(gids[1]);
			int commInts = Util.intersect(g1.getIntronHash(), g2.getIntronHash()).size();
			o.println(g1.getId()+"\t"+g2.getId()+"\t"+c.get(gs)+"\t"+g1.getIntronCount()+","+g2.getIntronCount()+","+commInts);
		}
		for(String g1 : Util.diff(a1.genes.keySet(),gid1))
			o.println(a1.genes.get(g1).getId()+"\t-\t-\t"+a1.genes.get(g1).getIntronCount()+",0,0");
		for(String g2 : Util.diff(a2.genes.keySet(),gid2))
			o.println("-\t"+a2.genes.get(g2).getId()+"\t-\t"+"0,"+a2.genes.get(g2).getIntronCount()+",0");
		o.close();
	}
	
	private boolean hasExon(HashSet<Seg> segs){
		for(Seg s : segs)
			if(s.segtype != Seg.segType.INT)
				return true;
		return false;
	}
	
	private void load(String in) throws IOException, GFFException{
		chrs = new HashMap<>();
		genes = new HashMap<>();
		GFFParser gff = new GFFParser(in);
		for(GFFeature f=gff.next();f != null;f=gff.next()){
			if(f.getAttr("gene_id") == null)
				Log.closeWithError("Wrong annotation file format. Features should have gene_id attribute.", null);
			ChrAnnotation chr = chrs.get(f.seqname);
			if(chr == null){
				chr = new ChrAnnotation(f.seqname);
				chrs.put(f.seqname, chr);
			}
			switch(f.feature) {
			case "gene":
				Gene g = new Gene(f.start, f.stop,f.strand, f.seqname,f.getAttr("gene_id"));
				chr.addGene(g);
				genes.put(g.getId(), g);
				break;
			case "segment":
				g = genes.get(f.getAttr("gene_id"));
				try {
					g.addSeg(new Seg(f.start, f.stop,f.strand, Seg.segType.valueOf(f.getAttr("type")), Seg.segPos.valueOf(f.getAttr("position")),f.getAttr("segment_id")));
				}catch(IllegalArgumentException e) {
					Log.closeWithError("Unknown segment type = '"+f.getAttr("type")+
							"' or position = '"+f.getAttr("position")+"'. " +
							"Type should be in: "+Util.join(Seg.segType.values(), ", ")+
							", position should be in: "+Util.join(Seg.segPos.values(), ", "), e);
				}
				break;
			case "intron":
				g = genes.get(f.getAttr("gene_id"));
				Intron i = new Intron(f.start, f.stop,f.strand);
				g.addIntron(i);
				break;
			default:
				Log.closeWithError("Annotation contains unknown feature: '"+f.feature+"'. Only gene,segment and intron are allowed.", new RuntimeException());
			}
			
		}
		for(ChrAnnotation chr : chrs.values())
			chr.loaded();
		gff.close();
	}

}
