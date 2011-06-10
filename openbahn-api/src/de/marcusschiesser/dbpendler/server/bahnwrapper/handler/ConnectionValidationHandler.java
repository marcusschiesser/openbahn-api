package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;

public class ConnectionValidationHandler extends DefaultHandler {
	private boolean insideDiv = false;
	private boolean insidePriceDiv = false;
	private boolean insideStationDiv = false;
	private String start = null;
	private String destination = null;
	private double price;
	private Pattern pricePattern = Pattern.compile("Gesamtpreis: ([0-9,]+) EUR");
	
	private final Logger log = Logger.getLogger(ConnectionValidationHandler.class.getName());
	private boolean insideConnectionDiv = false;


/*
// TODO: parse this form and check connection data
//		<div id="content">
//		<form method="post" name="formular" autocomplete="off" action="/mobile/bu/bd.post?mId=21299b_x&lang=de">
//		 <input type="hidden" name="click_id" value="1.1107909795895603" />
//		<h1>Ihre Buchungsdaten</h1>
//		<div class="haupt rline">
//		<div class="bold">Reservierungswunsch:</div>
//		<div>1 Pl., 2. Kl., Groﬂraum, Fenster</div>
//		</div>
//		<div class="haupt bold rline">
//		Gesamtpreis: 2,50 EUR
//		</div>
//		<div class="haupt">
//		<div class="bold">Karlsruhe Hbf</div>
//		ICE   78<br />
//		ab 08:51, 18.05.2011<br />	
//		<div class="bold">Frankfurt(Main)Hbf</div>
//		an 09:53, 18.05.2011<br />
//		</div>
//		<div class="bline rline">
//		<input type="submit" name="button.weiter_p" value="Weiter" class="hauptbtn" title="Weiter" id="button.weiter" />
//		</div>
//		<div class="bline">
//		<input type="submit" name="button.zurueck_p" value="Zur¸ck" class="nebenbtn" title="Zur¸ck" id="button.zurueck" />
//		</div>
//		</form>
 */
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equalsIgnoreCase("form")) {
			insideDiv = true;
		}
		if(insideDiv && localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("haupt bold rline")) {
			insidePriceDiv = true;
		}
		if(insideDiv && localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("haupt")) {
			insideConnectionDiv  = true;
		}
		if(insideConnectionDiv && localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("bold")) {
			insideStationDiv = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase("form")) {
			insideDiv = false;
		}
		if(localName.equalsIgnoreCase("div")) {
			if(insidePriceDiv) {
				insidePriceDiv = false;
			}
			if(!insideStationDiv) {
				insideConnectionDiv = false;
			}
			if(insideStationDiv) {
				insideStationDiv = false;
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch);
		if(insidePriceDiv) {
			Matcher priceMatcher = pricePattern.matcher(s);
			if(priceMatcher.find()) {
				price = Double.parseDouble(priceMatcher.group(1).replace(',', '.'));
			} else {
				log.warning("strange: we are in the price tag, but no price has been found - check the parser.");
			}
		}
		if(insideStationDiv) {
			if(this.start==null) {
				this.start = s;
			} else {
				this.destination = s;
			}
		}
	}

	public double getPrice() {
		return price;
	}

	public String getStart() {
		return start;
	}

	public String getDestination() {
		return destination;
	}

	public boolean isValid(ConnectionVO connection) {
		return connection.getStart().getValue().equals(getStart())
			&& connection.getDestination().getValue().equals(getDestination());
//	TODO: this does not work yet, because of costs for reservation	connection.getPrice() == getPrice();
	}

}