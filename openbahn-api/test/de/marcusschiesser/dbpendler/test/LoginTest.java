package de.marcusschiesser.dbpendler.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import de.marcusschiesser.dbpendler.server.bahnwrapper.Login;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;

public class LoginTest {

	public static final String TEST_USER_PIN = "x";
	public static final String TEST_USER_LOGIN = "x";
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testLoginOk() throws IOException, SAXException {
		HTTPSession session = Login.getInstance().login(TEST_USER_LOGIN, TEST_USER_PIN);
		Assert.assertNotNull("Login with test user failed.", session);
	}
	
	@Test
	public void testLoginFails() {
		boolean loginFails = false;
		try{
			Login.getInstance().login("abcdef", "12345");
		}catch(Exception e) {
			loginFails = true;
		}
		Assert.assertTrue("Login with 'impossible' user (abcdef,12345) somehow succeded.", loginFails);
	}
		
}
