#!/usr/bin/perl

#chdir("~/workspace/crash-reports/");
#perl build.pl ei.jar com.mozilla.main.ExecutableInvoker
#hadoop dfs -rmr output ; hadoop jar t.jar jsonz_input output
#chdir("../");
$jarFolder = "jars/";
$jarName = $jarFolder . $ARGV[0];
$className = $ARGV[1];
$serverName = $ARGV[2];
if (length($serverName) == 0) {
	$serverName = "cm-hadoop-dev02";
}

#using jars as folder to prevent accidental *.* :)
#clean existing jar
$sys_cmd = "rm -rf jars";
print $sys_cmd . "\n";
system($sys_cmd);

mkdir("jars");

#now build the jar
$sys_cmd = "ant -Dmainclass=$className -DpathToJar=$jarName hadoop-jar";
print $sys_cmd . "\n";
system($sys_cmd);


#now copy the jar to dev cluster
$sys_cmd = "scp $jarName aphadke\@" . $serverName . ":/home/aphadke/";
print $sys_cmd . "\n";
system($sys_cmd);
