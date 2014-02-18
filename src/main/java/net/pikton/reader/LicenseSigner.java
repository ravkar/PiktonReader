package net.pikton.reader;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseSigner {
	
	final static Logger logger = LoggerFactory.getLogger(LicenseSigner.class);	
	
	private static final String LICENSE_KEY_STORE = "/net/pikton/reader/licenseKey";
	
	private static LicenseSigner instance;
	
	private static final String passwd = "pikton2011";
	
	private Signature dsa;
	
	public static LicenseSigner getInstance() throws LicenseException{
		if (instance == null){
			instance = new LicenseSigner();
		}
		return instance; 
	}
	
	public LicenseSigner() throws LicenseException{
		dsa = getDSA();
	}
	
	public License signLicense(License anUnsignedLicense) throws LicenseException{
		License signedLicense = null; 
		try {
			dsa.update(anUnsignedLicense.getBinaryContent());
			byte[] signature = dsa.sign();
			signedLicense = new License(anUnsignedLicense.getMessage(), anUnsignedLicense.getValidity(), signature);			
			return signedLicense;			
		} catch (SignatureException e) {
			logger.error("",e);
			throw new LicenseException("License signature failure.");			
		} catch (Exception e){
			logger.error("",e);			
			throw new LicenseException("License signature failure.");	
		}
	}	
	
	private  Signature getDSA() throws LicenseException{
		try {
			InputStream fileStream = null;		
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());	
			fileStream = Registration.class.getResourceAsStream(LICENSE_KEY_STORE);		
			keyStore.load(fileStream, passwd.toCharArray());
			Key key = keyStore.getKey("piktonlicense", passwd.toCharArray());
			Signature dsa = Signature.getInstance("SHA1withDSA");	
			dsa.initSign((PrivateKey)key);
			return dsa;						
		} catch (Exception e) {
			throw new LicenseException("No valid license!");
		}			
	}		
}
