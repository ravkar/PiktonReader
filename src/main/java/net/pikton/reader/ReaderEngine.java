package net.pikton.reader;


public interface ReaderEngine extends ReaderObservable{
	
	void connect() throws ReaderEngineException;
	
	void disconnect() throws ReaderEngineException;

	ReaderWindow getReaderWindow();
}
