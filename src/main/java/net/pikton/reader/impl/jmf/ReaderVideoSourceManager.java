package net.pikton.reader.impl.jmf;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;

import net.pikton.reader.ReaderEngineException;
import net.pikton.reader.config.ReaderConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author rwa
 *
 */
public class ReaderVideoSourceManager{   
	static Logger logger = LoggerFactory.getLogger(ReaderVideoSourceManager.class);		
	
    private DataSource dataSource;
    
	public ReaderVideoSourceManager() throws ReaderEngineException{    		
		MediaLocator mediaLocator = new MediaLocator(ReaderConfiguration.DEFAULT_MEDIA_LOCATOR);
		try {
			dataSource = Manager.createDataSource(mediaLocator);			
			logger.debug("Device info-> " + ((CaptureDevice)dataSource).getCaptureDeviceInfo());			
		} catch (Exception e) {
			logger.error("Video source manager initialization error.",e);
			throw new ReaderEngineException(e); 
		}
	}
	
	public DataSource getInnerSource(){
		return dataSource;
	}
		
	public void setVideoFormat(Dimension aVideoFormatDimension) throws ReaderEngineException{			
		FormatControl formatControl = ((CaptureDevice)dataSource).getFormatControls()[0];
		
		Map<Dimension,VideoFormat> videoFormats = new HashMap<Dimension,VideoFormat>();
		
		for(Format f: formatControl.getSupportedFormats()){
			if (f instanceof VideoFormat){
				VideoFormat vf = (VideoFormat)f;
				videoFormats.put(vf.getSize(), vf);
			}
		}
		logger.debug("Video formats -> " + videoFormats);
				
		if (!videoFormats.containsKey(aVideoFormatDimension)){			
			ReaderEngineException re =  new ReaderEngineException("Video format not found.");
			throw re;
		}
		// set video format selected
		VideoFormat captureFormat = videoFormats.get(aVideoFormatDimension);
		formatControl.setFormat(captureFormat);
	}	
}	