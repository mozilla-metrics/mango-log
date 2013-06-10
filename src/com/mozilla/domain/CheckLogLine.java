package com.mozilla.domain;

import org.apache.commons.lang3.StringUtils;

public class CheckLogLine {
	public static final String BUGZILLA_MOZILLA_ORG = "bugzilla.mozilla.org";
	public static final String DOWNLOAD_MOZILLA_ORG = "download.mozilla.org";
	public static final String RELEASES_MOZILLA_COM = "releases.mozilla.com";
	
	public String logLine (String domainName, String line) {
		if (StringUtils.containsIgnoreCase(domainName, BUGZILLA_MOZILLA_ORG) || StringUtils.containsIgnoreCase(domainName, DOWNLOAD_MOZILLA_ORG)) {
			line = line + "\t\"-\"";
		}
		
		return line;
	}
}
