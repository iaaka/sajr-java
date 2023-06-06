package run;

import java.io.IOException;

import rc.ReadCounter;

import util.Log;
import util.Settings;
import util.bio.Annotation;
import util.bio.GFFException;

import ann.Annotator;
import ann.Gff2SAJR;
import ann.TranscExtractor;

public class Run {
	public static final String ANNOTATE = "annotate";
	public static final String COUNT_READS = "count_reads";
	public static final String GFF2SAJR = "gff2sajr";
	public static final String GFF32SAJR = "gff32sajr";
	public static final String SAJRCOMP = "sajrcomp";
	public static final String SAJR2TRANSC = "sajr2transc";
	
	public static void main(String[] args)  {
		try {
			run(args);
		} catch (Exception e) {
			Log.closeWithError("Unrecognized error: "+e.getMessage(), e);
		}
	}
	
	private static void run(String[] args) throws IOException, ClassNotFoundException, GFFException {
		if(!readParams(args)) {
			printHelp();
			return;
		}
		switch(args[0]) {
		case ANNOTATE:
			new Annotator().annotate();
			break;
		case COUNT_READS:
			String batch_in = Settings.S().getString(Settings.BATCH_IN);
			if(batch_in.equals("null"))
				ReadCounter.countAndPrint();
			else {
				String[] in = batch_in.split(",");
				String[] out = Settings.S().getString(Settings.BATCH_OUT).split(",");
				if(in.length != out.length) {
					Log.closeWithError("Number of elements in batch_in isn't equal to number of elements in batch_out", null);
					return;
				}
				for(int i=0;i<in.length;i++) {
					 Settings.S().set(Settings.IN, in[i]);
					 Settings.S().set(Settings.OUT_BASE, out[i]);
					 ReadCounter.countAndPrint();
				}
			}
			break;
		case GFF2SAJR:
			Gff2SAJR.gff2sajr();
			break;
		case GFF32SAJR:
			Gff2SAJR.gff32sajr();
			break;
		case SAJRCOMP:
			Annotation.compare(Settings.S().getString(Settings.COMP_ANN1), 
					Settings.S().getString(Settings.COMP_ANN2),
					Settings.S().getString(Settings.COMP_OUT));
			break;
		case SAJR2TRANSC:
			TranscExtractor.extractTranscripts(
					Settings.S().getString(Settings.GFF_IN), 
					Settings.S().getString(Settings.GFF_OUT), 
					Settings.S().getString(Settings.SEG_COUNTS_PATH),
					Settings.S().getDouble(Settings.IR_PSI),
					Settings.S().getDouble(Settings.AS_PSI));
			break;
		default:
			System.out.println("Unrecognized method: '"+args[0]+"'");
			printHelp();
		}
	}
	
	private static boolean readParams(String[] args) throws ClassNotFoundException {
		Class.forName("util.Log");
		if(args.length == 0) {	
			System.out.println("please choose method.");
			return false;
		}
		String p = null;
		try{
			p = parseArgs(args);
		}catch(Exception e) {
			System.out.println("Unexpected parameter format.");
			return false;
		}
		if(p != null) {
			System.out.println("Unrecognized parameter: '"+p+"'");
			return false;
		}
		return true;
	}

	private static String parseArgs(String[] a) {
		if(a.length == 1)
			return null;
		int i = 1;
		if(a[i].charAt(0) != '-') {
			Settings.setSettings(a[i]);
			i++;
		}
		for(;i<a.length;i++) {
			String[] t = a[i].substring(1).split("=");
			Settings.S().set(t[0], t[1]);
		}
		return null;
	}
	
	private static void printHelp() {
		System.out.println();
		System.out.println(Settings.VERSION);
		System.out.println("Developed by Mazin Pavel");
		System.out.println("mailto: iaa.aka@gmail.com");
		System.out.println("site: http://storage.bioinf.fbb.msu.ru/~mazin/");
		System.out.println("cite: P. Mazin et al. MSB 9:633 (2013).");
		System.out.println("Moscow, 2012");
		System.out.println();
		System.out.println("usage: method={count_reads|gff2sajr|gff32sajr|annotate|sajrcomp|sajr2transc} [settings file] [optionis]");
		System.out.println("Options, in form of -option_name=value can be used to overwride any settings from settings file");
		System.out.println("Example: annotate mysettings -ann_out=myAnnotation.gff");
	}
}
