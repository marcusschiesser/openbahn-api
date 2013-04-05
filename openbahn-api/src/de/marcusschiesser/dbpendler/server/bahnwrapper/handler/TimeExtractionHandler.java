package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marcusschiesser.dbpendler.server.utils.DateUtils;

public class TimeExtractionHandler extends DefaultHandler {

	private final Logger log = Logger.getLogger(TimeExtractionHandler.class.getName());

	private int insideTimelink = 0;
	private List<Date> startTimes = new ArrayList<Date>();
	private List<Date> endTimes = new ArrayList<Date>();
	private List<String> anchors = new ArrayList<String>();
	private List<Double> prices = new ArrayList<Double>();
	private List<String> connectionTypes = new ArrayList<String>();
	private DateFormat timeFormat = DateUtils.getTimeFormat();
	private int insidePrice = 0;
	
	@Override
	public void startElement(String uri, String localName, String qName,
	        Attributes attributes) throws SAXException {
	
	        if (localName.equalsIgnoreCase("td")
	                && "overview timelink".equalsIgnoreCase(attributes.getValue("class"))) {
	            insideTimelink = 1;
	        }
	        if (localName.equalsIgnoreCase("td")
	                && "overview iphonepfeil".equalsIgnoreCase(attributes.getValue("class"))) {
	            insidePrice = 1;
	        }
	        if (insideTimelink > 0 && "a".equalsIgnoreCase(localName)) {
	            anchors.add(attributes.getValue("href"));
	        }
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase("td")) {
			if(insideTimelink>0) insideTimelink = 0;
			if(insidePrice>0) insidePrice = 0;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch);
		if(insideTimelink>0) {
			if(insideTimelink==1) {
				Date d = null;
				try {
					d = timeFormat.parse(s);
				} catch (ParseException e) {
					log.severe("Error parsing start time: " + e.toString());
				}
				startTimes.add(d);
			} else {
				Date d = null;
				try {
					d = timeFormat.parse(s);
				} catch (ParseException e) {
					log.severe("Error parsing end time: " + e.toString());
				}
				endTimes.add(d);
			}
			insideTimelink++;
		}
		if(insidePrice>0) {
			if(insidePrice==1) {
				connectionTypes.add(s);
			} else {
				String priceAsString = s.replace("&nbsp;EUR", "").replace(',', '.');
				try {
					double price = Double.parseDouble(priceAsString);
					log.info("Parsed price: " + price);
					prices.add(price);
				}catch(NumberFormatException e) {
					prices.add(null);
				}
			}
			insidePrice++;
		}
	}
	
	public List<Date> getStartTimes() {
		return startTimes;
	}
	public List<Date> getEndTimes() {
		return endTimes;
	}
	public List<String> getConnectionTypes() {
		return connectionTypes;
	}
	public List<Double> getPrices() {
		return prices;
	}
	public List<String> getAnchors() {
		return anchors;
	}
	
	public void reset() {
		startTimes.clear();
		endTimes.clear();
		connectionTypes.clear();
		anchors.clear();
		prices.clear();
	}
	
	public int getSize() {
		return anchors.size();
	}

}
