package exceptions;

@SuppressWarnings("serial")
public class AliasExistsException extends RuntimeException{
	
	public AliasExistsException(){
		System.err.println("Entered alias already exists!");;
	}

}
