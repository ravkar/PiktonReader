package net.pikton.reader.impl.jmf.plugins;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.media.Buffer;
import javax.media.Control;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;

import net.pikton.reader.ReaderListener;
import net.pikton.reader.ReaderObservable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author rwa
 *
 */
public abstract class FrameGrabberBase implements Effect, ReaderObservable{	
	static Logger logger = LoggerFactory.getLogger(FrameGrabberBase.class);		
	
	
	Format[] inputFormats;
	Format[] outputFormats;
	Format inputFormat;
	Format outputFormat;
	
	List<ReaderListener> readerListeners;
	
	public FrameGrabberBase(){
		inputFormats = new Format[]{new RGBFormat(null,Format.NOT_SPECIFIED,Format.intArray,-1,32,0x00FF0000, 0x0000FF00, 0x000000FF)};		
		outputFormats = new Format[]{new RGBFormat(null,Format.NOT_SPECIFIED,Format.intArray,-1,32,0x00FF0000, 0x0000FF00, 0x000000FF)};
		readerListeners = new ArrayList<ReaderListener>();			
	}
	
	public void close() {
	}
	
	public abstract String getName();


	public void open() throws ResourceUnavailableException {
		logger.debug(Thread.currentThread() + " started watchdog.");		
	}

	public void reset() {
	}

	public Format[] getSupportedInputFormats() {
		return inputFormats;
	}

	public Format[] getSupportedOutputFormats(Format arg0) {
		return outputFormats;
	}

	public int process(Buffer inBuffer, Buffer outBuffer) {
		VideoDeviceWatchdog.getInstance().reset();
		Dimension sizeIn = ((RGBFormat)inBuffer.getFormat()).getSize();
		BufferedImage bimage = new BufferedImage(sizeIn.width,sizeIn.height,BufferedImage.TYPE_INT_RGB);
		bimage.setRGB(0,0,sizeIn.width,sizeIn.height,(int[])inBuffer.getData(),0,sizeIn.width);
			process(bimage);		
		bimage.getRGB(0,0,sizeIn.width,sizeIn.height,(int[])inBuffer.getData(),0,sizeIn.width);	
		outBuffer.copy(inBuffer);
		return BUFFER_PROCESSED_OK; 
	}
	
	
	public abstract int process(BufferedImage aBufferedImage); 
	
	public void addReaderListener(ReaderListener aListener){
		synchronized(readerListeners){
			readerListeners.add(aListener);
		}
	}
	
	public void removeReaderListener(ReaderListener aListener){
		synchronized(readerListeners){
			readerListeners.remove(aListener);
		}
	}
	
	protected void notifyFrameListeners(Object anObject){		
		synchronized(readerListeners){
			for(ReaderListener f: readerListeners){
				f.onFrameEvent(anObject);
			}					
		}
	}
	
	public Format setInputFormat(Format anInputFormat) {
		inputFormat = anInputFormat;
		return anInputFormat;		
	}

	public Format setOutputFormat(Format anOutputFormat) {
		outputFormat = anOutputFormat;
		return anOutputFormat;
	}

	public Object getControl(String arg0) {
		return null;
	}

	public Object[] getControls() {
		return new Control[0];
	}		
}
