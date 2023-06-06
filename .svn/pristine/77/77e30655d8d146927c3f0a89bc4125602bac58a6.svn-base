package util.bio;

import util.Interval;

public class Seg extends Interval {
	public enum segType{EXN,ALT,INT,NA};
	public enum segPos{FIRST,LAST,INTERNAL,ONLY};
	public final segType segtype;
	public final segPos segpos;
	
	public Seg(int start, int stop, int strand){
		this(start, stop, strand, segType.NA, segPos.ONLY, null);
	}
	
	public Seg(int start, int stop, int strand,segType t,segPos p){
		this(start, stop, strand, t, p, null);
	}

	public Seg(int start, int stop, int strand,segType t,segPos p,String id) {
		super(start, stop, strand,id);
		segtype = t;
		segpos = p;
	}

}
