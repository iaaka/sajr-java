package ann;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import run.Run;

import util.Settings;
import util.bio.Annotation;
import util.bio.ChrAnnotation;
import util.bio.GFFException;
import util.bio.Gene;


public class TranscExtractor {
	public static void extractTranscripts(String gff_in,String gff_out,String seg_counts_path,double irPSI, double asPSI) throws IOException, GFFException{
		Annotation a = new Annotation(gff_in);
		PrintStream out = new PrintStream(gff_out);
		
		out.println("##"+new Date()+" - "+Settings.VERSION+" - "+Run.ANNOTATE);
		HashSet<String> gr = new HashSet<String>();
		gr.add("sajr2transc");
		out.println("##Settings:");
		Settings.S().printSettings("##",gr, out);

		BufferedReader cnts = new BufferedReader(new FileReader(seg_counts_path));
		HashMap<String,Double> seg2psi = new HashMap<>();
		for(String l=cnts.readLine();l!=null;l=cnts.readLine()){
			if(l.charAt(0)=='#' || l.startsWith("segment_id"))
				continue;
			String t[] = l.split("\t");
			seg2psi.put(t[0],Double.parseDouble(t[3]));
		}
		cnts.close();
		ArrayList<String> chrs = new ArrayList<>(a.getChrIDs());
		Collections.sort(chrs);
		for(String chr : chrs){
			ChrAnnotation ca = a.getChrAnnotation(chr);
			for(Gene g : ca.getGenes()){
				g.printTranscripts(out, seg2psi, irPSI, asPSI);
			}
		}
		
		out.close();
	}
	
	public static void main(String[] args) {
		System.out.println(Double.parseDouble("NaN"));
	}

}
