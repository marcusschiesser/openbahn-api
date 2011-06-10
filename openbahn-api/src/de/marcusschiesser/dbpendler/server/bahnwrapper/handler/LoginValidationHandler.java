package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LoginValidationHandler extends DefaultHandler {
	private boolean isValid = false;
	private boolean insideDiv = false;
	private String fullName;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("haupt")) {
			insideDiv = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase("div")) {
			insideDiv = false;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch).trim();
		if(insideDiv) { 
			if(isValid) {
				// this string must be the fullname(except the ending !)
				fullName = s.replace("!", "");
			}
			if(s.equals("Herzlich Willkommen")) {
				isValid = true;
			}
		}
	}

	public String getFullName() {
		return fullName;
	}

	public boolean isValid() {
		return isValid;
	}
}