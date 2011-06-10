package de.marcusschiesser.dbpendler.server.bahnwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.htmlparser.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.LoginValidationHandler;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.SessionExtractionHandler;
import de.marcusschiesser.dbpendler.server.utils.ExceptionUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;
import de.marcusschiesser.dbpendler.server.utils.HTTPUtils;

public class Login {
	private static Login instance = new Login();

	private final Logger log = Logger.getLogger(Login.class.getName());

	private URL startURL;
	private URL loginURL;
	private URL logoutURL;
	private URL loginAnonymousURL;

	private XMLReader reader;

	public Login() {
		reader = new org.htmlparser.sax.XMLReader();
		try {
			loginAnonymousURL = new URL(
			"http://mobile.bahn.de/bin/mobil/query2.exe/dox?rt=1&use_realtime_filter=1");
			startURL = new URL("https://fahrkarten.bahn.de/mobile/st/st.post?lang=de");
			loginURL = new URL("https://fahrkarten.bahn.de/mobile/st/li.post?lang=de");
			logoutURL = new URL("https://fahrkarten.bahn.de/mobile/st/lo.post?lang=de");
		} catch (MalformedURLException e) {
			log.severe("error parsing URL: " + e.toString());
		}
	}
	
	public static Login getInstance() {
		return instance ;
	}
	// curl -c mycookie -k -i
	// "https://fahrkarten.bahn.de/mobile/st/st.post?lang=de"
	// parse click_id (optionally) and mId from action attribute
	// curl -b mycookie -k -d
	// "login=marcus.schiesser&pin=8X5VF1&button.login_p=Login"
	// "https://fahrkarten.bahn.de/mobile/st/li.post?lang=de"
	public HTTPSession login(String login, String pin) throws IOException, SAXException {
		HTTPSession session = new HTTPSession();
		session.getMethod(startURL);
		final String params = "login=" + login + "&pin=" + pin + "&button.login_p=Login";
		String response = session.postMethod(loginURL, params);
		InputStream inputStream = HTTPUtils.stringToStream(response);
		LoginValidationHandler handler = new LoginValidationHandler();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(inputStream));
		if(!handler.isValid()) {
			log.info("User: " + login + " provided wrong credentials.");
			ExceptionUtils.throwError("Invalid login/pin combination. Please try again with different credentials.");
		}
		return session;
	}

	/*
	 * 1. schritt // curl //
	 * http://mobile.bahn.de/bin/mobil/query2.exe/dox?rt=1&use_realtime_filter=1
	 * // dort die parameter i und ld filtern // 2. schritt // curl -d
	 * "ld=9627&amp;n=1&amp;i=02.02743727.1304240411&rt=1&use_realtime_filter=1&OK#focus&REQ0HafasOptimize1=0%3A1&REQ0HafasSearchForw=1&REQ0JourneyDate=28.04.11&REQ0JourneyStopsS0A=1&REQ0JourneyStopsS0G=frankfurt&REQ0JourneyStopsS0ID=&REQ0JourneyStopsZ0A=1&REQ0JourneyStopsZ0G=berlin&REQ0JourneyStopsZ0ID=&REQ0JourneyTime=19%3A30&REQ0Tariff_Class=2&REQ0Tariff_TravellerAge.1=35&REQ0Tariff_TravellerReductionClass.1=0&REQ0Tariff_TravellerType.1=E&existOptimizePrice=1&existProductNahverkehr=yes&immediateAvail=ON&start=Suchen"
	 * http://mobile.bahn.de/bin/mobil/query2.exe/dox
	 */
	
	public HTTPSession loginAnonymous() throws IOException,
			UnsupportedEncodingException, SAXException {
		SessionExtractionHandler handler = new SessionExtractionHandler();

		String response = HTTPUtils.getMethod(loginAnonymousURL);
		InputStream inputStream = HTTPUtils.stringToStream(response);

		reader.setContentHandler(handler);
		reader.parse(new InputSource(inputStream));
		return new HTTPSession(handler.ldValue, handler.iValue);
	}
	
	public void logout(HTTPSession session) throws IOException {
		session.getMethod(logoutURL);
	}

}
