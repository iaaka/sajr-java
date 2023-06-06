package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Settings {
	private HashMap<String, Object> params;
	private ArrayList<String> param_names;
	private HashMap<String,String> param_group;
	private String del = ": ";
	static String fname = "sajr.config";
	private static Settings singleton;

	public static final String IN = "in";
	public static final String FASTA = "fasta";
	public static final String STRANDED = "stranded";
	public static final String SITE_USAGE_FREQ = "site_usage_freq";
	public static final String MAX_COV_GAP = "max_cov_gap";
	public static final String MIN_COV = "min_cov";
	public static final String USE_MULT = "use_mult";
	public static final String USE_SINGLETONS = "use_singletons";
	public static final String JUNC_OVERHANG = "junc_overhang";
	public static final String INDEP_POS = "indep_pos";
	public static final String EXCEPTION2WARN = "exception2warn";
	public static final String ANN_OUT = "ann_out";
	public static final String PAIRED = "paired";
	public static final String ANN_IN = "ann_in";
	public static final String COUNT_ONLY_BORDER_READS = "count_only_border_reads";
	public static final String COUNT_INTRON_READS = "count_intron_reads";
	public static final String VERBOSE = "verbose";
	public static final String SUPPRESS_WARNINGS = "suppress_warnings";
	public static final String OUT_BASE = "out_base";
	public static final String COUNT_ONLY_INTERNAL = "count_only_internal";
	public static final String EFFECTIVE_READ_LENGTH = "effective_read_length";
	public static final String USE_READS_WITH_UNKNOWN_JUNCTIONS = "use_reads_with_unknown_junctions";
	public static final String ONLY_JUNCTIONS_FROM_SAME_GENE = "only_junctions_from_same_gene";
	public static final String BATCH_IN = "batch_in";
	public static final String BATCH_OUT = "batch_out";
	public static final String DEBUG = "debug";
	public static final String ANN_FOREIGN = "ann_foreign";
	public static final String GENE_BLACK_LIST  = "gene_black_list";
	public static final String LOOK_FOR_GENE_FOR_UNKNOWN_JUNCTIONS  = "look_for_gene_for_unknown_junctions";
	public static final String MIN_SINGLE_EXON_GENE_LENGTH  = "min_single_exon_gene_length";
	public static final String MIN_SINGLE_EXON_GENE_COV  = "min_single_exon_gene_cov";
	public static final String ID_PREFIX  = "id_prefix";
	public static final String COV_WIN_LEN  = "cov_win_len";
	public static final String MAX_COV_STEP  = "max_cov_step";
	public static final String FILL_NS  = "fill_ns";
	public static final String FOREIGN_JUNC_COV  = "foreign_junc_cov";
	public static final String FORSED_INTRON_SET = "forced_intron_set";
	
	public static final String GFF_IN  = "gff_in";
	public static final String GFF_OUT  = "gff_out";
	public static final String SEG_COUNTS_PATH  = "seg_counts_path";
	public static final String IR_PSI  = "ir_psi";
	public static final String AS_PSI  = "as_psi";
	
	public static final String COMP_ANN1 = "comp_ann1";
	public static final String COMP_ANN2 = "comp_ann2";
	public static final String COMP_OUT = "comp_out";
	
	public static final String SHORT_VERSION = "SAJR-0.1";
	public static final String VERSION = SHORT_VERSION+": Splicing Ananalyzer by Java&R";
	
	private Settings() throws IOException{
		params = new HashMap<String, Object>();
		param_names = new ArrayList<String>();
		if(!(new File(fname)).exists()) {
			throw new RuntimeException("Settings file '"+fname+"' doesn't exist!");
		}
		BufferedReader i = new BufferedReader(new FileReader(fname));
		ArrayList<String> cgroup = new ArrayList<String>();
		param_group = new HashMap<String, String>();
		for(String l = i.readLine();l!=null;l=i.readLine()){
			try{
				l = l.replace("\t", "");
				if(l.length()==0 || l.startsWith("#")){
					continue;
				}
				if(l.equals("</>")){
					cgroup.remove(cgroup.size()-1);
					continue;
				}
				if(l.startsWith("<")){
					cgroup.add(l.substring(1,l.length()-1));
					continue;
				}
				l = l.substring(0,l.indexOf(';'));
				String[] t = l.split(del);
				params.put(t[0], t[1]);
				param_names.add(t[0]);
				param_group.put(t[0],Util.join(cgroup,"."));
			}catch(Exception e) {
				Log.closeWithError("Wrong settings file '"+fname+"': cannot parse line '"+l+"'",e);
			}
		}
		i.close();
	}
	
	public static void setSettings(String file){
		fname = file;
		singleton = null;
	}
	
	public static Settings S(){
		if(singleton == null)
			try {
				singleton = new Settings();
			} catch (IOException e) {
				Log.closeWithError("Cannot read settings file '"+fname+"'",e);
			}
		return singleton;
	}
	
	
	
	public boolean paramExists(String name){
		return params.containsKey(name);
	}
	
	
	public String getParamGroup(String name){
		return param_group.get(name);
	}
		
	
	public int getInt(String name){
		Object t = get(name);
		if(t instanceof String){
			try {
				t = Integer.parseInt((String)t);
			}catch(NumberFormatException e) {
				Log.closeWithError("Wrong value for parameter "+name+" = "+t+". Integer expected.", e);
			}
			params.put(name, t);
		}
		return (Integer) t;
	}
	
	public double getDouble(String name){
		Object t = get(name);
		if(t instanceof String){
			try {
				t = Double.parseDouble((String)t);
			}catch(NumberFormatException e) {
				Log.closeWithError("Wrong value for parameter "+name+" = "+t+". Double expected.", e);
			}
			params.put(name, t);
		}
		return (Double) t;
	}
	
	private Object get(String name) {
		if(!params.containsKey(name))
			Log.closeWithError("Parameter "+name+" is unset", new RuntimeException());
		return params.get(name);
	}
	
	public Boolean getBoolean(String name){
		Object t = get(name);
		if(t instanceof String){
			if(t == null || (!t.equals("true") && !t.equals("false")))
				Log.closeWithError("Wrong value for parameter "+name+" = "+t+". true/false expected.",  new RuntimeException());
			t = Boolean.parseBoolean((String)t);
			params.put(name, t);
		}
		return (Boolean) t;
	}
	
	
	public String getString(String name){
		return get(name).toString();
	}
	
	public void set(String name,String v){
		Object oldv = params.put(name, v);
		if(oldv == null)
			Log.closeWithError("Attempt to set parameter with unrecognized name: "+name+".", new RuntimeException());
	}
	
	public void printSettings(String pref,HashSet<String> groups,PrintStream o) {
		for(String k : param_names)
			if(groups.contains(getParamGroup(k)))
				o.println(pref+k+": "+params.get(k));
	}
}
