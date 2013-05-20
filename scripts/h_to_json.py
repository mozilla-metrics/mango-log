#!/usr/bin/python
import subprocess, re, sys, json, math

CC = "cl"
CPP_FLAG = "-E"
INPUT_FILE = "TelemetryHistograms.h"
if len(sys.argv) == 4:
    [_, CC, CPP_FLAG, INPUT_FILE] = sys.argv

HGRAM_RE = re.compile(r"HISTOGRAM\(\s*([^, ]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^, ]+)\s*,\s*\"((?!HISTOGRAM).+)\"\s*\)\s*$")
UNIT_RE = re.compile(r"\(([^()]+)\)$")

def exp_buckets(declared_min, declared_max, bucket_count):
    log_max = math.log(declared_max);
    bucket_index = 2;
    ret_array = [0]
    current = declared_min
    ret_array.append(current)
    for bucket_index in range(2, bucket_count):
        log_current = math.log(current)
        log_ratio = (log_max - log_current) / (bucket_count - bucket_index)
        log_next = log_current + log_ratio
        next_value = int(math.floor(math.exp(log_next) + 0.5))
        if next_value > current:
            current = next_value
        else:
            current = current + 1
        ret_array.append(current)
    assert len(ret_array) == bucket_count
    return ret_array

def linear_buckets(declared_min, declared_max, bucket_count):
    if bucket_count == 2:
        return [0, declared_max]
    ret_array = [0]
    declared_min = float(declared_min)
    declared_max = float(declared_max)
    for i in range(1, bucket_count):
        linear_range = (declared_min * (bucket_count - 1 - i) + declared_max * (i - 1)) / (bucket_count - 2)
        ret_array.append(int(linear_range + 0.5))
    assert len(ret_array) == bucket_count
    return ret_array

class Histograms:
    def __init__(self):
        self.hgrams = {}

    def add(self, name, min, max, bucket_count, type, comment):
        try:
            min = eval(min)
            max = eval(max)
            bucket_count = eval(bucket_count)
        except SyntaxError:
            sys.stderr.write("Could not parse %s in %s\n" % (bucket_count, name));
            return False
        obj = {'min':min,
               'max':max,
               'bucket_count':bucket_count,
               'type':type,
               }
        labels = UNIT_RE.search(comment);
        if labels != None:
            labels_str = labels.group(1)
            labels = labels_str.split(",")
            comment = re.sub(UNIT_RE, "", comment).rstrip()
            if len(labels) == 1:
                obj['unit'] = labels[0]
            else:
                if len(labels) != bucket_count:
                    sys.stderr.write("Units(%s) do not match bucket_count(%d) in %s" % (labels_str, bucket_count, name));
                    return False
                obj['labels'] = labels
        comment = comment.replace('" "',"")
        obj['comment'] = comment
        if type == "EXPONENTIAL":
            obj['buckets'] = exp_buckets(min, max, bucket_count)
        # boolean histograms are a bit of a hack in C++, emulate it by adding a dead 3rd bucket
        elif type == "BOOLEAN" or type == "FLAG":
            obj['bucket_count'] = 3
            obj['max'] = 2
            obj['min'] = 1
            obj['buckets'] = [0, 1, 2]
        else:
            obj['buckets'] = linear_buckets(min, max, bucket_count)
        self.hgrams[name] = obj
        #   if (name == 'HTTP_PAGE_DNS_ISSUE_TIME'):
        print([name, obj])
        return True

    def get(self):
        return self.hgrams

    def parse(self, file):
        txt = subprocess.check_output([CC, CPP_FLAG, file])
        # stick histograms on multiple lines
        txt = txt.replace(") HISTOGRAM(", ")\nHISTOGRAM(");
        lines = txt.split("\n")
        success = True
        for line in lines:
            line = line.strip()
            if len(line) == 0 or line[0] == '#':
                continue
            find_match = re.search(HGRAM_RE, line)
            if (find_match is not None):
                success = self.add(*HGRAM_RE.match(line).groups()) and success
            
#            success = self.add(*HGRAM_RE.match(line).groups()) and success
        return success

def main():
    hgrams = Histograms()
    success = hgrams.parse(INPUT_FILE)
    f = open('reference.json', 'w')
    f.write(json.dumps({'histograms':hgrams.get()}))
    exit(int(not success))

main()

