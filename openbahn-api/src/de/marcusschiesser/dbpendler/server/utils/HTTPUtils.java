package de.marcusschiesser.dbpendler.server.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class HTTPUtils {

	private static HTTPSession session = new HTTPSession();
	
	public static String getMethod(URL url) throws IOException {
		return session.getMethod(url);
	}

	public static String postMethod(URL url, String params) throws IOException {
		return session.postMethod(url, params);
	}

	public static InputStream stringToStream(String response)
			throws UnsupportedEncodingException {
		byte[] byteArray = response.getBytes("UTF-8");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		return inputStream;
	}

}
