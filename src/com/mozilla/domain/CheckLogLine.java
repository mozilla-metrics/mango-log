package com.mozilla.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class CheckLogLine {
	public static String BUGZILLA_MOZILLA_ORG = "bugzilla.mozilla.org";
	public static String DOWNLOAD_MOZILLA_ORG = "download.mozilla.org";
	public static String RELEASES_MOZILLA_COM = "releases.mozilla.com";
	public StringBuffer sb;
	private static Pattern p = Pattern.compile("^(?>([\\d.]{7,15})\\s([^\\s]*)\\s([^\\s]*)\\s\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}\\s[-+]\\d{4})\\]\\s)(?>\"([A-Z]+)\\s([^\\s]*)\\sHTTP/1\\.[01]\"\\s(\\d{3})\\s(\\d+)\\s([^\\s]*)\\s\"([^\"]+)\"\\s)(?>\"\"?([^\"]*)\"?\")(?>\\s\"([^\"]*)\")?");
	
	private Matcher m;

	public String logLine (String domainName, String line) {
		if (StringUtils.containsIgnoreCase(domainName, BUGZILLA_MOZILLA_ORG) || StringUtils.containsIgnoreCase(domainName, DOWNLOAD_MOZILLA_ORG)) {
			line = line + "\t\"-\"";
		} else if (StringUtils.containsIgnoreCase(domainName, RELEASES_MOZILLA_COM)) {
			m = p.matcher(line);
			if (m.find() && m.groupCount() == 12) {
				sb = new StringBuffer();
				String customField = "";
				boolean matchDownloadFlag = false;
				for (int i=1; i <= m.groupCount(); i++) {
					if (i == 9) {
						customField = m.group(i);
						matchDownloadFlag = true;
					} else {
						sb.append(m.group(i) + " ");
					}
				}
				if (matchDownloadFlag) {
					line = sb.toString() + customField;
				} else {
					line = sb.toString().trim();
				}
			}
		}


		return line;
	}
}
