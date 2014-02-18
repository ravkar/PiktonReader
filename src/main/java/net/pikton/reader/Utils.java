package net.pikton.reader;

import net.pikton.reader.config.ReaderConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

	static Logger logger = LoggerFactory.getLogger(ApplicationMain.class);	
	
	public static boolean isValidJson(String aJsonString) {
		try {
			JSONObject jso = new JSONObject(aJsonString);
			return true;
		} catch (JSONException e) {
			return false;
		}
	}

	public static void playBeep(){
		InputStream fileStream = null;
		AudioInputStream audioInputStream = null; 				
		try {
			fileStream = Utils.class.getResourceAsStream(ReaderConfiguration.READER_BEEP_RESOURCE_NAME );		
			audioInputStream = AudioSystem.getAudioInputStream( fileStream );
			playAudioStream(audioInputStream);
		} catch (Exception e) {
			logger.error("Exception during beep.", e);
		} finally{
			try {
				audioInputStream.close();
			} catch (IOException e) {
			}
			
			try {
				fileStream.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void playAudioStream(AudioInputStream audioInputStream) {
		AudioFormat audioFormat = audioInputStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		if (!AudioSystem.isLineSupported(info)) {

			return;
		}
		try {
			SourceDataLine dataLine = (SourceDataLine) AudioSystem
					.getLine(info);
			dataLine.open(audioFormat);
			dataLine.start();
			int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
			byte[] buffer = new byte[bufferSize];
			try {
				int bytesRead = 0;
				while (bytesRead >= 0) {
					bytesRead = audioInputStream.read(buffer, 0, buffer.length);
					if (bytesRead >= 0) {
						int framesWritten = dataLine.write(buffer, 0, bytesRead);
					}
				}
			} catch (IOException e) {
				logger.error("Sound resource reading issue.",e);
			}
			dataLine.drain();
			dataLine.close();
		} catch (LineUnavailableException e) {
			logger.error("Sound resource reading issue.",e);
		}
	}
	
	public static void doInit(){
		
	}
}
