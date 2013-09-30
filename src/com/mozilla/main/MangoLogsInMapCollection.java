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
import java.util.List;
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

import ua_parser.Client;
import ua_parser.Parser;

import com.maxmind.geoip.LookupService;
import com.mozilla.custom.parse.LogLine;
import com.mozilla.domain.CheckLogLine;
import com.mozilla.geo.IPtoGeo;

public class MangoLogsInMapCollection {
    /**
     * The map class of WordCount.
     */
    
    static enum LOG_PROGRESS { REDUCER_COUNT, REDUCER_COUNT_IO, REDUCER_COUNT_IE, ZERO_SIZED_HTTP_REQUEST, MAPPER_LINE_COUNT, INVALID_ANONYMOUS_SPLIT_COUNT, ERROR_UA_PARSER, LOG_LINES, INVALID_SPLIT, VALID_SPLIT, ERROR_DISTRIBUTED_CACHE, ERROR_GEOIP_DAT_URI_MISSING, ERROR_GEOIP_CITY_DAT_URI_MISSING, ERROR_GEOIP_DOMAIN_DAT_URI_MISSING, SETUP_CALLS, ERROR_GEOIP_LOOKUP,ERROR_REGEXES_YAML_LOOKUP, INVALID_DATE_FORMAT, INVALID_GEO_LOOKUP, VALID_ANONYMOUS_LINE_COUNT, INVALID_ANONYMOUS_LINE_COUNT, VALID_RAW_LINE_COUNT, ERROR_GEOIP_ISP_DAT_URI_MISSING, ERROR_GEOIP_ORG_DAT_URI_MISSING, DEBUG_COUNTER };
    public static String ANONYMIZED_PREFIX = "anonymized";
    public static String RAW_PREFIX = "raw";
    public static String ERROR_PREFIX = "error";
    //public static String DISTRIBUTED_CACHE_URI = "hdfs://node3.admin.mango.metrics.scl3.mozilla.com:8020/user/aphadke/maxmind-2013-07/";
    public static String DISTRIBUTED_CACHE_URI = "/user/metrics-etl/maxmind/";
    public static String GEOIP_CITY_DAT = "GeoIPCity.dat";
    public static String GEOIP_ORG_DAT = "GeoIPOrg.dat";
    public static String GEOIP_DOMAIN_DAT = "GeoIPDomain.dat";
    public static String GEOIP_ISP_DAT = "GeoIPISP.dat";
    public static String DOMAIN_NAME = "DOMAIN_NAME";

    public static class MangoLogsInMapCollectionMapper extends Mapper<Object, Text, Text, Text> {
        //public static final Log LOG =  LogFactory.getLog("ReadLzoFile"); 
        private String input_fname, domain_name;
        private String[] splitSlash;
        private FileSplit fileSplit;
        private CheckLogLine cll;

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
        private int REDUCER_COUNT; 
        private LogLine logline;
        /**
         * runs before starting every mapper
         * useful to get the file split name from the mapper
         */



