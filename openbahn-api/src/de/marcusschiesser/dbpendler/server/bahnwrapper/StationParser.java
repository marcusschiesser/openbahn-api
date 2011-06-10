package de.marcusschiesser.dbpendler.server.bahnwrapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;

import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.StationServerResource;
import de.marcusschiesser.dbpendler.server.utils.HTTPUtils;

public class StationParser {

	public static final Logger log = Logger.getLogger(StationParser.class.getName());
	private static StationParser instance = new StationParser();
	private Pattern pattern;

	public StationParser() {
		pattern = Pattern.compile("\\[.*\\]");
	}

	public static StationParser getInstance() {
		return instance;
	}
	
	public List<StationVO> getStations(String contains)  {
		try {
			contains = URLEncoder.encode(contains, "utf-8");
			String response = HTTPUtils.getMethod(new URL("http://reiseauskunft.bahn.de/bin/ajax-getstop.exe/dn?start=1&tpl=sls&REQ0JourneyStopsB=12&REQ0JourneyStopsS0A=1&getstop=1&noSession=yes&iER=yes&S=" + contains + "?&js=true"));
	
			Matcher matcher = pattern.matcher(response);
			if(matcher.find()) {
				String filter = matcher.group();
				ObjectMapper m = new ObjectMapper();
				try {
					StationVO stations[] = m.readValue(filter, StationVO[].class);
					return Arrays.asList(stations);
				} catch (Exception e) {
					StationServerResource.log.severe("could not parse object to stationvo: " + e.toString());
				}
			} else {
				StationServerResource.log.severe("bahn.de changed the format. can not match: " + response);
			}
		} catch (MalformedURLException e) {
			StationServerResource.log.severe("error getting station object: " + e.toString());
		} catch (IOException e) {
			StationServerResource.log.severe("error getting station object: " + e.toString());
		}
		return Collections.emptyList();
	}

}
