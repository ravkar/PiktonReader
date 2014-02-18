package net.pikton.reader;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseVerifier {
	
	final static Logger logger = LoggerFactory.getLogger(LicenseVerifier.class);	
	
	private static final String LICENSE_CERT_STORE = "/net/pikton/reader/license";
	
	private static LicenseVerifier instance;
		
	private Signature dsa;
	
	public static LicenseVerifier getInstance() throws LicenseException{
		if (instance == null){
			instance = new LicenseVerifier();
		}
		return instance; 
	}
	
	public LicenseVerifier() throws LicenseException{
		dsa = getDSA();
	}
		
	public boolean verifyLicense(License aSignedLicense) throws LicenseException{
		boolean sigVerifyResult = false;
		try {
			dsa.update(aSignedLicense.getBinaryContent());	
			sigVerifyResult = dsa.verify(aSignedLicense.getSignature());
			return sigVerifyResult;		
		} catch (SignatureException e) {
			logger.error("",e);
			throw new LicenseException("License verification failure.");			
		} catch (Exception e){
			logger.error("",e);
			throw new LicenseException("License verification failure.");	
		}
	}	
	
	private  Signature getDSA() throws LicenseException{
		try {
			InputStream fileStream = null;		
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());	
			fileStream = Registration.class.getResourceAsStream(LICENSE_CERT_STORE);		
			long uid = Math.abs(License.getSerialVersionUID());						
			keyStore.load(fileStream, String.valueOf(uid).toCharArray());
			Certificate cert = keyStore.getCertificate("piktonlicense");
			Signature dsa = Signature.getInstance("SHA1withDSA");	
			dsa.initVerify(cert);			
			return dsa;
		} catch (Exception e) {
			throw new LicenseException("No valid license!");
		}					
	}	
}
