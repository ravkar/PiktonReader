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

        int port = -1;
        try{
            port = ReaderConfiguration.getInstance().getReaderPort();
            Server server = new Server(port);
            server.setHandler(new ReaderServerHandler(reader));
            server.start();

            server.join();
        } catch (Throwable e) {
            logger.error("Callback-server initialization failed. Check security settings to listen on TCP port " + port, e);
        }
	}
}
