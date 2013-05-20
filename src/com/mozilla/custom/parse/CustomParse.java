package com.mozilla.custom.parse;

public class CustomParse {
	
	public String tabletUAParse(String input, String user_agent) {
		if (input.contains("Android; Tablet")) {
			user_agent += " Tablet";
		}
		return user_agent;

	}
}
