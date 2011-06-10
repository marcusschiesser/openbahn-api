package de.marcusschiesser.dbpendler.server.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	
	public static Date unifyDateTime(Date date, Date time) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		Calendar timeCalender = getCalendar();
		timeCalender.setTime(time);
		calendar.set(Calendar.HOUR_OF_DAY, timeCalender.get(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, timeCalender.get(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, timeCalender.get(Calendar.SECOND));
		return calendar.getTime();
	}
	
	public static Calendar getCalendar() {
		// should be fine for date calculations as bahn.de operates in GERMANY
		return Calendar.getInstance(Locale.GERMANY);
	}
	
	public static Date getThisMonday() {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String date = getDateFormat().format(calendar.getTime());
		try {
			return getDateFormat().parse(date);
		} catch (ParseException e) {
			// should never ever be called.
			e.printStackTrace();
			return null;
		}
	}
	
	public static Date addMinutes(Date date, int minutes) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}


	public static Date addDay(Date date, int days) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}

	public static DateFormat getTimeFormat() {
		return new SimpleDateFormat("HH:mm");
	}

	public static DateFormat getDateFormat() {
		return new SimpleDateFormat("dd.MM.yyyy");
	}
}
