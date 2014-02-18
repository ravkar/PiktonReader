package net.pikton.reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * 
 * @author rwarno
 *
 */
public class License implements Serializable, Cloneable{
	
	private static final long serialVersionUID = -9060839033572953674L;
	
	private String message;
		
	private Date validity;
	
	private byte[] signature;
		
	public License(){		
	}
	
	
	public License(String aLicensedTo, Date aValidity){
		this.message = aLicensedTo;
		this.validity = aValidity;	
	}
	
	public License(String aLicensedTo, Date aValidity, byte[] aLicenseSignature){
		this.message = aLicensedTo;
		this.validity = aValidity;		
		this.signature = new byte[aLicenseSignature.length];		
		System.arraycopy(aLicenseSignature, 0, signature, 0, aLicenseSignature.length);		
	}	
				
	public static License getInstance(String aBinaryLicenseObject) throws LicenseException{
		try {		
			String s = aBinaryLicenseObject.replaceAll("\\n", "");
			BASE64Decoder dec = new BASE64Decoder();			
			byte[] bytes = dec.decodeBuffer(aBinaryLicenseObject);
			return getInstance(bytes);
		} catch (Exception e) {
			throw new LicenseException("Not valid license!");
		}
	}
	
	public static License getInstance(byte[] aBinaryLicenseObject) throws LicenseException{
		try {		
			byte[] bytes = aBinaryLicenseObject;
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream dis = new ObjectInputStream(bis);
			return (License)dis.readObject();
		} catch (Exception e) {
			throw new LicenseException("Not valid license!");
		}
	}	
			
//	public License(byte[] aBinaryLicenseObject) throws Exception{
//	ByteArrayInputStream bis = new ByteArrayInputStream(aBinaryLicenseObject);
//	DataInputStream dis = new DataInputStream(bis);
//	licensedTo = dis.readUTF();
//	int sigLen = dis.readInt(); 
//	licenseSignature = new byte[sigLen];
//	dis.read(licenseSignature);		
//	long uid = dis.readLong();
//	if (uid != getSerialVersionUID()){
//		throw new IOException("License version invalid!");
//	}
//}	
	
	public byte[] getBinaryContent() throws Exception{
		License unsigned = new License(this.message, this.validity);
		return unsigned.getSerialized();
	}
	
	public  String getSerializedString() throws IOException{
		BASE64Encoder enc = new BASE64Encoder();
		return enc.encode(getSerialized());
	}	
	
	public  byte[] getSerialized() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(bos);
		dos.writeObject(this);
		return bos.toByteArray();
	}		
		
	public String getMessage() {
		return message;
	}
		
	public Date getValidity() {
		return validity;
	}

	public byte[] getSignature() {
		return signature;
	}
	
	public void setSignature(byte[] aLicenseSignature) {
		if (aLicenseSignature == null){
			this.signature = null;
			return;
		}			
		this.signature = new byte[aLicenseSignature.length];		
		System.arraycopy(aLicenseSignature, 0, signature, 0, aLicenseSignature.length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
		result = prime * result
				+ ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		License other = (License) obj;
		if (!Arrays.equals(signature, other.signature))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
		
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "License [message=" + message + ", validity="+ validity +", signature="
				+ Arrays.toString(signature) + "]";
	}			
}
