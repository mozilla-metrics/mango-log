package com.mozilla.custom.parse;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ua_parser.Client;
import ua_parser.Parser;

import com.maxmind.geoip.LookupService;
import com.mozilla.date.conversion.TimeToUtc;
import com.mozilla.geo.IPtoGeo;


public class LogLine {
	Pattern p = Pattern.compile("(?>([^\\s]+)\\s([^\\s]*)\\s(?>-|([^-](?:[^\\[\\s]++(?:(?!\\s\\[)[\\[\\s])?)++))\\s\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}\\s[-+]\\d{4})\\]\\s)(?>\"([A-Z]+)\\s([^\\s]*)\\sHTTP/1\\.[01]\"\\s(\\d{3})\\s(\\d+)\\s\"([^\"]+)\"\\s)(?>\"\"?([^\"]*)\"?\")(?>\\s\"([^\"]*)\")(?>\\s\"([^\"]*)\")?");
	
	Matcher m;
	String line;
	StringBuffer sb;
	private TimeToUtc timeToUtc;
	private Vector<String> dbLogLine;
	private IPtoGeo iptg;
	private Client cParser;
	private String userAgent;
	
	public LogLine(String line, String domain_name) throws Exception {
		dbLogLine = new Vector<String>(30);
		timeToUtc = new TimeToUtc();
		this.line = line;
		if (StringUtils.isNotEmpty(this.line)) {
			if (StringUtils.equals(domain_name, "marketplace.firefox.com")) {
			    p = Pattern.compile("(?>([^\\s]+)\\s([^\\s]*)\\s(?>-|([^-](?:[^\\[\\s]++(?:(?!\\s\\[)[\\[\\s])?)++))\\s\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}\\s[-+]\\d{4})\\]\\s)(?>\"([A-Z]+)\\s([^\\s]*)\\sHTTP/1\\.[01]\"\\s(\\d{3})\\s(\\d+)\\s\"([^\"]+)\"\\s)(?>\"\"?([^\"]*)\"?\")(?>\\s\"([^\"]*)\")(?>\\s\"([^\"]*)\")(?>\\s\"([^\"]*)\")?");
			}
			
            this.m = p.matcher(this.line);
			
		} else {
			throw new IllegalArgumentException("input argument is null");
		}
	}

	public int getSplitCount() {
		if (StringUtils.isNotEmpty(line)) {
			if (m.find()) {
				return m.groupCount();
			} 
		}
		return -1;
	}

	public String getRawTableString() {
		sb = new StringBuffer();
		for (int i = 1; i <= m.groupCount(); i++) {
			sb.append(m.group(i) + "\t");
		}
		return sb.toString().trim();
	}

	public boolean addDate() {
		String utcDate = timeToUtc.getUTCDate(m.group(4));

		if (StringUtils.isNotBlank(utcDate)) {
			dbLogLine.insertElementAt(utcDate, 0); //utc date
			dbLogLine.insertElementAt(m.group(4), 1); //pst date
			return true;
		} 
		return false;
	}
	
	public boolean addGeoLookUp(LookupService cityLookup, LookupService domainLookup, LookupService ispLookup, LookupService orgLookup) {
		iptg = new IPtoGeo();
		iptg.performGeoLookup(m.group(1), cityLookup);
		dbLogLine.insertElementAt(iptg.getCountryCode(), 2);
		dbLogLine.insertElementAt(iptg.getCountryName(), 3);
		dbLogLine.insertElementAt(iptg.getLatitude() + "", 4);
		dbLogLine.insertElementAt(iptg.getLongitude() + "", 5);
		dbLogLine.insertElementAt(iptg.getStateCode() + "", 6);
		String lookup;
		
		if (iptg.performOrgLookup(m.group(1), domainLookup)) {
			lookup = iptg.getLookupName();
			if (StringUtils.equals(lookup,"NO_GEO_LOOKUP")) {
				lookup = "NO_DOMAIN_LOOKUP";
			} 
			dbLogLine.insertElementAt(lookup, 7);
		} else {
			return false;
		}
		if (iptg.performOrgLookup(m.group(1), orgLookup)) {
			lookup = iptg.getLookupName();
			if (StringUtils.equals(lookup,"NO_GEO_LOOKUP")) {
				lookup = "NO_ORG_LOOKUP";
			} 
			dbLogLine.insertElementAt(lookup, 8);
		} else {
			return false;
		}
		if (iptg.performOrgLookup(m.group(1), ispLookup)) {
			lookup = iptg.getLookupName();
			if (StringUtils.equals(lookup,"NO_GEO_LOOKUP")) {
				lookup = "NO_ISP_LOOKUP";
			} 
			dbLogLine.insertElementAt(lookup, 9);
		} else {
			return false;
		}
		return true;
	}
	
	public Vector<String> getDbLogLine() {
		return dbLogLine;
	}
	
	public Matcher getDbSplitPattern() {
		return m;
	}
	
	
	public void addHttpLogInfo() {
		dbLogLine.insertElementAt(m.group(5), 10);
		dbLogLine.insertElementAt(m.group(6), 11);
		dbLogLine.insertElementAt(m.group(7), 12);
		dbLogLine.insertElementAt(m.group(8), 13);
		dbLogLine.insertElementAt(m.group(9), 14);
	}
	
	public boolean addUserAgentInfo(Parser ua_parser) {
		cParser = ua_parser.parse(m.group(10));
		userAgent = cParser.userAgent.family;

		dbLogLine.insertElementAt(userAgent, 15);

		userAgent = cParser.userAgent.major;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_UA_MAJOR";
		}
		dbLogLine.insertElementAt(userAgent, 16);

		userAgent = cParser.userAgent.minor;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_UA_MINOR";
		}
		dbLogLine.insertElementAt(userAgent, 17);

		userAgent = cParser.os.family;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_OS_FAMILY";
		}
		dbLogLine.insertElementAt(userAgent, 18);

		userAgent = cParser.os.major;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_OS_MAJOR";
		}
		dbLogLine.insertElementAt(userAgent, 19);

		userAgent = cParser.os.minor;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_OS_MINOR";
		}
		dbLogLine.insertElementAt(userAgent, 20);

		userAgent = cParser.device.family;
		if (StringUtils.isBlank(userAgent)) {
			userAgent = "NULL_DEVICE_FAMILY";
		}
		dbLogLine.insertElementAt(userAgent, 21);
		
		return true;
	}

	public void addCustomAndOtherInfo() {
		dbLogLine.insertElementAt(m.group(12), 22);
		if (m.groupCount() == 13) {
			dbLogLine.insertElementAt(m.group(13), 23);
		} else {
			dbLogLine.insertElementAt("-", 23);
		}
		dbLogLine.insertElementAt("-", 24);
	}
	
	public boolean addFilename(String filename) {
		if (StringUtils.isNotEmpty(filename)) {
			dbLogLine.insertElementAt(filename, 25);
			return true;
		}
		return false;
	}
	
	public boolean checkOutputFormat() {
		if (dbLogLine.size() == 26) {
			return true;
		}
		return false;
	}
	
	public String getOutputLine() {
		sb = new StringBuffer();
		for (String st : dbLogLine) {
			sb.append(st + "\t");
		}
		return sb.toString().trim();

	}
}

