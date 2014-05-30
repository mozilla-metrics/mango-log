#!/bin/bash
#./udf.sh src/com/mozilla/udf/ParseDateForQtr.java && scp src/com/mozilla/udf/ParseDateForQtr.jar aphadke@peach-gw.peach.metrics.scl3.mozilla.com:/home/aphadke/

if [ "$1" == "" ]; then
   echo "Usage: $0 <java file>"
   exit 1
fi

CNAME=${1%.java}
JARNAME=$CNAME.jar
JARDIR=$CNAME
#CLASSPATH=$(ls lib/hive-serde-*.jar):$(ls lib/hive-exec-*.jar):$(ls lib/hadoop-core-*.jar):$(ls lib/maxmindgeoip.jar)
CLASSPATH=$(ls lib/hadoop-core.jar):$(ls lib/hive-common-*.jar):$(ls lib/hadoop-common-*.jar):$(ls lib/hive-contrib-*.jar):$(ls lib/hive-exec-*.jar)

function tell {
    echo
    echo "$1 successfully compiled.  In Hive run:"
    echo "$> add jar $JARNAME;"
    echo "$> create temporary function $CNAME as 'com.example.hive.udf.$CNAME';"
    echo
}

mkdir -p $JARDIR
javac -classpath $CLASSPATH -d $JARDIR/ $1 && jar -cf $JARNAME -C $JARDIR/ . && tell $1

#./udf.sh src/com/mozilla/hive/GenericUDFGeoIP.java && scp src/com/mozilla/hive/GenericUDFGeoIP.jar aphadke@etl2.metrics.scl3.mozilla.com:/home/aphadke/
#add file GeoIPCityNew.dat;
#add jar /home/aphadke/GenericUDFGeoIP.jar;
#add jar /home/aphadke/maxmindgeoip.jar;
#create temporary function g as 'com.mozilla.hive.GenericUDFGeoIP';
#select g(ip_address, 'COUNTRY_NAME',  './GeoIPCityNew.dat' ) from research_logs where ds='2012-06-05' and domain='www.mozilla.com' and ip_address != 'null' limit 10;
