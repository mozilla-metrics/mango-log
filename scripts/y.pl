#!/usr/bin/perl

#!/bin/bash

$sys_cmd = "curl \"http://mxr.mozilla.org/mozilla-central/source/toolkit/components/telemetry/TelemetryHistograms.h?raw=1\" | grep -v '^#\\(if\\|elif\\|end\\)' > TelemetryHistograms.h";


#$sys_cmd = "wget \"http://mxr.mozilla.org/mozilla-central/source/toolkit/components/telemetry/TelemetryHistograms.h?raw=1\" -O TelemetryHistograms.h";
print $sys_cmd . "\n";
system($sys_cmd);

$sys_cmd = "python2.7 h_to_json.py gcc -E TelemetryHistograms.h";
print $sys_cmd . "\n";
system($sys_cmd);

$sys_cmd = "scp reference.json admin4.generic.metrics.scl3.mozilla.com:/home/aphadke/";
print $sys_cmd . "\n";
system($sys_cmd);


