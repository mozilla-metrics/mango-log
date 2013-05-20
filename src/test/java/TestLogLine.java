/**
 * 
 */
package test.java;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mozilla.custom.parse.LogLine;

/**
 * @author aphadke
 *
 */
public class TestLogLine {


	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testValidateSplitNullInput() {
		LogLine ll;
		try {
			ll = new LogLine(null);
		} catch (Exception e) {
			assertNotNull(e.getMessage());
		}

	}

	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testValidateSplitDmoLine() {

		String v = "2620:101:8003:200:e5af:bd49:7c0f:ceaa - dmo=10.8.81.215.1367977569087177; __utma=150903082.1262655689.1367998579.1367998579.1367998579.1; __utmz=150903082.1367998579.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none) [10/May/2013:00:00:24 -0700] \"GET /?product=firefox-20.0.1-complete&os=win&lang=zh-TW HTTP/1.1\" 302 422 \"-\" \"Mozilla/5.0 (Windows NT 6.1; rv:14.0) Gecko/20100101 Firefox/14.0.1\" \"-\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
	}


	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testValidateSplitAmoLine() {
		String v = "1.1.1.1 addons.mozilla.org - [15/May/2013:00:00:00 -0700] \"GET /blocklist/3/%7Bec8030f7-c20a-464f-9b0e-13a3a9e97384%7D/19.0/Firefox/20130215130331/WINNT_x86-msvc/fi/release/Windows_NT%205.1/default/default/4/434/1/ HTTP/1.1\" 200 64622 \"-\" \"Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0\" \"-\" \"DNT:-\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testValidateSplitMarketplaceLine() {
		String v = "1.1.1.1 marketplace.firefox.com - [15/May/2013:00:00:05 -0700] \"POST /reviewers/review_viewing HTTP/1.1\" 200 783 \"https://marketplace.firefox.com/reviewers/apps/review/image-uploader\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:21.0) Gecko/20100101 Firefox/21.0\" \"lang=\"en-US\054\"; region=us; sessionid=\".eJxrYKotZNQI5UouLkqLL8nPTs0rZApVSA0wcA0uK4kqcDNO9Hf3iHTNz3Iur0hzDA0rMi03L88sZA7lKkktLknOz8_OTC1kCWUrzy_KTk0pZA3ljU8sLcmILy1OLYrPTClk62K--iAuVAhJNCkxGWhNSiF7qFpKVmJeen58UlF-OVAmM0UPpErPCcL1dHGCquQo1QMAZN48cQ:1UbvRp:abr1TatM8ygMVRaEkmF3YPBAmes\"; multidb_pin_writes=y\" \"DNT:1\" \"X-MOZ-B2G-DEVICE:- X-MOZ-B2G-MCC:- X-MOZ-B2G-MNC:- X-MOZ-B2G-SHORTNAME:- X-MOZ-B2G-LONGNAME:-\"";
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
		
	}

	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testValidateSplitVamoLine() {
		String v = "1.1.1.1 versioncheck.addons.mozilla.org - [14/May/2013:07:00:09 -0700] \"GET /update/VersionCheck.php?reqVersion=2&id={972ce4c6-7e08-4474-a285-3208198ce6fd}&version=10.0&maxAppVersion=10.0&status=userEnabled&appID={ec8030f7-c20a-464f-9b0e-13a3a9e97384}&appVersion=10.0&appOS=WINNT&appABI=x86-msvc&locale=en-US&currentAppVersion=10.0&updateType=112&compatMode=normal HTTP/1.1\" 200 525 \"-\" \"Mozilla/5.0 (Windows NT 6.1; rv:10.0) Gecko/20100101 Firefox/10.0\" \"__utma=150903082.733308021.1361633654.1361633654.1361633654.1; __utmz=150903082.1361633654.1.1.utmcsr=firstrow1.eu|utmccn=(referral)|utmcmd=referral|utmcct=/watch/155095/1/watch-manchester-united-vs-queens-park-rangers.html\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#validateSplit(java.lang.String)}.
	 */
	@Test
	public void testInvalidSplitLine() {
		String v = "1.1.1.1 - - [12/May/2013:07:44:57 -0700] \"GET /bundles/bing/addon/bing.xpi HTTP/1.1\" 200 24166 - \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:19.0) Gecko/20100101 Firefox/19.0\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), -1);
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
	}


	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#getRawTableString()}.
	 */
	@Test
	public void testGetRawTableString() {

		String v = "1.1.1.1 versioncheck.addons.mozilla.org - [14/May/2013:07:00:09 -0700] \"GET /update/VersionCheck.php?reqVersion=2&id={972ce4c6-7e08-4474-a285-3208198ce6fd}&version=10.0&maxAppVersion=10.0&status=userEnabled&appID={ec8030f7-c20a-464f-9b0e-13a3a9e97384}&appVersion=10.0&appOS=WINNT&appABI=x86-msvc&locale=en-US&currentAppVersion=10.0&updateType=112&compatMode=normal HTTP/1.1\" 200 525 \"-\" \"Mozilla/5.0 (Windows NT 6.1; rv:10.0) Gecko/20100101 Firefox/10.0\" \"__utma=150903082.733308021.1361633654.1361633654.1361633654.1; __utmz=150903082.1361633654.1.1.utmcsr=firstrow1.eu|utmccn=(referral)|utmcmd=referral|utmcct=/watch/155095/1/watch-manchester-united-vs-queens-park-rangers.html\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
			assertNotNull(ll.getRawTableString());
		} catch (Exception e) {
			assertNull(e.getMessage());
		}
		
	}
	
	@Test
	public void testCorrectaddDate() {

		String v = "1.1.1.1 versioncheck.addons.mozilla.org - [14/May/2013:07:00:09 -0700] \"GET /update/VersionCheck.php?reqVersion=2&id={972ce4c6-7e08-4474-a285-3208198ce6fd}&version=10.0&maxAppVersion=10.0&status=userEnabled&appID={ec8030f7-c20a-464f-9b0e-13a3a9e97384}&appVersion=10.0&appOS=WINNT&appABI=x86-msvc&locale=en-US&currentAppVersion=10.0&updateType=112&compatMode=normal HTTP/1.1\" 200 525 \"-\" \"Mozilla/5.0 (Windows NT 6.1; rv:10.0) Gecko/20100101 Firefox/10.0\" \"__utma=150903082.733308021.1361633654.1361633654.1361633654.1; __utmz=150903082.1361633654.1.1.utmcsr=firstrow1.eu|utmccn=(referral)|utmcmd=referral|utmcct=/watch/155095/1/watch-manchester-united-vs-queens-park-rangers.html\"";		
		LogLine ll;
		try {
			ll = new LogLine(v);
			assertEquals(ll.validateSplitCount(), 12);
			assertTrue(ll.addDate());

			assertEquals(ll.getDbLogLine().get(0), "2013-05-14:14:00:09 +0000");
			assertEquals(ll.getDbLogLine().get(1), ll.getDbSplitPattern().group(4));
			
		} catch (Exception e) {
			assertNull(e.getMessage());
		}

	}

	
	
	/**
	 * Test method for {@link com.mozilla.custom.parse.LogLine#getAnonymousTableString()}.
	 */
	@Test
	public void testGetAnonymousTableString() {
		fail("Not yet implemented");
	}

}
