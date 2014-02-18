package net.pikton.reader;

public class ReaderEngineException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5892660056017821513L;

	public ReaderEngineException(String s){
		super(s);
	}
	
	public ReaderEngineException(String s, Throwable th){
		super(s, th);
	}
	
	public ReaderEngineException(Throwable th){
		super(th);
	}	
}
