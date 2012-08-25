package com.vaguehope.lookfar.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatFactory extends ThreadLocal<DateFormat> {

	private static final DateFormatFactory INSTANCE = new DateFormatFactory();

	private DateFormatFactory () {}

	public static DateFormat getInstance () {
		return INSTANCE.get();
	}

	public static String format (Date date) {
		return getInstance().format(date);
	}

	@Override
	protected DateFormat initialValue () {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df;
	}

}
