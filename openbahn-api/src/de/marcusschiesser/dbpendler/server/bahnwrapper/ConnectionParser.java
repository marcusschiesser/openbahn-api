package de.marcusschiesser.dbpendler.server.bahnwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.TimeExtractionHandler;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;
import de.marcusschiesser.dbpendler.server.utils.HTTPUtils;

public class ConnectionParser {
	
	private static ConnectionParser instance = new ConnectionParser();
	
	private final Logger log = Logger.getLogger(ConnectionParser.class.getName());
	private XMLReader reader;
	private DateFormat format;
	private DateFormat timeFormat;

	private URL connectionURL;

	public static ConnectionParser getInstance() {
		return instance;
	}
	
	private ConnectionParser() {
		reader = new org.htmlparser.sax.XMLReader();
		format = DateUtils.getDateFormat();
		timeFormat = DateUtils.getTimeFormat();
		try {
			connectionURL = 
				new URL("http://mobile.bahn.de/bin/mobil/query2.exe/dox");
		} catch (MalformedURLException e) {
			log.severe("error parsing URL: " + e.toString());
		}
	}

	public List<ConnectionVO> getConnection(StationVO start, StationVO destination, Date date) {
		try {
			HTTPSession session = Login.getInstance().loginAnonymous();
			Collection<ConnectionVO> result = new ArrayList<ConnectionVO>();
			Date actTime = timeFormat.parse("00:00");
			final Date endTime = timeFormat.parse("23:59");
			do{
				List<ConnectionVO> connections = getConnectionTime(session, start, destination, date, actTime);
				ConnectionVO lastConnection = connections.get(connections.size()-1);
				result.addAll(connections);
				// increment one minute the actual time
				actTime = DateUtils.addMinutes(lastConnection.getStartTime(), 1);
			}while(actTime.before(endTime));
			// filter connections that are not from today
			Iterable<ConnectionVO> filteredResult = Iterables.filter(result, new Predicate<ConnectionVO>() {
				@Override
				public boolean apply(ConnectionVO connection) {
					return connection.getStartTime().before(endTime);
				}
			});
			// filter duplicate connections and return the result
			return Lists.newArrayList(Sets.newLinkedHashSet(filteredResult));
		} catch (IOException e) {
			log.severe("I/O error: " + e.toString());
		} catch (SAXException e) {
			log.severe("Parsing error: " + e.toString());
		} catch (ParseException e) {
			log.severe("wrong time format error: " + e.toString());
		}
		return Collections.emptyList();
	}

	public List<ConnectionVO> getConnectionTime(HTTPSession session, StationVO start,
			StationVO destination, Date date, Date queryTime)
			throws UnsupportedEncodingException, IOException, SAXException {
		String queryTimeString = timeFormat.format(queryTime);
		String queryDateString = format.format(date);
		String postMethod = session.postMethod(connectionURL,
						  "n=1&rt=1&use_realtime_filter=1&OK#focus&REQ0HafasOptimize1=0%3A1&REQ0HafasSearchForw=1"
						+ "&REQ0JourneyDate="
						+ queryDateString
						+ "&REQ0JourneyStopsS0A=1&REQ0JourneyStopsS0G="
						+ start.getValue()
						+ "&REQ0JourneyStopsS0ID=&REQ0JourneyStopsZ0A=1"
						+ "&REQ0JourneyStopsZ0G="
						+ destination.getValue()
						+ "&REQ0JourneyStopsZ0ID=&REQ0JourneyTime="
						+ queryTimeString
						+ "&REQ0Tariff_Class=2&REQ0Tariff_TravellerAge.1=35&REQ0Tariff_TravellerReductionClass.1=0&REQ0Tariff_TravellerType.1=E&existOptimizePrice=1&existProductNahverkehr=yes&immediateAvail=ON&start=Suchen");

		InputStream inputStream = HTTPUtils.stringToStream(postMethod);
		TimeExtractionHandler timeHandler = new TimeExtractionHandler();
		reader.setContentHandler(timeHandler);
		reader.parse(new InputSource(inputStream));
		
		List<ConnectionVO> result = new ArrayList<ConnectionVO>(timeHandler.getStartTimes().size());
		for (int i=0; i<timeHandler.getStartTimes().size(); i++) {
			Date startTimeDate = timeHandler.getStartTimes().get(i);
			Date destinationTimeDate = timeHandler.getEndTimes().get(i);
			String link = timeHandler.getAnchors().get(i);
			Double price = timeHandler.getPrices().get(i);
			if(destinationTimeDate.before(startTimeDate)) {
				// destination time can not be before start time, must add one day 
				// TODO: does only work for less than 24 hour trips!, but right now they are not in scope
				destinationTimeDate = DateUtils.addDay(destinationTimeDate, 1);
			}
			if(startTimeDate.before(queryTime)) {
				// start time can not be before query time, must add one day 
				// TODO: does only work for less than 24 hour trips!, but right now they are not in scope
				startTimeDate = DateUtils.addDay(startTimeDate, 1);
			}
			ConnectionVO connection = new ConnectionVO(start, destination, 
					startTimeDate, destinationTimeDate, date, price, link);
			result.add(connection);
		}
		return result;
	}

}
