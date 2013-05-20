package com.mozilla.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloWorld {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String v = "89.200.192.1 addons.mozilla.org - [15/May/2013:00:00:00 -0700] \"GET /blocklist/3/%7Bec8030f7-c20a-464f-9b0e-13a3a9e97384%7D/19.0/Firefox/20130215130331/WINNT_x86-msvc/fi/release/Windows_NT%205.1/default/default/4/434/1/ HTTP/1.1\" 200 64622 \"-\" \"Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0\" \"-\" \"DNT:-\"";		
		Pattern p = Pattern.compile("(?>([^\\s]+)\\s([^\\s]*)\\s(?>-|([^-](?:[^\\[\\s]++(?:(?!\\s\\[)[\\[\\s])?)++))\\s\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}\\s[-+]\\d{4})\\]\\s)(?>\"([A-Z]+)\\s([^\\s]*)\\sHTTP/1\\.[01]\"\\s(\\d{3})\\s(\\d+)\\s\"([^\"]+)\"\\s)(?>\"\"?([^\"]*)\"?\")(?>\\s\"([^\"]*)\")(?>\\s\"([^\"]*)\")?");		
		Matcher m = p.matcher(v);
		if (m.find()) {
			System.out.println(m.groupCount() + "\t" + m.group(1));
			
		}
		

	}

}
