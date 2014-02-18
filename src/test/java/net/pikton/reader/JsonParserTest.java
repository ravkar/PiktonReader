package net.pikton.reader;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void parseStringToJson(){
		String s = "{ala:87654422}";
		try {
			JSONObject jso = new JSONObject(s);
			Assert.assertTrue(true);			
		} catch (JSONException e) {
			Assert.assertTrue(false);
		}
		
	}
}
