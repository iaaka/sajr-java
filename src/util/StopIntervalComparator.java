package util;

import java.util.Comparator;

public class StopIntervalComparator implements Comparator<Interval> {


	public int compare(Interval o1, Interval o2) {
		if(o1.strand != o2.strand)
			return o1.strand - o2.strand;
		if(o1.stop != o2.stop)
			return o1.stop - o2.stop;
		return o1.start-o2.start;
	}

}
