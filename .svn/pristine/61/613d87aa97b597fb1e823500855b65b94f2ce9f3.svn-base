package util.bio;

import java.util.HashSet;
import util.Interval;


public class Intron extends Interval {
	private int max_ovehang = 0;
	private HashSet<Integer> read_positions = null;
	Integer position_no = null;
	
	//private int[][] overhangStat = null;
	//int min_read_length = -1;

	
	
	public Intron(int start, int stop, int strand) {
		this(start, stop, strand, null);
	}

	public Intron(int start, int stop, int strand,String id) {
		super(start, stop, strand, id);
	}
	
	//isUniq was used for overhangStat, it can be removed
	public void addRead(int readLength, int overhang,int read_start,boolean isUniq){
		/*if(overhangStat == null){
			overhangStat = new int[2][readLength];
			min_read_length = readLength;
		}*/
		int oh = Math.min(overhang, readLength-overhang);
		if(read_positions == null)
			read_positions = new HashSet<>();
		max_ovehang = Math.max(max_ovehang,oh);
		read_positions.add(read_start);
		cov++;
		
		//we "trim" all reads to length of the shortest read (only for overhang calculation)
		/*min_read_length = Math.min(readLength, readLength);
		if(overhang < overhangStat[0].length)
			if(isUniq)
				overhangStat[0][overhang]++;
			else
				overhangStat[1][overhang]++;*/
	}
	
	/*public String overhangStat2String(boolean isUniq){
		int[] a = overhangStat[0];
		if(!isUniq)
			a = overhangStat[1];
		StringBuffer r = new StringBuffer(1000);
		r.append(a[0]);
		for(int i = 1;i<min_read_length-1;i++)
			r.append(",").append(a[i]);
		return r.toString();
	}*/
	
	public void setMaxOverhang(int o){
		max_ovehang = o;
	}
	
	public void setCov(double c){
		cov = c;
	}
	
	public void setPosNo(int pn){
		position_no = pn;
	}
	
	public int getMaxOverhang(){
		return max_ovehang;
	}
		
	public int getPosNo(){
		//used to force usage of this intron
		if(position_no != null)
			return position_no;
		return read_positions == null?0:read_positions.size();
	}
	
	public void addReads(Intron i){
		if(read_positions == null)
			read_positions = new HashSet<>();
		max_ovehang = Math.max(max_ovehang,i.max_ovehang);
		read_positions.addAll(i.read_positions);
		cov+=i.cov;
		/*min_read_length = Math.min(min_read_length, i.min_read_length);
		for(int u=0;u<overhangStat.length;u++)
			for(int j=0;j<min_read_length;j++)
				overhangStat[u][j]+=i.overhangStat[u][j];
				*/
	}

}
