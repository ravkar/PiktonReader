package net.pikton.reader.impl.jmf.plugins.autoid;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import net.pikton.reader.Utils;
import net.pikton.reader.config.ReaderConfiguration;
import net.pikton.reader.impl.jmf.plugins.FrameDecoderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Vector;


public class FrameDecoderZxing extends FrameDecoderBase {

	static Logger logger = LoggerFactory.getLogger(FrameDecoderZxing.class);

	volatile boolean recognized;

	boolean outputJsonChecking;
	boolean beepEnabled;

	Object lockMonitor;
	RecognitionMarker marker;
	Hashtable<DecodeHintType, Object> hints;

	Font warnFont = new Font("Courier", Font.BOLD, 20);
	Font infoFont = new Font("Courier", Font.PLAIN, 16);

	public FrameDecoderZxing(ReaderConfiguration aConfiguration) {
		outputJsonChecking = aConfiguration.isJsonCheckingEnabled();
		beepEnabled = aConfiguration.isBeepEnabled();
		lockMonitor = new Object();
		marker = new RecognitionMarker();
		hints = new Hashtable<DecodeHintType, Object>(3);
		Vector<BarcodeFormat> barcodeFormats = new Vector<BarcodeFormat>(8);
		barcodeFormats.addElement(BarcodeFormat.QR_CODE);
		barcodeFormats.addElement(BarcodeFormat.DATA_MATRIX);
		if (aConfiguration.isLegacyBarcodeEnabled()) {
			barcodeFormats.addElement(BarcodeFormat.UPC_A);
			barcodeFormats.addElement(BarcodeFormat.UPC_E);
			barcodeFormats.addElement(BarcodeFormat.EAN_13);
			barcodeFormats.addElement(BarcodeFormat.EAN_8);
		}
		hints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);
	}

	@Override
	public String getName() {
		return "Frame Decoder";
	}

	@Override
	public int process(BufferedImage aBufferedImage) {
		LuminanceSource source = new BufferedImageLuminanceSource(
				aBufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result = null;
		String txt = null;
		Graphics g = aBufferedImage.getGraphics();
		setFont(g);
		try {
			// try to recognize
			if (!recognized) {
				try {
					result = new MultiFormatReader().decode(bitmap, hints);

					txt = result.getText();

					// option JSON checking
					if (outputJsonChecking && !Utils.isValidJson(txt)) {
						return 0;
					}
					logger.info("decoded pikton->" + result.getText());
					logger.info("msg lenght->" + result.getText().length());

					notifyFrameListeners(txt);
					marker.raise();
				} catch (ReaderException re) {
					// not recognized
				}
				// quiet period
			} else if (recognized) {				
				g.fillRect(20, 20, 30, 30);
			}
		} finally {
			setFont(g);			
			drawMessages(g, aBufferedImage.getWidth(), aBufferedImage.getHeight());
		}
		return 0;
	}

	void setFont(Graphics g) {
			g.setFont(infoFont);
			g.setColor(Color.RED);
	}

	void drawMessages(Graphics g, int width, int height) {
//			g.drawString(INFO, 5, height - 10);
	}

	class RecognitionMarker implements Runnable {
		volatile int numOfThreads;

		public void raise() {
			synchronized (lockMonitor) {
				recognized = true;
				new Thread(this).start();
			}
		}

		public void run() {
			numOfThreads++;
			logger.debug("numOfThreadrs->" + numOfThreads);
			synchronized (lockMonitor) {
				if (beepEnabled) {
					Utils.playBeep();
				}
				try {
					lockMonitor.wait(1000);
				} catch (InterruptedException e) {
					// interrupted
				}
				recognized = false;
			}
			numOfThreads--;
		}

		public int getActiveCount() {
			return numOfThreads;
		}

	};

}
