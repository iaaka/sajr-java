package util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class Log {
	static PrintStream out = System.out;
	static PrintStream err = System.err;
	
	static HashMap<String, Integer> stat = new HashMap<String, Integer>();
	//annotator stat
	static public final String BAD_JUNCTIONS_CNT = "#bad junctions";
	//read_counter stat
	static public final String JUNCTIONS_CNT = "#junctions";
	static public final String TOTAL_READS = "#total records";
	static public final String MULTI_READS = "#multiple reads records";
	static public final String USED_READS = "#used records";
	static public final String UNKNOWN_JUNCTION = "#unknown junction";
	static public final String UNKNOWN_JUNCTION_COMB = "#unknown junction combination";
	static public final String GENE_READS = "#gene records";
	static public final String EXON_READS = "#exon records";
	//static public final String UNKNOWN_CHR = "#unknown chr";
	static public final String PAIRED = "#paired records";
	static public final String SINGLETONS = "#singletons records";
	static public final String UNMAPPED = "#unmapped reads";
	static public final String NEW_JUNCTIONS_FOUND = "#new junctions found";
	
	static public final long START_TIME = System.currentTimeMillis();
	
	static {
		
	}
	
	public static void println(Object o) {
		if(Settings.S().getBoolean(Settings.VERBOSE)) {
			new Date(System.currentTimeMillis()).toString();
			long t = (System.currentTimeMillis()-START_TIME); 
			String time = t>10000?(t>600000?(t/60000+"m"):((t/1000)+"s")):(t+"ms");
			out.println("[Time elapsed: "+time+"] "+o);
		}
	}
	
	
	public static void warn(String w) {
		if(!Settings.S().getBoolean(Settings.SUPPRESS_WARNINGS))
			println("[WARN] "+w);
	}
	
	public static void throwUncrucialExc(String w) {
		if(Settings.S().getBoolean(Settings.EXCEPTION2WARN))
			warn(w);
		else {
			closeWithError(w,null);
		}
	}
		
	public static void closeWithError(String w,Exception e) {
		err.println("[FATAL EXCEPTION] "+w);
		if(Settings.S().getBoolean(Settings.DEBUG) && e != null)
			e.printStackTrace();
		System.exit(0);
	}
	
	public static void addStat(String name,int add) {
		Integer i = stat.get(name);
		if( i == null) 
			i = 0;
		stat.put(name, i+add);	
	}
	
	public static void cleanStat() {
		stat = new HashMap<String, Integer>();
	}
	
	public static void printStat() {
		println("Statistics:");
		if(Settings.S().getBoolean(Settings.VERBOSE)) {
			ArrayList<String> ks = new ArrayList<>(stat.keySet());
			Collections.sort(ks,new Comparator<String>() {

				public int compare(String o1, String o2) {
					return stat.get(o2) - stat.get(o1);
				}
			});
			for(String k : ks)
				System.out.println(k+" = "+stat.get(k));
		}
	}
}
