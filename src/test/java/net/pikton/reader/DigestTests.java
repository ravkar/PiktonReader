package net.pikton.reader;


import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DigestTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCreateDigest() throws Exception{
		LicenseSigner signer = LicenseSigner.getInstance();
		License license = new License("Rafal Warno - Pikton", new Date());
		license = signer.signLicense(license);
		Registration rm = new Registration();
		Assert.assertTrue(rm.store(license));
	}
	
	@Test
	public void testVerifyDigest() throws Exception{
		Registration rm = new Registration();
		Assert.assertTrue(rm.verify());
	}	
		
}
