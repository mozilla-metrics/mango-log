#!/usr/bin/perl

#USAGE
#perl processDailyDumps.pl 1 10
#arg[0] = 1 == test
#arg[1] = 10 == number of ooids to process
$daysToProcess = 2;
$wgetLocation = "/usr/bin/wget";
$gunzipLocation = "/bin/gunzip";
$tempLocation = "../temp/";
$outputLocation = "../dailyDumps/";
$cutLocation = "/bin/cut";

mkdir($tempLocation);
mkdir($outputLocation);
$testRun = $ARGV[0];
$numberOfLines = $ARGV[1];
for ($i = 1; $i < $daysToProcess; $i++) {
	chdir("/home/aphadke/code/perl/");

	($Second, $Minute, $Hour, $Day, $Month, $Year, $WeekDay, $DayOfYear, $IsDST) = localtime (time() - 86400 * $i);
	$Year += 1900;
	$Month+= 1;
	#http://people.mozilla.com/crash_analysis/20100502/20100502-pub-crashdata.csv.gz
	$dateTime = $Year . sprintf("%02d",$Month) . sprintf("%02d",$Day);
	print "============================================================\n";
	print "Processing for day: $dateTime\n";
	$downloadUrl = "http://people.mozilla.com/crash_analysis/" . $dateTime . "/" . $dateTime . "-pub-crashdata.csv.gz";
	$filename = "download.txt.gz";
	
	#download
	$sys_cmd = $wgetLocation . " " . $downloadUrl . " -O " . $tempLocation . $filename;
	print $sys_cmd . "\n";
	system($sys_cmd);
	
	#unzip
	$sys_cmd = $gunzipLocation . " -f " . $tempLocation . $filename;
	print $sys_cmd . "\n";
	system($sys_cmd);
	
	#now start processing
	$outFile = $tempLocation . "download.txt";
	open FILE, "$outFile" or die $!;
	$outputFile = $outputLocation . $dateTime . ".txt";
	open OOIDFILE,">$outputFile";
	$linesRead = 0;
	while (<FILE>) {
		$line = $_;
		chomp($line);
		@splitTab = split("\t",$line);
		$url = $splitTab[2];
		@splitSlash = split("/",$url);
		$ooid = $splitSlash[@splitSlash - 1];
		print OOIDFILE $ooid . "\n";
		$linesRead++;
		if ($testRun == 1 && $linesRead > $numberOfLines) {
			last;
		}
	}
	close(FILE);
	close(OOIDFILE);
	
	#now delete all files from hadoop input folder
	$sys_cmd = "hadoop dfs -rm input/*";
	print $sys_cmd . "\n";
	system($sys_cmd);
	
	#now put the file inside hadoop
	$sys_cmd = "hadoop dfs -put $outputFile input/";
	print $sys_cmd . "\n";
	system($sys_cmd);
	
	#now start the hadoop job
	$sys_cmd = "hadoop dfs -rmr output";
	print $sys_cmd . "\n";
	system($sys_cmd);
	
	chdir("/home/aphadke/");
	$sys_cmd = "hadoop jar t.jar input output 50 " . $dateTime;
	print $sys_cmd . "\n";
	system($sys_cmd);

	print "Done processing for day: $dateTime\n";
	print "============================================================\n";	
	
	
}

