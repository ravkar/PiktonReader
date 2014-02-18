package net.pikton.reader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Registration {

	static Logger logger = LoggerFactory.getLogger(Registration.class);
		
	private String msgKey = "Q09NUFVURVJOQU1FOlBST0NFU1NPUl9SRVZJU0lPTjpVU0VSRE9NQUlO";
	
	private String path = "U3lzdGVtUm9vdA==", registry = "cGlrdG9u";
		
	private boolean state;
	
	public Registration(){
		state = verify();
	}	
	
	public boolean isRegistered() {
		return state;
	}

	public boolean register(License aLicense){
		try {
			state = store(aLicense);
			return state;
		} catch (ReaderEngineException e) {
			logger.debug("Problem during registration of valid license.");
			state = false;
			return state;
		}
	}
		
	boolean store(License aLicense) throws ReaderEngineException{		
		try {
			File f = getPath();
			DataOutputStream bos = new DataOutputStream(new FileOutputStream(f));
			BASE64Encoder enc = new BASE64Encoder();
			bos.writeUTF(enc.encode(calculateDigest()));
//			bos.writeInt(aLicense.getSerialized().length);
			bos.writeUTF(aLicense.getSerializedString());
			bos.close();
			return true;
		} catch (Exception e) {
			throw new ReaderEngineException(e);
		}
	}	
	
	File getPath() throws ReaderEngineException{
		try {
			BASE64Decoder dec = new BASE64Decoder();
			byte[] bpath = dec.decodeBuffer(path), breg = dec.decodeBuffer(registry);		
			String kpath = new String(bpath), kreg = new String(breg);		
			Map envs = System.getenv();
			String vpath = (String)envs.get(kpath);		
			File f = new File(vpath, kreg); 		
			return f;
		} catch (Exception e) {
			throw new ReaderEngineException(e);
		}
	}
	

		
	boolean verify(){						
		FileInputStream fis = null;
		DataInputStream bis = null;
		try {	
			File f = getPath();		
			if (!f.exists()){
				logger.debug("Not registrated.");
				return false;
			}					
			fis = new FileInputStream(f);
			bis = new DataInputStream(fis);		
//			byte[] digest = new byte[20];		
			String digestStr = bis.readUTF();
			BASE64Decoder dec = new BASE64Decoder();
			byte[] digest = dec.decodeBuffer(digestStr);
			String obj = bis.readUTF();
			License lic = License.getInstance(obj);		
			if (Arrays.equals(digest, calculateDigest()) && LicenseVerifier.getInstance().verifyLicense(lic) && lic.getValidity().before(new Date())){
				return true;
			}							
			return false;
		} catch (Exception e) {
			logger.debug("Registration problem.");
			return false;
		}finally{
			try {
				if (bis != null) bis.close();
			} catch (Exception e) {			
			}
		}
	}
	
	byte[] calculateDigest() throws ReaderEngineException{
		try {
			BASE64Decoder dec = new BASE64Decoder();
			byte[] bytes = dec.decodeBuffer(msgKey);
			String decoded = new String(bytes);
			String[] arr = decoded.split(":");				
			Map envs = System.getenv();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arr.length; i++){
				sb.append(envs.get(arr[i]));
			}
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(new String(sb).getBytes());
			byte[] digest = md.digest();		
			return digest;
		} catch (Exception e) {		
			throw new ReaderEngineException("");
		}
	}	
}
