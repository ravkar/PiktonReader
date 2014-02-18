package net.pikton.reader;

public class LicenseException extends ReaderEngineException{

	private static final long serialVersionUID = 7737689086892635581L;

	public LicenseException(String s, Throwable th) {
		super(s, th);
	}

	public LicenseException(String s) {
		super(s);
	}

	public LicenseException(Throwable th) {
		super(th);
	}

}
