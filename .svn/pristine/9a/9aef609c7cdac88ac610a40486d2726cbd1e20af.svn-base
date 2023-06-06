package ann;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import run.Run;

import util.Interval;
import util.Settings;
import util.bio.GFFeature;
import util.bio.Gene;
import util.bio.Intron;
import util.bio.Seg;

public class GFFPrinter {
	private PrintStream out;
	private int gene_id = 1,seg_id,int_id;
	private String zeros = "000000";

	public GFFPrinter(PrintStream out) {
		this.out=out;
	}
	
	public void printAnnotateHeader() {
		out.println("##"+new Date()+" - "+Settings.VERSION+" - "+Run.ANNOTATE);
		HashSet<String> gr = new HashSet<String>();
		gr.add("common");
		gr.add("annotate");
		out.println("##Settings:");
		Settings.S().printSettings("##",gr, out);
	}
	
	public void printCuff2SAJRHeader() {
		out.println("##"+new Date()+" - "+Settings.VERSION+" - "+Run.GFF2SAJR);
		out.println("##Settings:");
		out.println("##Gene black list is: "+Settings.S().getString(Settings.GENE_BLACK_LIST));
		out.println("##Derived from: "+Settings.S().getString(Settings.ANN_FOREIGN));
	}
	
	public void close(){
		out.close();
	}
	
	private String addZeros(int i){
		String ii = ""+i;
		if(ii.length()>=zeros.length())
			return ii;
		return zeros.substring(0,zeros.length()-ii.length())+ii;
	}
	
	public void printGene(Gene g){
		seg_id = 1;
		int_id = 1;
		String  gid;
		if(g.getId() != null)
			gid = g.getId();
		else
			gid = Settings.S().getString(Settings.ID_PREFIX)+"G"+addZeros(gene_id);
		printInterval(g,g.chr_id,gid);
		ArrayList<Interval> f = new ArrayList<>();
		for(int i=0;i<g.getSegCount();i++) 
			f.add(g.getSeg(i));
		for(int i=0;i<g.getIntronCount();i++)
			f.add(g.getIntron(i));
		Collections.sort(f);
		for(Interval i : f)
			printInterval(i,g.chr_id,gid);
		gene_id++;
	}
	
	private void printInterval(Interval i,String chr_id,String gene_id){
		String type = null;
		String attr = "gene_id="+gene_id;
		if(i instanceof Intron){
			String intID = i.getId();
			if(intID == null){
				intID = gene_id+".i"+int_id;
				int_id++;
			}	
			type = "intron";
			attr += "; intron_id="+intID;// +"; overhang="+((Intron)i).getMaxOverhang()+"; indep_pos="+((Intron)i).getPosNo()+"; overhangUniq="+((Intron)i).overhangStat2String(true)+"; overhangMult="+((Intron)i).overhangStat2String(false);

		}else if(i instanceof Seg){
			String segID = i.getId();
			if(segID == null){
				segID = gene_id+".s"+seg_id;
				seg_id++;
			}	
			type = "segment";
			attr += "; segment_id="+segID+"; type="+((Seg)i).segtype+"; position="+((Seg)i).segpos;
		}else if(i instanceof Gene)
			type = "gene";
		out.println(chr_id+"\t"+Settings.SHORT_VERSION+"\t"+type+"\t"+i.start+"\t"+i.stop+"\t0\t"+(i.strand==0?".":(i.strand==1?"+":"-"))+"\t.\t"+attr);
	}
}
