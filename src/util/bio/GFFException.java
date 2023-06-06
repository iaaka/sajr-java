package util.bio;

public class GFFException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3942254095661378129L;

	public GFFException(String message,String line) {
		super(message+". At line :'"+line+"'");
	}

}
