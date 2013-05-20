package com.mozilla.geo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public class IPtoGeo {
	private String countryCode, countryName, lookupName, stateCode;
	
	private float latitude;
	private float longitude;
	private Location l1;
	private String GEO_LOOKUP_ERROR = "NO_GEO_LOOKUP";
	public IPtoGeo() {

	}
	public IPtoGeo(File geoDatFile) throws IOException {

	}

	public IPtoGeo(String ipAddress, Path p) throws IOException {
	}
	public boolean performGeoLookup(String ip, LookupService cl) {
		if (StringUtils.isBlank(ip)) {
			this.setCountryCode("GEO_ERROR_COUNTRY_CODE");
			this.setCountryName("GEO_ERROR_COUNTRY_NAME");
			this.setLatitude(-0.0f);	
			this.setLongitude(-0.0f);
			this.setStateCode("GEO_ERROR_STATE_CODE");
			return false;
		}
		l1  = cl.getLocation(ip);
		if (l1 == null) {
			this.setCountryCode("GEO_ERROR_COUNTRY_CODE");
			this.setCountryName("GEO_ERROR_COUNTRY_NAME");
			this.setLatitude(-0.0f);	
			this.setLongitude(-0.0f);
			this.setStateCode("GEO_ERROR_STATE_CODE");
		} else {
			this.setCountryCode(l1.countryCode);
			this.setCountryName(l1.countryName);
			this.setLatitude(l1.latitude);	
			this.setLongitude(l1.longitude);
			this.setStateCode(l1.region);
		}
		return true;
	}
	public boolean performOrgLookup(String ip, LookupService cl) {
		if (StringUtils.isBlank(ip)) {
			return false;
		}
		String orgName = cl.getOrg(ip);
		if (StringUtils.isBlank(orgName)) {
			this.setLookupName(GEO_LOOKUP_ERROR);
		} else {
			this.setLookupName(orgName);
		}
		return true;
	}
	
	
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getLookupName() {
		return lookupName;
	}
	public void setLookupName(String lookupName) {
		this.lookupName = lookupName;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
}
