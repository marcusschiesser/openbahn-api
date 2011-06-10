package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marcusschiesser.dbpendler.common.vo.CommitVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;

public class CommitValidationHandler extends DefaultHandler {
	
	//private final Logger log = Logger.getLogger(CommitValidationHandler.class.getName());

	private InsideDivType insideDiv = InsideDivType.none;
	private Pattern orderPattern = Pattern.compile("Auftrags-Nr.: (.+)");
	private Pattern datePattern = Pattern.compile("am ([0-9\\.]+).+ab ([0-9:]+)");
	private InsideSpanType insideSpan = InsideSpanType.none;
	private String orderNumber = null;
	private StationVO start;
	private StationVO destination;
	private Date date;
	private Date time;
	private String reservation;

	private enum InsideDivType { none, main, order, connection, off; };
	private enum InsideSpanType { none, date, reservervation, off; };

	public CommitValidationHandler() {
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(insideDiv==InsideDivType.none && localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("haupt")) {
			insideDiv = InsideDivType.main;
		} else if(insideDiv==InsideDivType.main && localName.equalsIgnoreCase("div")) {
			insideDiv = InsideDivType.order;
		} else if(insideDiv==InsideDivType.order && localName.equalsIgnoreCase("div")) {
			insideDiv = InsideDivType.connection;
		} else if(insideDiv==InsideDivType.connection && localName.equalsIgnoreCase("span")) {
			if(insideSpan==InsideSpanType.none) {
				insideSpan = InsideSpanType.date;
			} else {
				insideSpan = InsideSpanType.reservervation;
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(insideDiv==InsideDivType.connection && localName.equalsIgnoreCase("div")) {
			insideDiv = InsideDivType.off;
			insideSpan = InsideSpanType.off;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch);
		switch(insideDiv) {
		case order:
			Matcher matcher = orderPattern.matcher(s);
			if(matcher.find()) {
				orderNumber = matcher.group(1);
			}
			break;
		case connection:
			switch(insideSpan) {
			case none:
				String stations[] = s.split("-");
				this.start = new StationVO(stations[0].trim());
				destination = new StationVO(stations[1].trim());
				break;
			case date:
				// am 26.05.2011, ICE   78, ab 08:51
				Matcher dateMatcher = datePattern.matcher(s);
				if(dateMatcher.find()) {
					try {
						date = DateUtils.getDateFormat().parse(dateMatcher.group(1));
						time = DateUtils.getTimeFormat().parse(dateMatcher.group(2));
					} catch(ParseException e) {
					}
				}
				break;
			case reservervation:
				reservation = s;
				break;
			}
			break;
		}
	}

	public boolean isValid() {
		return orderNumber!=null;
	}
	
	public CommitVO getCommitData() {
		return new CommitVO(start, destination, date, time, reservation, orderNumber);
	}

}