package net.pikton.reader;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
 
public class Play {
   /** Plays audio from given file names. */
   public static void main( String [] args ) throws Exception {
	   InputStream fileStream = Play.class.getResourceAsStream( "/beep.wav" );
	   
//	   InputStream in = getClass().getResourceAsStream("/sound.mid");	   
	   
	   AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( fileStream );
       playAudioStream(audioInputStream);
   
      // Must exit explicitly since audio creates non-daemon threads.
      System.exit( 0 );
   } // main
 
   public static void playAudioFile( String fileName ) {
      File soundFile = new File( fileName );
 
      try {
         // Create a stream from the given file.
         // Throws IOException or UnsupportedAudioFileException
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( soundFile );
         // AudioSystem.getAudioInputStream( inputStream ); // alternate audio stream from inputstream
         playAudioStream( audioInputStream );
      } catch ( Exception e ) {
         System.out.println( "Problem with file " + fileName + ":" );
         e.printStackTrace();
      }
   } // playAudioFile

   
   public static void playAudioStream( AudioInputStream audioInputStream ) {
      AudioFormat audioFormat = audioInputStream.getFormat();
      DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );
      if ( !AudioSystem.isLineSupported( info ) ) {
         
         return;
      }
      try {
         SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine( info );
         dataLine.open( audioFormat );
         dataLine.start();
         int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
         byte [] buffer = new byte[ bufferSize ];
         try {
            int bytesRead = 0;
            while ( bytesRead >= 0 ) {
               bytesRead = audioInputStream.read( buffer, 0, buffer.length );
               if ( bytesRead >= 0 ) {
                  int framesWritten = dataLine.write( buffer, 0, bytesRead );
               }
            } 
         } catch ( IOException e ) {
            e.printStackTrace();
         }
         dataLine.drain();
         dataLine.close();
      } catch ( LineUnavailableException e ) {
         e.printStackTrace();
      }
   } 
} 
