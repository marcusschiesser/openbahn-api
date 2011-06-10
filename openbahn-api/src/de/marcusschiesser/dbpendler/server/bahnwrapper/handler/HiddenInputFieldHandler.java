package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HiddenInputFieldHandler extends DefaultHandler {
	public Map<String, String> values = new HashMap<String, String>();

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equalsIgnoreCase("input") && attributes.getValue("type").equalsIgnoreCase("hidden")) {
			values.put(attributes.getValue("name"), attributes.getValue("value"));
		}
	}
		
	public Map<String, String> getValues() {
		return values;
	}
	
	public void clear() {
		values.clear();
	}
}