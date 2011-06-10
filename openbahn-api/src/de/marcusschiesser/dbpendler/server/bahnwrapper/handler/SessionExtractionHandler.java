package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SessionExtractionHandler extends DefaultHandler {
	private Pattern pattern = Pattern.compile("ld=([^&]*).*i=([^&]*)");

	public String ldValue = null;
	public String iValue = null;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equalsIgnoreCase("form")) {
			String action = attributes.getValue("action");
			Matcher matcher = pattern.matcher(action);
			if (matcher.find()) {
				ldValue = matcher.group(1);
				iValue = matcher.group(2);
			}
		}
	}
}