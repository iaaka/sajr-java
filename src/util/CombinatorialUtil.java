package util;

public class CombinatorialUtil {
	
	
	public static double logNoOfCombByStirling(double n,double k){
		return logOfFactorialByStirling(n) - logOfFactorialByStirling(k) - logOfFactorialByStirling(n-k);
	}
	
	public static double logOfFactorialByStirling(double n){
		if(n == 0)
			return 0;
		return 0.5*(Math.log(Math.PI)+Math.log(2)+Math.log(n))+n*(Math.log(n)-1);
	}
	
	public static double factorial(double n){
		if(n>2)
			return n*factorial(n-1);
		else
			return n;
	}
	
	
	/**
	 * gives probability to have k empty bins when distributing n balls into l bins
	 * @param l
	 * @param n
	 * @param k
	 */
	public static double getProbOfEmptyBins(int l,int n,int k){
		if(n < l-k)
			return 0;
		//double lp = logNoOfCombByStirling(n-1,l-k-1)+logNoOfCombByStirling(l,k)-logNoOfCombByStirling(n+l-1,n);
		double lp = logNoOfCombByStirling(n,l-k) +
					logOfFactorialByStirling(l-k) +
					logNoOfCombByStirling(l,k) + 
					Math.log(l-k)*(n-l+k)- Math.log(l)*n;
		return Math.exp(lp);
	}
	
	public static double emulateProbOfEmptyBins(int l,int n,int k,int iters){
		double r = 0;
		for(int i=0;i<iters;i++){
			int[] t = new int[l];
			for(int j=0;j<n;j++)
				t[(int) (l*Math.random())]++;
			int zeros = 0;
			for(int tt : t)
				if(tt == 0)
					zeros++;
			if(zeros == k)
				r++;
		}
			
		return r/iters;
	}
	
	public static double[][] getProbOfNotEmptyBins(int l,int nm){
		double[][] r = new double[nm+1][l+1];
		r[0][0] = 1;
		for(int n=1;n<=nm;n++){
			for(int k=0;k<=l;k++){
				double p = ((double)(l-k))/l;
				if(k != l)
					r[n][k+1] += r[n-1][k] * p;
				r[n][k]   += r[n-1][k] * (1-p);
			}
		}
		return r;
	}
	
	public static void main(String[] args) {
		int k = 5;
		int l = 20;
		int mx = 40;
		long tm = System.currentTimeMillis();
		double[][] t = getProbOfNotEmptyBins(l,mx);
		System.out.println("time: "+(System.currentTimeMillis()-tm));
		for(int n=0;n<mx;n+=2)
			System.out.println(n+"\t"+t[n][l-k]+"\t"+emulateProbOfEmptyBins(l,n,k,100000));
		System.out.println("time: "+(System.currentTimeMillis()-tm));
	}

}
