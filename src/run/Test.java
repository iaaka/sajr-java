package run;

public class Test {
	String version = "$LastChangedDate: 2014-09-08 12:04:56 +0400 (Mon, 08 Sep 2014) $\n$LastChangedRevision: 5128 $".replace("$", "");
	
	public static void main(String[] args)  {
		Run.main(new String[]{"annotate"});
		//Run.main(new String[]{"sajrcomp"});
	}
}
