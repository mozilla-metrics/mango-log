package com.mozilla.domain;

import org.apache.commons.lang3.StringUtils;

public class CheckLogLine {
	public static String BUGZILLA_MOZILLA_ORG = "bugzilla.mozilla.org";
	public static String DOWNLOAD_MOZILLA_ORG = "download.mozilla.org";
	
	public String logLine (String domainName, String line) {
		if (StringUtils.containsIgnoreCase(domainName, BUGZILLA_MOZILLA_ORG) || StringUtils.containsIgnoreCase(domainName, DOWNLOAD_MOZILLA_ORG)) {
			line = line + "\t\"-\"";
		}
		return line;
	}
}
