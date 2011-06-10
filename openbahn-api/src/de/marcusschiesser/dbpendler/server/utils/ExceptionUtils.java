package de.marcusschiesser.dbpendler.server.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ExceptionUtils {

	public static void checkRequiredParamters(Object... params) throws WebApplicationException {
		for (int i = 0; i < params.length; i += 2) {
			String paramName = (String) params[i];
			Object param = params[i + 1];
			if (param == null) {
				throwError("must set '" + paramName + "' parameter");
			}
		}
	}

	public static void throwError(String errorText) throws WebApplicationException {
		throw new WebApplicationException(Response.status(Status.OK).entity("{error: \"" + errorText + "\"}").build());
	}
	
	public static void throwError(Exception e) throws WebApplicationException {
		throw new WebApplicationException(e, Status.OK);
	}

}
