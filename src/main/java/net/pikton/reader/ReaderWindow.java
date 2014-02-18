package net.pikton.reader;


import net.pikton.reader.config.ReaderConfiguration;
import net.pikton.reader.impl.ReaderEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ReaderWindow extends JFrame{

	private static final long serialVersionUID = -1241359755135112050L;

	static Logger logger = LoggerFactory.getLogger(ReaderWindow.class);

	ReaderEngineImpl reader;
	
	Component readerComponent;
	
	public ReaderWindow(ReaderEngineImpl aReader) throws ReaderEngineException{	
		Dimension dim = ReaderConfiguration.getInstance().getWindowDimension();
		reader = aReader;		
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {				
				try {
					reader.disconnect();
				}catch (ReaderEngineException e) {
				}finally{
					System.exit(0);
				}
			}
		});	
		
		BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/pikton-icon.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
		this.setIconImage(image);				
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		this.setMinimumSize(dim);		
		this.setResizable(false);
		this.setTitle("Pikton Reader");						
	}
	
	public void showReaderWindow(){
		if (readerComponent != null){
			this.remove(readerComponent);
		}
		readerComponent = reader.getVisualComponent();
		
		this.add("Center",readerComponent);	
		this.setVisible(true);			
	}
	
	public void hideReaderWindow(){
		this.setVisible(false);			
	}	
	
}
