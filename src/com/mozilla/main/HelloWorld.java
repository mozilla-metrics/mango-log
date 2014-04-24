package com.mozilla.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

public class HelloWorld {

	/**
	 * @param args
	 * @throws GeoIp2Exception 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, GeoIp2Exception {
		// A File object pointing to your GeoIP2 or GeoLite2 database
		File database = new File("/Users/aphadke/Desktop/GeoIP2-City.mmdb");

		// This creates the DatabaseReader object, which should be reused across
		// lookups.
		DatabaseReader reader = new DatabaseReader.Builder(database).build();

		// Replace "city" with the appropriate method for your database, e.g.,
		// "country".
		CityResponse response = reader.city(InetAddress.getByName("128.101.101.101"));

		System.out.println(response.getCountry().getIsoCode()); // 'US'
		System.out.println(response.getCountry().getName()); // 'United States'
		System.out.println(response.getCountry().getNames().get("zh-CN")); // '美国'

		System.out.println(response.getMostSpecificSubdivision().getName()); // 'Minnesota'
		System.out.println(response.getMostSpecificSubdivision().getIsoCode()); // 'MN'

		System.out.println(response.getCity().getName()); // 'Minneapolis'

		System.out.println(response.getPostal().getCode()); // '55455'

		System.out.println(response.getLocation().getLatitude()); // 44.9733
		System.out.println(response.getLocation().getLongitude()); // -93.2323
		
		String v = "89.200.192.1 addons.mozilla.org - [15/May/2013:00:00:00 -0700] \"GET /blocklist/3/%7Bec8030f7-c20a-464f-9b0e-13a3a9e97384%7D/19.0/Firefox/20130215130331/WINNT_x86-msvc/fi/release/Windows_NT%205.1/default/default/4/434/1/ HTTP/1.1\" 200 64622 \"-\" \"Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0\" \"-\" \"DNT:-\"";		
		Pattern p = Pattern.compile("(?>([^\\s]+)\\s([^\\s]*)\\s(?>-|([^-](?:[^\\[\\s]++(?:(?!\\s\\[)[\\[\\s])?)++))\\s\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}\\s[-+]\\d{4})\\]\\s)(?>\"([A-Z]+)\\s([^\\s]*)\\sHTTP/1\\.[01]\"\\s(\\d{3})\\s(\\d+)\\s\"([^\"]+)\"\\s)(?>\"\"?([^\"]*)\"?\")(?>\\s\"([^\"]*)\")(?>\\s\"([^\"]*)\")?");		
		Matcher m = p.matcher(v);
		if (m.find()) {
			System.out.println(m.groupCount() + "\t" + m.group(1));
			
		}
		
		
		

	}

}
