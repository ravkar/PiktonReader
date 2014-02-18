package net.pikton.reader.impl;

import net.pikton.reader.ReaderEngine;
import net.pikton.reader.ReaderEngineException;
import net.pikton.reader.VideoDeviceEvent;
import net.pikton.reader.VideoDeviceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoDeviceListenerImpl implements VideoDeviceListener{
	
	static Logger logger = LoggerFactory.getLogger(VideoDeviceListenerImpl.class);		
	
	ReaderEngine readerEngine;
	
	public VideoDeviceListenerImpl(ReaderEngine anEngine){
		readerEngine = anEngine;
	}

	public void onFrameEvent(Object o) {
	}

	public void onVideoDeviceEvent(int anEvent) {
		 switch(anEvent){
		 	case VideoDeviceEvent.EVENT_DISCONNECTED:
				 reconnect();  
				 break;
		    case VideoDeviceEvent.EVENT_CONNECTED:			 
			 	 break;
		}
	}
	
	void reconnect(){	
		try {
			logger.debug("disconnect");				
			readerEngine.disconnect();	
			logger.debug("disconnect");				
			readerEngine.connect();
			logger.info("Reconnection success.");
		} catch (ReaderEngineException e) {
			logger.error("Reconnection failed.", e);
		}		
	}
}
