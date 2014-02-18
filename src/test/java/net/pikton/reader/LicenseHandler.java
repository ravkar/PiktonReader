package net.pikton.reader;

import java.io.File;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class LicenseHandler extends TestCase{

	final String KEY1 = "COMPUTERNAME";
	final String KEY2 = "SystemRoot";	
	final String KEY3 = "PROCESSOR_REVISION";
	
	private String msgKey = "Q09NUFVURVJOQU1FOlBST0NFU1NPUl9SRVZJU0lPTjpVU0VSRE9NQUlO"; 	
	
	
	final static Logger logger = LoggerFactory.getLogger(Registration.class);
	
	public static void main(String[] args) {
		Map envs = System.getenv();
		System.out.println(envs);
		String osDir = (String)envs.get("SystemRoot");
		File file = new File(osDir,"temp"); 		
		File file2 = new File(file,"kuku.log"); 
		
	}
    
	public void testMsg() throws Exception{
			BASE64Decoder dec = new BASE64Decoder();
			byte[] bytes = dec.decodeBuffer(msgKey);
			String decoded = new String(bytes);
			System.out.println(decoded);
			String[] arr = decoded.split(":");
			System.out.println("len->" + arr.length);			
			Map envs = System.getenv();
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for (i = 0; i < arr.length; i++){
				sb.append((String)envs.get(arr[i]));
			}				

			System.out.println("secret msg->" + new String (sb));
			
	}
	
	
	
	public void testEncode64() throws Exception{
		StringBuffer sb = new StringBuffer();
		
		String s0 = "COMPUTERNAME", s1 = "PROCESSOR_REVISION", s2 = "USERDOMAIN";
		String s = s0 +':' + s1 + ':' + s2;
		String path = "SystemRoot", registryFile = "pikton";
		
		BASE64Encoder enc = new BASE64Encoder();
		sb.append(enc.encode(s0.getBytes()));
		sb.append(enc.encode(s1.getBytes()));
		sb.append(enc.encode(s2.getBytes()));
		
		String encoded = enc.encode(s.getBytes());
		System.out.println(s + "encoded string sum ->" + enc.encode(s.getBytes()));		
		System.out.println("encoded string->" + new String(sb));
		System.out.println("encoded path->" + enc.encode(path.getBytes()));
		System.out.println("encoded registry->" + enc.encode(registryFile.getBytes()));
		
		
		BASE64Decoder dec = new BASE64Decoder();
		byte[] bytes = dec.decodeBuffer(encoded);
		String decoded = new String(bytes);
		String[] arr = decoded.split(":");
		
		System.out.println("decoded string->" + new String(bytes));		
	}
	
	public void testKeyPairGen() throws Exception{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		System.out.println("priv key lenght->" + pair.getPrivate());
		
		////DSA
		Signature dsa = Signature.getInstance("SHA1withDSA");	
		//sign
		dsa.initSign(pair.getPrivate());
		byte[] data = "Piktonie nie daj sie!".getBytes();
		dsa.update(data);
		byte[] sig = dsa.sign();
		System.out.println("sig->" + new String(sig).length());
		//verify
	    dsa.initVerify(pair.getPublic());
	    dsa.update(data);
	    boolean sigVerifyResult = dsa.verify(sig);
	    System.out.println("sigVerifyResult->" + sigVerifyResult);	     
	}
	
	public void testLicenseSignerVerifier() throws Exception{
		LicenseSigner signer = LicenseSigner.getInstance();
		License license = new License("Rafal Warno - Pikton", new Date());
		license = signer.signLicense(license);
				
		QRCode result = new QRCode();
		Encoder.encode(license.getSerializedString(), ErrorCorrectionLevel.L, result);		
		MatrixToImageWriter.writeToFile(result.getMatrix(), "png", new File("pikton_trial_license.jpg"),3);
				
		LicenseVerifier verifier = LicenseVerifier.getInstance();
		String s = license.getSerializedString();//"rO0ABXNyABluZXQucGlrdG9uLnJlYWRlci5MaWNlbnNlgkFu58Tj8bYCAAJMAAdtZXNzYWdldAASTGphdmEvbGFuZy9TdHJpbmc7WwAJc2lnbmF0dXJldAACW0J4cHQAFFBpa3RvbiBSJkQgZGl2aXNpb24udXIAAltCrPMX+AYIVOACAAB4cAAAAC4wLAIUIWWrlDop4eNpZWsyAIgPXtF/WdwCFDrLQLmybeGYwz7FY7OVDkQzzZ95";
		logger.debug(s);
		License deserializedLicense = License.getInstance(s);
		assertTrue(verifier.verifyLicense(deserializedLicense));
		logger.debug(deserializedLicense.toString());		
	}
		
	
	public void testSerializeLicense() throws Exception{
		License license = new License("Intersport Sp. Z.o.o.", new Date(), new byte[]{12,34,1,77,32,32,12,90,4,1,3,4});
		String serialized = license.getSerializedString();
		System.out.println("serialized license->" + serialized.length());
		System.out.println("serialized license->" + new String(serialized));
		
		
		License licenseSerialized = License.getInstance(serialized);
		System.out.println("read from serialized->" + licenseSerialized);
		
		assertTrue(licenseSerialized.equals(license));		
	}	
	

	
	public void testKeystoreUsage() throws Exception{
		InputStream fileStream = null;		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		char[] pass = new char[]{'p','i','k','t','o','n','2','0','1','1'};
		String s = "jahs3123wdqwdqwefkjdhpikton1827930HKKIUIOJLKllklllLLKLl76887nnv120078gsvbj51hjnbcvcz32epikton2011wefjhwkjeqfkwq19802730891ehjl`e;nl12h3nl;13gfdgfsk";
		System.out.println("start idx->" + s.indexOf("pikton2011"));
		fileStream = Registration.class.getResourceAsStream("net.pikton.reader.license");				
//		FileInputStream fis = new FileInputStream(new File("piktonLicense.keystore"));
		keyStore.load(fileStream, pass);
		
		System.out.println("aliases->" + keyStore.aliases());		
	}
	
	
	public void testPiktonEncoder() throws Exception{
		
		String msg = "LN=Warno;FN=Rafal;ID=DB522181;TICKET=987006560255";
		System.out.println("msg lenght->" + msg.length());
		QRCode result = new QRCode();
		Encoder.encode(msg, ErrorCorrectionLevel.L, result);		
		System.out.println("result->" + result);
		MatrixToImageWriter.writeToFile(result.getMatrix(), "png", new File("gen_ticket.jpg"));
	}
	
	public void testLicenseFile() throws Exception{
		Map envs = System.getenv();
		System.out.println(envs);
		String osDir = (String)envs.get("SystemRoot");;
		
		
		File file = new File(osDir,"temp"); 		
		File file2 = new File(file,"kuku.log"); 
		if (!file2.exists()){
			file2.createNewFile();
		}		
	}
	
	public void testDigest() throws Exception{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update("ala ma asq".getBytes());
		byte[] digest = md.digest();
		System.out.println("digest len->" + digest.length);
		System.out.println("digest->" + new String(digest));
	}
	
	public void testCipherAES(){
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            Key key = kg.generateKey();
            
            System.out.println("key->" + key.getEncoded().length);
            
            Cipher cipher = Cipher.getInstance("AES");
 
            byte[] data = "Hello World!".getBytes();
            System.out.println("Original data : " + new String(data));
 
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(data);
            System.out.println("Encrypted data: " + new String(result));
 
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] original = cipher.doFinal(result);
            System.out.println("Decrypted data: " + new String(original));
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }		
	}		
	
}
