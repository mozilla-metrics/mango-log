package com.mozilla.main;

/**
 * This MR job converts each log line to the format as described in raw_logs and processed_logs
 * The transformation happens in the reducer. This class is invoked during nightly runs 
 * # of reducers is accepted via command line. Current default invocation via perl script is 40
 * 40 reducers are used so we get 40 splits in HIVE thereby enabling hive queries to run faster
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import ua_parser.Client;
import ua_parser.Parser;

import com.maxmind.geoip.LookupService;
import com.mozilla.custom.parse.LogLine;
import com.mozilla.domain.CheckLogLine;
import com.mozilla.geo.IPtoGeo;

public class MangoLogs {
	/**
	 * The map class of WordCount.
	 */
	static enum LOG_PROGRESS { ZERO_SIZED_HTTP_REQUEST, MAPPER_LINE_COUNT, INVALID_ANONYMOUS_SPLIT_COUNT, ERROR_UA_PARSER, LOG_LINES, INVALID_SPLIT, VALID_SPLIT, ERROR_DISTRIBUTED_CACHE, ERROR_GEOIP_DAT_URI_MISSING, ERROR_GEOIP_CITY_DAT_URI_MISSING, ERROR_GEOIP_DOMAIN_DAT_URI_MISSING, SETUP_CALLS, ERROR_GEOIP_LOOKUP,ERROR_REGEXES_YAML_LOOKUP, INVALID_DATE_FORMAT, INVALID_GEO_LOOKUP, VALID_ANONYMOUS_LINE_COUNT, INVALID_ANONYMOUS_LINE_COUNT, VALID_RAW_LINE_COUNT, ERROR_GEOIP_ISP_DAT_URI_MISSING, ERROR_GEOIP_ORG_DAT_URI_MISSING };
	public static String ANONYMIZED_PREFIX = "anonymized";
	public static String RAW_PREFIX = "raw";
	public static String DISTRIBUTED_CACHE_URI = "hdfs://admin1.testing.stage.metrics.scl3.mozilla.com:8020/user/aphadke/maxmind/";
	public static String GEOIP_CITY_DAT = "GeoIPCity.dat";
	public static String GEOIP_ORG_DAT = "GeoIPOrg.dat";
	public static String GEOIP_DOMAIN_DAT = "GeoIPDomain.dat";
	public static String GEOIP_ISP_DAT = "GeoIPISP.dat";


	public static class MangoLogsMapper extends Mapper<Object, Text, Text, Text> {
		//public static final Log LOG =  LogFactory.getLog("ReadLzoFile"); 
		private String input_fname, domain_name;
		private String[] splitSlash;
		private FileSplit fileSplit;
		private CheckLogLine cll;
		public void setup (Context context) {
			cll = new CheckLogLine();
			fileSplit = (FileSplit)context.getInputSplit();
			splitSlash = fileSplit.getPath().toString().split("/");
			input_fname = splitSlash[splitSlash.length - 2];
			domain_name = splitSlash[splitSlash.length - 1];

		}
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			context.getCounter(LOG_PROGRESS.MAPPER_LINE_COUNT).increment(1);
			String v = cll.logLine(fileSplit.toString(), value.toString());
			context.write(new Text(v), new Text(input_fname));
		}
	}

	/**
	 * The reducer class of WordCount
	 */
	public static class MangoLogsReducer extends
	Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		private Path[] localFiles;
		private LookupService cityLookup, domainLookup, orgLookup, ispLookup;
		private Vector<String> splitTab;
		private boolean validAnonymizedLine = true;
		private StringBuffer sb;
		private IPtoGeo iptg;
		private boolean missingDatFile = false;
		private Parser ua_parser;
		private Client c_parser;
		private InputStream is;

		private LogLine logline;
		/**
		 * runs before starting every mapper
		 * useful to get the file split name from the mapper
		 */

		public void setup(Context context) {
			mos = new MultipleOutputs<Text, Text>(context);
			context.getCounter(LOG_PROGRESS.SETUP_CALLS).increment(1);
			try {
				localFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				context.getCounter(LOG_PROGRESS.ERROR_DISTRIBUTED_CACHE).increment(1);
				e1.printStackTrace();
			}
			if (localFiles.length > 0) {
				for (Path localFile : localFiles) {
					if ((localFile.getName() != null) && (localFile.getName().equalsIgnoreCase(GEOIP_CITY_DAT))) {
						try {
							cityLookup = new LookupService(new File(localFile.toUri().getPath()), LookupService.GEOIP_MEMORY_CACHE);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							missingDatFile = true;
							context.getCounter(LOG_PROGRESS.ERROR_GEOIP_LOOKUP).increment(1);
						}
					} 
					if ((localFile.getName() != null) && (localFile.getName().equalsIgnoreCase(GEOIP_DOMAIN_DAT))) {
						try {
							domainLookup = new LookupService(new File(localFile.toUri().getPath()), LookupService.GEOIP_MEMORY_CACHE);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							missingDatFile = true;
							context.getCounter(LOG_PROGRESS.ERROR_GEOIP_LOOKUP).increment(1);
						}
					} 
					if ((localFile.getName() != null) && (localFile.getName().equalsIgnoreCase(GEOIP_ORG_DAT))) {
						try {
							orgLookup = new LookupService(new File(localFile.toUri().getPath()), LookupService.GEOIP_MEMORY_CACHE);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							missingDatFile = true;
							context.getCounter(LOG_PROGRESS.ERROR_GEOIP_LOOKUP).increment(1);
						}
					} 

					if ((localFile.getName() != null) && (localFile.getName().equalsIgnoreCase(GEOIP_ISP_DAT))) {
						try {
							ispLookup = new LookupService(new File(localFile.toUri().getPath()), LookupService.GEOIP_MEMORY_CACHE);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							missingDatFile = true;
							context.getCounter(LOG_PROGRESS.ERROR_GEOIP_LOOKUP).increment(1);
						}
					} 
					if ((localFile.getName() != null) && (localFile.getName().equalsIgnoreCase("regexes.yaml"))) {
						try {
							is = new FileInputStream(new File(localFile.toUri().getPath()));
							ua_parser = new Parser(is);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							missingDatFile = true;
							context.getCounter(LOG_PROGRESS.ERROR_REGEXES_YAML_LOOKUP).increment(1);
						}
					} 


				}
				if (missingDatFile) {
					context.getCounter(LOG_PROGRESS.ERROR_GEOIP_DAT_URI_MISSING).increment(1);
				}
			}

		}

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text value : values) {
				validAnonymizedLine = true;
				String v = key.toString();
				try {
					logline = new LogLine(v, "");
					
					splitTab = new Vector<String>();
					if (logline.getSplitCount() > 0) {
						context.getCounter(LOG_PROGRESS.VALID_SPLIT).increment(1);
						mos.write(RAW_PREFIX, logline.getRawTableString(), "");
						context.getCounter(LOG_PROGRESS.VALID_RAW_LINE_COUNT).increment(1);

						if (logline.addDate()) {
							if (!logline.addGeoLookUp(cityLookup, domainLookup, ispLookup, orgLookup)) {
								validAnonymizedLine = false;
							}
						} else {
							//TODO: add error date counter
							validAnonymizedLine = false;
						}
						logline.addHttpLogInfo();
						
						if (!logline.addUserAgentInfo(ua_parser)) {
							validAnonymizedLine = false;
						} 

						logline.addCustomAndOtherInfo();

						if (!logline.addFilename(value.toString())) {
							validAnonymizedLine = false;
						}

						if (validAnonymizedLine) {
							if (logline.checkOutputFormat()) {
								mos.write(ANONYMIZED_PREFIX, logline.getOutputLine(), "");
								context.getCounter(LOG_PROGRESS.VALID_ANONYMOUS_LINE_COUNT).increment(1);

							} else {
								context.write(new Text(logline.getOutputLine()), new Text(""));
								context.getCounter(LOG_PROGRESS.INVALID_ANONYMOUS_SPLIT_COUNT).increment(1);

							}

						} else {
							context.write(key, new Text(""));
							context.getCounter(LOG_PROGRESS.INVALID_ANONYMOUS_LINE_COUNT).increment(1);

						}

					} else {
						if (v.contains("\"  \" - 0 \"-\" \"-\" \"-\"")) {
							context.getCounter(LOG_PROGRESS.ZERO_SIZED_HTTP_REQUEST).increment(1);
						} else {
							context.getCounter(LOG_PROGRESS.INVALID_SPLIT).increment(1);
						}
						context.write(key, new Text(""));

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		protected void cleanup(Context context) throws IOException, InterruptedException {
			mos.close();
		}
	}

	/**
	 * The main entry point.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage:\nhadoop jar lzo.jar " +
					"<input> <output> <# of reducers>\n");
		}
		Configuration c = new Configuration();
		DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_CITY_DAT), c);
		DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_DOMAIN_DAT), c);
		DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_ISP_DAT), c);
		DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_ORG_DAT), c);
		DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + "regexes.yaml"), c);

		Job job;
		if (StringUtils.isNotBlank(args[3])) {
			job = new Job(c, args[3] + "backfill-" + args[0] + "-" + args[1]);
		}
		else {
			job = new Job(c, "backfill-" + args[0] + "-" + args[1]);
		}

		MultipleOutputs.addNamedOutput(job, ANONYMIZED_PREFIX, TextOutputFormat.class , Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, RAW_PREFIX, TextOutputFormat.class , Text.class, Text.class);

		job.setJarByClass(MangoLogs.class);

		job.setMapperClass(MangoLogsMapper.class);
		job.setReducerClass(MangoLogsReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setJobName("Logs " + args[3] + " " + args[2]);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		FileOutputFormat.setCompressOutput(job, true);

		FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);	

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		if (StringUtils.isNotBlank(args[2]) && Integer.parseInt(args[2]) > 0) {
			job.setNumReduceTasks(Integer.parseInt(args[2]));
		} else {
			job.setNumReduceTasks(20);
		}

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}