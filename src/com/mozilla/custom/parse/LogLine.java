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
	private TimeToUtc pstToUtc;
	private Vector<String> dbLogLine;
	private IPtoGeo iptg;
	private Client c_parser;
	private String user_agent;
	
	public LogLine(String line) throws Exception {
		dbLogLine = new Vector<String>();
		pstToUtc = new TimeToUtc();
		this.line = line;
		if (StringUtils.isNotEmpty(this.line)) {
			this.m = p.matcher(this.line);
		} else {
			throw new Exception("input argument is null");
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
		String utcDate = pstToUtc.getUTCDate(m.group(4));

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
		c_parser = ua_parser.parse(m.group(10));
		user_agent = c_parser.userAgent.family;
		/*
		if (StringUtils.isNotBlank(user_agent)) {
			user_agent = customparse.tabletUAParse(m.group(10), user_agent);
		} else {
			user_agent = "NULL_UA_FAMILY";
		}
		*/
		dbLogLine.insertElementAt(user_agent, 15);

		user_agent = c_parser.userAgent.major;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_UA_MAJOR";
		}
		dbLogLine.insertElementAt(user_agent, 16);

		user_agent = c_parser.userAgent.minor;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_UA_MINOR";
		}
		dbLogLine.insertElementAt(user_agent, 17);

		user_agent = c_parser.os.family;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_OS_FAMILY";
		}
		dbLogLine.insertElementAt(user_agent, 18);

		user_agent = c_parser.os.major;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_OS_MAJOR";
		}
		dbLogLine.insertElementAt(user_agent, 19);

		user_agent = c_parser.os.minor;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_OS_MINOR";
		}
		dbLogLine.insertElementAt(user_agent, 20);

		user_agent = c_parser.device.family;
		if (StringUtils.isBlank(user_agent)) {
			user_agent = "NULL_DEVICE_FAMILY";
		}
		dbLogLine.insertElementAt(user_agent, 21);
		
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










