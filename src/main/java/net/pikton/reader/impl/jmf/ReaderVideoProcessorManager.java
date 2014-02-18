package net.pikton.reader.impl.jmf;

import net.pikton.reader.ReaderEngineException;
import net.pikton.reader.ReaderListener;
import net.pikton.reader.ReaderObservable;
import net.pikton.reader.config.ReaderConfiguration;
import net.pikton.reader.impl.jmf.plugins.autoid.FrameDecoderZxing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import java.awt.*;
import java.util.List;

/**
 * 
 * @author rwa
 *
 */
public class ReaderVideoProcessorManager  implements ControllerListener{
	static Logger logger = LoggerFactory.getLogger(ReaderVideoProcessorManager.class);		
	
	Object waitSync = new Object();
	boolean stateTransitionOK = true; 	
	Processor processor;
	Component visualComponent;
	Component controlPanelComponent;
    List<String> readerResults;	
	
	public ReaderVideoProcessorManager(Processor aProcessor){
		this.processor = aProcessor;
	}
	
	public ReaderVideoProcessorManager(DataSource anInputDataSource, List<String> aResultsQueue) throws ReaderEngineException{
		this.readerResults = aResultsQueue;
		try{
			this.processor = Manager.createProcessor(anInputDataSource);
			this.processor.addControllerListener(this);
			this.processor.configure();			
			if (!waitForState(processor.Configured)) {
				String err = "Failed during configure the processor.";
				logger.error(err);
				throw new ReaderEngineException(err);
			}			
			processor.setContentDescriptor(null); 
		} catch (Exception e) {
			logger.error("Processor manager initialization error.", e);
			throw new ReaderEngineException(e); 
		}
	}  
		

	public void start() throws ReaderEngineException {
		// Realize the processor.
		processor.prefetch();
		if (!waitForState(processor.Prefetched)) {
			logger.error("Failed to realize the processor.");
		}
		visualComponent = processor.getVisualComponent();
		controlPanelComponent = processor.getControlPanelComponent();
		processor.start();
	}

	public void stop() throws ReaderEngineException {
		processor.stop();		
	}

	public void close() throws ReaderEngineException {
		processor.close();
	}

	public void configure() throws ReaderEngineException {
		TrackControl tc[] = processor.getTrackControls();
		
		// Search for the track control for the video track.
		TrackControl videoTrack = null;
		
		for (int i = 0; i < tc.length; i++) {
			if (tc[i].getFormat() instanceof VideoFormat) {
				videoTrack = tc[i];
				break;
			}
		}
		
		if (videoTrack == null) {
			String s = "The input media does not contain a video track.";
			logger.error(s);
			throw new ReaderEngineException(s);
		}
		logger.info("Video format: " + videoTrack.getFormat());

		//TODO refactor - configure plugins passed from PluginManager  
		Codec codec[];
		try {			
			FrameDecoderZxing piktonFrameDecoder = new FrameDecoderZxing(ReaderConfiguration.getInstance());
			((ReaderObservable)piktonFrameDecoder).addReaderListener(new ReaderListenerImpl());
			codec = new Codec[1];			
			codec[0] = piktonFrameDecoder;
			videoTrack.setCodecChain(codec);
		} catch (UnsupportedPlugInException e) {
			logger.error("The processor does not support plugins.");
			throw new ReaderEngineException(e);			
		}		
	}
	
	public Component getVisualComponent() {
		return visualComponent;
	}

	public Component getControlPanelComponent() {
		return controlPanelComponent;
	}

	/**
	* Block until the processor has transitioned to the given state.
	* Return false if the transition failed.
	*/
	private boolean waitForState(int state) {
		synchronized (waitSync) {
			try {
				while (processor.getState() != state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e) {
				logger.error("Error during waiting for processor state change.", e);
			}
		}
		return stateTransitionOK;
	}    
	
	/**
	* Controller Listener.
	*/
	public void controllerUpdate(ControllerEvent evt) {    		
		if (evt instanceof ConfigureCompleteEvent ||
			evt instanceof RealizeCompleteEvent ||
			evt instanceof PrefetchCompleteEvent ||
			evt instanceof StopByRequestEvent ||
			evt instanceof ControllerClosedEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
			logger.info("end of media event !!! Closing processor" );	
			processor.close();			
		}else{
			logger.info("other event->" + evt);	
		}
	}    
	
	class ReaderListenerImpl implements ReaderListener {				
		public void onFrameEvent(Object o) {
			logger.info("fire Frame Event->" + o);			
			synchronized(readerResults){
				readerResults.add((String)o);
				readerResults.notifyAll();
			}
		}		
	}
}