        public void setup (Context context) {
            cll = new CheckLogLine();
            fileSplit = (FileSplit)context.getInputSplit();
            splitSlash = fileSplit.getPath().toString().split("/");
            input_fname = splitSlash[splitSlash.length - 2];
            domain_name = splitSlash[splitSlash.length - 1];

            mos = new MultipleOutputs<Text, Text>(context);

            context.getCounter(LOG_PROGRESS.SETUP_CALLS).increment(1);
            try {
                localFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
                domain_name = context.getConfiguration().get(DOMAIN_NAME);
                REDUCER_COUNT = Integer.parseInt(context.getConfiguration().get("REDUCER_COUNT"));
                
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
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            context.getCounter(LOG_PROGRESS.MAPPER_LINE_COUNT).increment(1);
            String v = cll.logLine(fileSplit.toString(), value.toString());
            validAnonymizedLine = true;
            try {
                logline = new LogLine(v, domain_name);
                if (StringUtils.equals(domain_name, "marketplace.mozilla.org")) {
                    context.getCounter(LOG_PROGRESS.DEBUG_COUNTER).increment(1);
                }
                splitTab = new Vector<String>();
                if (logline.getSplitCount() > 0) {
                    context.getCounter(LOG_PROGRESS.VALID_SPLIT).increment(1);
                    context.write(new Text(RAW_PREFIX), new Text(logline.getRawTableString()));

                    if (REDUCER_COUNT == 0) {
                        mos.write(RAW_PREFIX, new Text(RAW_PREFIX), new Text(logline.getRawTableString()));
                    }

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

                    if (!logline.addFilename(input_fname)) { //value.toString()
                        validAnonymizedLine = false;
                    }

                    if (validAnonymizedLine) {

                        if (logline.checkOutputFormat()) {
                            if (REDUCER_COUNT == 0) {
                                mos.write(ANONYMIZED_PREFIX, new Text(ANONYMIZED_PREFIX), new Text(logline.getOutputLine()));
                            }
                            else {
                                context.write(new Text(ANONYMIZED_PREFIX), new Text(logline.getOutputLine()));
                            }

                            context.getCounter(LOG_PROGRESS.VALID_ANONYMOUS_LINE_COUNT).increment(1);

                        } else {
                            validAnonymizedLine = false;
                            context.getCounter(LOG_PROGRESS.INVALID_ANONYMOUS_SPLIT_COUNT).increment(1);

                        }

                    } else {
                        context.getCounter(LOG_PROGRESS.INVALID_ANONYMOUS_LINE_COUNT).increment(1);

                    }

                } else {
                    validAnonymizedLine = false;
                    if (v.contains("\"  \" - 0 \"-\" \"-\" \"-\"")) {
                        context.getCounter(LOG_PROGRESS.ZERO_SIZED_HTTP_REQUEST).increment(1);
                    } else {
                        context.getCounter(LOG_PROGRESS.INVALID_SPLIT).increment(1);
                    }
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (REDUCER_COUNT == 0) {
                    mos.write(ERROR_PREFIX, new Text(ERROR_PREFIX), new Text(value));
                } else {
                    context.write(new Text(ERROR_PREFIX), new Text(value));
                }


            }
        }
        protected void cleanup(Context context) throws IOException, InterruptedException {
            mos.close();
        }

    }

    /**
     * The reducer class of WordCount
     */
    public static class MangoLogsInMapCollectionReducer extends
    Reducer<Text, Text, Text, Text> {
        private MultipleOutputs<Text, Text> mos;

        public void setup (Context context) {
            mos = new MultipleOutputs<Text, Text>(context);		

        }

        public void reduce(Text key, Iterable<Text> values,
                Context context) {

            for (Text v : values) {
                try {
                    mos.write(key.toString(), new Text(key), new Text(v));
                    context.write(new Text(key), new Text(v));
                } catch (InterruptedException ie) {
                    System.err.println(ie.getMessage());
                    context.getCounter(LOG_PROGRESS.REDUCER_COUNT_IE).increment(1);	
                } catch (IOException io) {
                    System.err.println(io.getMessage());
                    context.getCounter(LOG_PROGRESS.REDUCER_COUNT_IO).increment(1);
                }

            }


        }
        protected void cleanup(Context context) throws IOException, InterruptedException {
            mos.close();
        }


    }

    public static String getJobDate(String input) {
        // /user/aphadke/tmp/temp_intermediate_raw_anon_logs-addons.mozilla.org-2013-06-03/
        String[] splitSlash = StringUtils.split(input, "/");
        if (splitSlash.length > 0) {
            String[] splitDash = StringUtils.split(splitSlash[3],"-");
            return (splitDash[2] + "-" + splitDash[3] + "-" + splitDash[4]);
        }


        return "";
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
        c.set(DOMAIN_NAME, args[3]);
        int reducerCount = Integer.parseInt(args[2]);
        c.set("REDUCER_COUNT", reducerCount + "");
        DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_CITY_DAT), c);
        DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_DOMAIN_DAT), c);
        DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_ISP_DAT), c);
        DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + GEOIP_ORG_DAT), c);
        DistributedCache.addCacheFile(new URI(DISTRIBUTED_CACHE_URI + "regexes.yaml"), c);

        Job job;
        job = new Job(c, "backfill-" + args[0] + "-" + args[1]);

        job.setJarByClass(MangoLogsInMapCollection.class);

        job.setMapperClass(MangoLogsInMapCollectionMapper.class);
        job.setReducerClass(MangoLogsInMapCollectionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setJobName("Logs: " + args[3] + ":" + getJobDate(args[1]));
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);	

        FileInputFormat.addInputPath(job, new Path(args[0]));
        SequenceFileOutputFormat.setOutputPath(job, new Path(args[1]));

        MultipleOutputs.addNamedOutput(job, ANONYMIZED_PREFIX, SequenceFileOutputFormat.class , Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, RAW_PREFIX, SequenceFileOutputFormat.class , Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, ERROR_PREFIX, SequenceFileOutputFormat.class , Text.class, Text.class);

    
        job.setNumReduceTasks(reducerCount);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}






