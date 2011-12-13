package de.marcusschiesser.dbpendler.test;

import java.io.IOException;
import java.io.InputStream;

import org.htmlparser.sax.XMLReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.CommitValidationHandler;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPUtils;

public class CommitValidationHandlerTest {

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
	public void testValidation() throws IOException, SAXException {
		String testData = "<h1>Best&auml;tigung</h1>\n" + 
		"<div class=\"haupt\">\n" +
		"<div class=\"bold\">Auftrags-Nr.: ZDZLUE</div>\n\n" +
		"<div>Karlsruhe Hbf - Frankfurt(Main)Hbf<br /><span class=\"bold\">am 26.05.2011, ICE   78, ab 08:51</span><br /><span class=\"bold\">Wg. 4, Pl. 113, 1 Mitte, Abteil, 2. Kl.</span></div>" +
		"Gesamtpreis: 2,50 EUR" +
		"</div>";
		XMLReader reader = new org.htmlparser.sax.XMLReader();
		InputStream inputStream = HTTPUtils.stringToStream(testData);
		CommitValidationHandler commitHandler = new CommitValidationHandler();
		reader.setContentHandler(commitHandler);
		reader.parse(new InputSource(inputStream));
		Assert.assertEquals("ZDZLUE", commitHandler.getCommitData().getOrderNumber());
		Assert.assertEquals("Karlsruhe Hbf", commitHandler.getCommitData().getStart().getValue());
		Assert.assertEquals("Frankfurt(Main)Hbf", commitHandler.getCommitData().getDestination().getValue());
		Assert.assertEquals("26.05.2011", DateUtils.getDateFormat().format(commitHandler.getCommitData().getDate()));
		Assert.assertEquals("08:51", DateUtils.getTimeFormat().format(commitHandler.getCommitData().getTime()));
		Assert.assertEquals("Wg. 4, Pl. 113, 1 Mitte, Abteil, 2. Kl.", commitHandler.getCommitData().getReservation());
	}
	
}
