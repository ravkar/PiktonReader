package net.pikton.reader.impl;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import net.pikton.reader.Registration;
import net.pikton.reader.ReaderEngine;
import net.pikton.reader.ReaderEngineException;
import net.pikton.reader.ReaderListener;
import net.pikton.reader.ReaderObservable;
import net.pikton.reader.ReaderWindow;
import net.pikton.reader.VideoDeviceListener;
import net.pikton.reader.config.ReaderConfiguration;
import net.pikton.reader.impl.jmf.ReaderVideoProcessorManager;
import net.pikton.reader.impl.jmf.ReaderVideoSourceManager;
import net.pikton.reader.impl.jmf.plugins.VideoDeviceWatchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author rwa
 *
 */
public class ReaderEngineImpl implements ReaderEngine, ReaderObservable{

	static Logger logger = LoggerFactory.getLogger(ReaderEngineImpl.class);	
    
	private ReaderVideoSourceManager videoSourceManager;
	private ReaderVideoProcessorManager processorManager;    
	private ReaderResultBroadcaster broadcaster;
	private ReaderWindow readerWindow;
    
    private List<String> readerResults;    
    private List<ReaderListener> readerListeners;
    
    private volatile boolean readerStarted;
    
    private VideoDeviceListener videoDeviceListenerImpl;
    
    private Registration licenseChecker;
        
	public ReaderEngineImpl() throws ReaderEngineException{				
		licenseChecker = new Registration(); 
		readerListeners = new LinkedList<ReaderListener>();
		readerResults = new LinkedList<String>();	
		readerWindow = new ReaderWindow(this);	
		videoDeviceListenerImpl = new VideoDeviceListenerImpl(this);
		VideoDeviceWatchdog.getInstance().addReaderListener(videoDeviceListenerImpl);		
	}
 
	public synchronized void connect() throws ReaderEngineException{
		VideoDeviceWatchdog.getInstance().setActive(true);			
		if (!readerStarted){
			try
			{
				videoSourceManager = new ReaderVideoSourceManager();
				videoSourceManager.setVideoFormat(ReaderConfiguration.getInstance().getVideoFormatDimension());			
	
				processorManager = new ReaderVideoProcessorManager(videoSourceManager.getInnerSource(), readerResults);
				processorManager.configure();
				
				broadcaster = new ReaderResultBroadcaster();
				broadcaster.start();
							
				processorManager.start();			
				
				readerWindow.showReaderWindow();	
				
				readerStarted = true;								
				logger.info("Reader engine started.");				
			}
			catch(Exception e)
			{
				logger.error("Failed to initialize reader engine: ", e);
				throw new ReaderEngineException("Failed to initialize reader engine: ",e) ;
			}										
		}		
	}

	public synchronized void disconnect() throws ReaderEngineException{
		VideoDeviceWatchdog.getInstance().setActive(false);				
		if (readerStarted){		
			broadcaster.stop();
			processorManager.stop();
			processorManager.close();
			readerStarted = false;
			broadcaster = null;
			processorManager = null;
		}
	}
		
	public Component getVisualComponent() {		
		return processorManager.getVisualComponent();
	}

	public Component getControlPanelComponent() {
		return processorManager.getControlPanelComponent();
	}
	
	public ReaderWindow getReaderWindow() {
		return readerWindow;
	}

	public void addReaderListener(ReaderListener aListener) {
		synchronized(readerListeners){
			readerListeners.add(aListener);
		}
	}

	public void removeReaderListener(ReaderListener aListener) {
		synchronized(readerListeners){
			readerListeners.remove(aListener);
		}		
	}
	
	private void notifyFrameEvent(String aReaderResult){
		synchronized(readerListeners){
			for(ReaderListener listener : readerListeners){
				listener.onFrameEvent(aReaderResult);
			}
		}			
	}
		
	class ReaderResultBroadcaster implements Runnable{
		Thread t = null;
		
		public void start(){
			t = new Thread(this);
			t.setDaemon(true);
			t.setName(ReaderResultBroadcaster.class.getName());
			t.start();			
		}
		
		public void stop(){
			t.interrupt();
		}		
		
		public void run() {
			do{
				synchronized(readerResults){
					if (readerResults.size() > 0){
						String result = readerResults.remove(0);
						ReaderEngineImpl.this.notifyFrameEvent(result);
					}else{
						try {
							readerResults.wait();
						} catch (InterruptedException e) {
							logger.error("Error during waiting for msg", e);
						}																		
					}					
				}
			}while(readerStarted);			
			logger.debug("Closing msg broadcaster.");
		}		
	}
}
