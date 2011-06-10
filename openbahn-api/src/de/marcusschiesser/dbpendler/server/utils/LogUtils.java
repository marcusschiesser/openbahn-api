package de.marcusschiesser.dbpendler.server.utils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.logging.Logger;

import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;

public class LogUtils {
	private final static Logger log = Logger.getLogger(LogUtils.class.getName());

	public static void logConnections(Collection<ConnectionVO> result) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		for (ConnectionVO connection : result) {
			if(connection.getDate()==null) 
				log.finer("Start time: " + timeFormat.format(connection.getStartTime()) + " End time: " + timeFormat.format(connection.getDestinationTime()));
			else
				log.finer("Date: " + dateFormat.format(connection.getDate()) + " - Start time: " + timeFormat.format(connection.getStartTime()) + " End time: " + timeFormat.format(connection.getDestinationTime()));
		}
	}
}
