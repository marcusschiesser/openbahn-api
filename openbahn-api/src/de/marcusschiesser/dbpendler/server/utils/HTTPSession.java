package de.marcusschiesser.dbpendler.server.utils;

import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;
import static com.google.appengine.api.urlfetch.HTTPMethod.POST;
import static com.google.appengine.api.urlfetch.URLFetchServiceFactory.getURLFetchService;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import com.google.appengine.repackaged.com.google.common.collect.Collections2;

public class HTTPSession {
	
	private final Logger log = Logger.getLogger(HTTPSession.class.getName());
	
	private HTTPHeader cookie;
	private String sessionParam;
	
	public HTTPSession() {
		cookie = null;
		sessionParam = null;
	}
	
	public HTTPSession(String ld, String i) {
		// this defines an anonymous session
		sessionParam = "ld=" + ld + "&i=" + i;
	}

	private void updateCookies(HTTPResponse resp) {
		if(sessionParam!=null) return; // don't use cookies with anonymous sessions
		List<HTTPHeader> headers = resp.getHeaders();
		Collection<HTTPHeader> cookies = Collections2.filter(headers, new Predicate<HTTPHeader>() {
			@Override
			public boolean apply(HTTPHeader header) {
				return header.getName().equalsIgnoreCase("Set-Cookie");
			}
		});
		if(!cookies.isEmpty()) {
			logCookies(cookies);
			HTTPHeader setcookie = cookies.iterator().next();
			String[] values = setcookie.getValue().split(";");
			if(values.length>0) {
				String value = values[0];
				if(value.split("=")[0].equalsIgnoreCase("jsessionid")) { 
					// only update jsessionid cookies
					cookie = new HTTPHeader("Cookie", value);
				}
			}
		}
	}
	
	private void logCookies(Collection<HTTPHeader> cookies) {
		for(HTTPHeader cookie: cookies) {
			log.finer("Server returned cookie: " + cookie.getName() + "=" + cookie.getValue());
		}
	}

	public String getMethod(URL url) throws IOException {
		log.finer("Calling GET on " + url);
		HTTPRequest req = new HTTPRequest(url);
		if(cookie!=null) 
			req.setHeader(cookie);
		HTTPResponse resp = getURLFetchService().fetch(req);
		updateCookies(resp);
		int responseCode = -1;
		if (resp != null) {
			responseCode = resp.getResponseCode();
			if (responseCode == 200) {
				String response = new String(resp.getContent());
				log.finer("GET returns: " + response);
				return response;
			}
		}
		throw new IOException("Problem calling GET on: " + url.toString() + " response: " + responseCode);
	}

	public String postMethod(URL url, String params) throws IOException {
		log.finer("Calling POST on " + url + " with params: " + params);
		HTTPRequest req = new HTTPRequest(url, POST, followRedirects()); //.withDeadline(10.0));
		//req.setHeader(new HTTPHeader("x-header-one", "some header"));
		if(cookie!=null)
			req.setHeader(cookie);
		if(sessionParam!=null)
			params = sessionParam + "&" + params;
		req.setPayload(params.getBytes());
	
		HTTPResponse resp = getURLFetchService().fetch(req);
		updateCookies(resp);
		int responseCode = -1;
		if (resp != null) {
			responseCode = resp.getResponseCode();
			if (responseCode == 200) {
				// List<HTTPHeader> headers = resp.getHeaders();
				String response = new String(resp.getContent());
				log.finer("POST returns: " + response);
				return response;
			}
		}
		throw new IOException("Problem calling GET on: " + url.toString() + " response: " + responseCode);
	}

}
