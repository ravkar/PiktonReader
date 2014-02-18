package net.pikton.reader.config;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import net.pikton.reader.ReaderEngineException;


public class ReaderConfiguration {

	private static final long serialVersionUID = -8877226667474521806L;
	
	private static ReaderConfiguration m_instance; 	
	          		
	public static final String READER_HOME_ENV = "reader.home";
		
	public static final String READER_LOG_ENV = "reader.log";	
	
	public static final String READER_LIB_ENV = "java.library.path";
		
	public static final String PARAM_READER_PORT = "reader.output.port";
	
	public static final String PARAM_READER_JSON_CHECK = "reader.output.json.check";	
	
	public static final String PARAM_READER_LEGACY_BARCODE_ENABLED = "reader.output.legacy_barcode.enabled";	
	
	public static final String PARAM_READER_BEEP_ENABLED = "reader.output.beep.enabled";
	
	public static final String READER_BEEP_RESOURCE_NAME = "/beep.wav";
			
	public static final String PARAM_VIDEO_FORMAT_DIM = "video.dim";		
	
	public static final String PARAM_WINDOW_DIM = "window.dim";			

	public static final int DEFAULT_READER_PORT = 5678;		
	
	public static final boolean DEFAULT_READER_JSON_VERIFY = true;			
	
	public static final Dimension DEFAULT_VIDEO_FORMAT_DIMENSION = new Dimension(320, 240);	
	
	public static final Dimension DEFAULT_WINDOW_DIMENSION = new Dimension(320, 240);			
    
    public static final String DEFAULT_MEDIA_LOCATOR = "vfw://0";
		
	
	Properties properties;	
	int mReaderPort;	
	boolean mReaderJsonCheck;
	boolean mReaderBeepEnabled;	
	boolean mReaderLegacyBarcodeEnabled;	
	Dimension mVideoFormatDimension;	
    Dimension mWindowDimension;
	
	        
    public static ReaderConfiguration getInstance() throws ReaderEngineException{
    	if (m_instance == null){
    		m_instance = new ReaderConfiguration();
    	}
    	return m_instance;    	
    }
    
    private ReaderConfiguration() throws ReaderEngineException{
		initialize();
    }
    
    private void initialize() throws ReaderEngineException{
    	load();
    	mReaderPort = parseIntParameter(PARAM_READER_PORT, DEFAULT_READER_PORT);
    	mReaderJsonCheck = parseBooleanParameter(PARAM_READER_JSON_CHECK, DEFAULT_READER_JSON_VERIFY);
    	mReaderLegacyBarcodeEnabled = parseBooleanParameter(PARAM_READER_LEGACY_BARCODE_ENABLED, false);    
    	mReaderBeepEnabled = parseBooleanParameter(PARAM_READER_BEEP_ENABLED, true);     	
    	
    	
    	mVideoFormatDimension = parseDimensionParameter(PARAM_VIDEO_FORMAT_DIM, DEFAULT_VIDEO_FORMAT_DIMENSION);
    	mWindowDimension = parseDimensionParameter(PARAM_WINDOW_DIM, DEFAULT_WINDOW_DIMENSION);
    	m_instance = this;
    }
    			
	private void load() throws ReaderEngineException{
		try {
			File confFile = getReaderConfigFile();
			InputStream fis = new FileInputStream(confFile);			
			properties = new Properties();
			properties.load(fis);
		} catch (Exception e) {
			throw new ReaderEngineException(e); 
		}	
	}
		
	public File getReaderConfigFile() {
		String homeDir = System.getProperty(READER_HOME_ENV);
		String confFileName =  homeDir + File.separator + "cfg" + File.separator + "reader.properties";
		return new File(confFileName);		
	}
			
	public int parseIntParameter(String aParamName, int aDefaultValue){
		int ivalue = aDefaultValue;
		String sport = properties.getProperty(aParamName, String.valueOf(aDefaultValue)).trim();
		try {
			ivalue = Integer.parseInt(sport);
		} catch (NumberFormatException e) {			
			return aDefaultValue;
		}
		return ivalue;
	}
	
	public boolean parseBooleanParameter(String aParamName, boolean aDefaultValue){
		boolean bvalue = aDefaultValue;
		String s = properties.getProperty(aParamName, String.valueOf(aDefaultValue)).trim();
		bvalue = Boolean.parseBoolean(s);
		return bvalue;
	}		
	
	public Dimension parseDimensionParameter(String aParamName, Dimension aDefaultValue){
		Dimension dimValue = aDefaultValue;
		String sdim = properties.getProperty(aParamName, String.valueOf(aDefaultValue)).trim();
		try {
			String[] arr = sdim.split(",");
			int width = Integer.parseInt(arr[0].trim());
			int heigh = Integer.parseInt(arr[1].trim());
			dimValue = new Dimension(width, heigh);
		} catch (NumberFormatException e) {			
			return aDefaultValue;
		}
		return dimValue;
	}		
	
	public int getReaderPort(){
		return mReaderPort;
	}
	
	public boolean isJsonCheckingEnabled(){
		return mReaderJsonCheck;
	}	
	
	public boolean isBeepEnabled(){
		return mReaderBeepEnabled;
	}
	
	public boolean isLegacyBarcodeEnabled(){
		return mReaderLegacyBarcodeEnabled;
	}	
		
	public Dimension getVideoFormatDimension(){
		return mVideoFormatDimension;
	}	
	
	public Dimension getWindowDimension(){
		return mWindowDimension;
	}		
}
