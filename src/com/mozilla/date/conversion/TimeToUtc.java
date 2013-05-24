package com.mozilla.date.conversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeToUtc {
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	private static SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss Z");

	public String getUTCDate(String date) {
		local.setTimeZone(TimeZone.getTimeZone("UTC"));  

		try {
			Date d = formatter.parse(date.toString());
			return (local.format(d).toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

}
