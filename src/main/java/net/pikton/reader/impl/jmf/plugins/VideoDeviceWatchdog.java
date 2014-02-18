package net.pikton.reader.impl.jmf.plugins;

import net.pikton.reader.ReaderListener;
import net.pikton.reader.ReaderObservable;
import net.pikton.reader.VideoDeviceEvent;
import net.pikton.reader.VideoDeviceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class VideoDeviceWatchdog implements Runnable, ReaderObservable{
	static Logger logger = LoggerFactory.getLogger(VideoDeviceWatchdog.class);		
	
	static VideoDeviceWatchdog instance;
	
	volatile int timer;
	volatile boolean isActive;
	
	Thread thread;
	Object lock = new Object();
		
	List<VideoDeviceListener> videoDeviceListeners;
	
	public VideoDeviceWatchdog(){	
		  videoDeviceListeners = Collections.synchronizedList(new LinkedList<VideoDeviceListener>());
		  thread = new Thread(this);
		  thread.setDaemon(true);		
		  thread.start();
		  logger.debug(Thread.currentThread() + " Watchdog thread started.");		
	}
			
	public static VideoDeviceWatchdog getInstance() {
		if (instance == null){
			instance = new VideoDeviceWatchdog();
		}
		return instance;
	}
		
	public void addReaderListener(ReaderListener aListener){
		synchronized(videoDeviceListeners){
			videoDeviceListeners.add((VideoDeviceListener)aListener);
		}
	}
	
	public void removeReaderListener(ReaderListener aListener){
		synchronized(videoDeviceListeners){
			videoDeviceListeners.remove(aListener);
		}
	}
	
	protected void notifyFrameListeners(int aState){		
		synchronized(videoDeviceListeners){
			for(VideoDeviceListener f: videoDeviceListeners){
				f.onVideoDeviceEvent(aState);
			}					
		}
	}

	public void setActive(boolean anIsActive){	
		isActive = anIsActive;
		  logger.debug(Thread.currentThread() + " Watchdog active->" + isActive);			
	}
	
	void stop(){
		thread.interrupt();
	}
	
	public void reset(){
		timer = 0;
	}
			
	public void run() {
		while(!thread.isInterrupted()){
			synchronized(lock){
				try {
					lock.wait(2000);
				} catch (InterruptedException e) {					
				}				
				timer++;
			}
			if (timer > 1  && isActive){		
				logger.warn("Media stream disconnection detected. Trying reconnect...");
				notifyFrameListeners(VideoDeviceEvent.EVENT_DISCONNECTED);	
				reset();
			}
		}
		logger.debug(Thread.currentThread() + " Finishing watchdog.");	
	}	
}
