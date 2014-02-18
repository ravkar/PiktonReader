package net.pikton.reader;

public interface ReaderObservable {
	public  void addReaderListener(ReaderListener listener);
	public  void removeReaderListener(ReaderListener aListener);
}