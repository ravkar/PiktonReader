package net.pikton.reader;

import net.pikton.reader.config.ReaderConfiguration;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;


public class ApplicationBase {
    static {
        String hdir = System.getProperty(ReaderConfiguration.READER_HOME_ENV);
        hdir = (hdir!=null)?hdir:".";
        
        System.setProperty(ReaderConfiguration.READER_LOG_ENV, hdir + File.separator + "log" );
        System.setProperty(ReaderConfiguration.READER_LIB_ENV, hdir + File.separator + "lib" );        
        
        File lfile = new File(hdir + File.separator + "cfg", "log4j.xml");
        if (!lfile.exists() || lfile.isDirectory()){
        	lfile = new File("log4j.xml");     
        }    
        
        // Load log4j configuration.
        try {
            DOMConfigurator.configure(lfile.getAbsolutePath());
        } catch (Exception ex) {
//            System.err.println("WARN: log4j.xml initialization from file " + lfile.getAbsolutePath() + " failed");
        }               
    }
}
