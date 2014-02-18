package net.pikton.reader;

import net.pikton.reader.config.ReaderConfiguration;
import net.pikton.reader.impl.ReaderEngineImpl;
import net.pikton.server.handler.ReaderServerHandler;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationMain extends ApplicationBase {
	
	static Logger logger = LoggerFactory.getLogger(ApplicationMain.class);
		
	public static void main(String[] args) throws Exception {				
		ReaderEngine reader = null;
		try {
			reader = new ReaderEngineImpl();		
			reader.connect();	
		} catch (Throwable e) { 
			logger.error("Reader engine initialization failed.", e);
			System.exit(-1);
		}								
		Server server = new Server(ReaderConfiguration.getInstance().getReaderPort());
		server.setHandler(new ReaderServerHandler(reader));
				
		server.start();
		server.join();	
	}
}
