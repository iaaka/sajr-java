package util;

public class Interval implements Comparable<Interval>{
	public final int start;
	public final int stop;
	public final int strand;
	private String id= null;
	protected double cov = 0;
	private boolean unstranded=false;
	
	public Interval(int start,int stop, int strand) {
		this.start = start;
		this.stop = stop;
		this.strand = strand;
	}
	
	public Interval(int start,int stop, int strand,String id) {
		this(start,stop,strand);
		this.id = id;
	}
	
	public double getCov() {
		return cov;
	}
	
	public void addCov() {
		cov++;
	}
	
	public String getId(){
		return id;
	}

	public boolean equals(Object arg0) {
		if(arg0 == null || this.getClass() != arg0.getClass())
			return false;
		if(this == arg0)
			return true;
		if(arg0 instanceof Interval){
			Interval i = (Interval) arg0;
			return (unstranded || i.strand == strand) && i.start == start && i.stop == stop && ((id == null && i.id == null) || id.equals(i.id)); 
		}
		return false;
	}
	
	/**
	 * if true, strand doesn't affect equals and compareTo methods;
	 * @param us
	 */
	public void setUnstranded(boolean us){
		unstranded = us;
	}

	public int compareTo(Interval o) {
		if(!unstranded && o.strand != strand)
			return strand - o.strand;
		if(o.start != start)
			return start - o.start;
		return stop-o.stop;
	}

	public boolean overlap(Interval i){
		return i.start <= stop && i.stop>= start;
	}

	public int hashCode() {
		return (strand!=0?strand:1)*(start+stop);
	}
	
	public String toString() {
		return getClass().getName()+(id != null?" (id="+id+")":"")+": "+strand+":"+start+"-"+stop;
	}
	
	public int length(){
		return stop-start+1;
	}
	
}
