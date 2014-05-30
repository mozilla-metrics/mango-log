package com.mozilla.udf;


import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

public class ParseDateForQtr extends UDF {
	public Text evaluate(Text s) {
		if (s != null) {

			String[] dateParse = s.toString().split("-");
			if (dateParse.length == 3) {
				String qtr = null;
				String year = dateParse[0];
				String month = dateParse[1];

				if (month.equals("01") || month.equals("02") || month.equals("03")) {
					qtr = "Q1";
				}
				if (month.equals("04") || month.equals("05") || month.equals("06")) {
					qtr = "Q2";
				}
				if (month.equals("07") || month.equals("08") || month.equals("09")) {
					qtr = "Q3";
				}
				if (month.equals("10") || month.equals("11") || month.equals("12")) {
					qtr = "Q4";
				}
				return new Text(year + "-" + qtr);
			}
		}
		return null;
	}
}
