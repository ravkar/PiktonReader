package net.pikton.server.handler;

import net.pikton.reader.ReaderEngine;
import net.pikton.reader.ReaderObservable;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * @author rwa
 *
 */
public class ReaderServerHandler extends AbstractHandler
{
	static Logger logger = LoggerFactory.getLogger(ReaderServerHandler.class);		
	
	private ReaderEngine readerEngine;
	
	private static volatile int activeRequests;
	
	public ReaderServerHandler(ReaderEngine aReaderEngine){
		this.readerEngine =  aReaderEngine;
	}
	
    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException
    {        
    	activeRequests++;
		logger.debug("started active request instance->" + activeRequests);  
    	logger.debug("request->" + baseRequest);
    	logger.debug("servlet request->" + request);
    	
    	net.pikton.reader.ReaderListener localListener = null;
    	PrintWriter writer = null;
    	final String[] readerResult = new String[1];    	
    	try{
	    	final Object lockObj = new Object();

	    	((ReaderObservable)readerEngine).addReaderListener(localListener = new net.pikton.reader.ReaderListener(){

				public void onFrameEvent(Object aResult) {
			    	logger.debug("onEvent->" + aResult);				
					synchronized(lockObj){
						readerResult[0] = (String)aResult;
						lockObj.notifyAll();
					}				
				}					
	        });
	        
	        synchronized(lockObj){
	        	try {
					lockObj.wait();
					logger.debug(Thread.currentThread().getName() + " is waiting for response...");
				} catch (InterruptedException e) {
					logger.error("Error during waiting for reader response.",e);
				}
	        }        	        
		    String jsonResponse = "handleReaderResponse(" + readerResult[0] + ");";
		       	    	    	    
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html; charset=utf-8");
			
			baseRequest.setHandled(true);

			writer = response.getWriter();
			writer.println(jsonResponse);
			logger.info("got response->" + jsonResponse);  			
    	}finally{
    		writer.flush();
    		writer.close();
    		readerResult[0] = null;
    		((ReaderObservable)readerEngine).removeReaderListener(localListener);
    		activeRequests--;
    		logger.debug("remained active requests count->" + activeRequests);  			    		
    	}             
    }
}
