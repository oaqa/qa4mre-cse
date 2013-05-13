#!/usr/bin/python

# generate per-trace evaluation result summary for qa4mre
# author: Zi Yang (ziy@)

import sys
import os
import re
from collections import defaultdict


TRACE_PATTERN = re.compile(r"\[(\d+)\] Executing option: (.*) on trace (.*)")
ACCURACY_PATTERN = re.compile(r"Correct: (\d+)/(\d+)=.*")
CAT1_PATTERN = re.compile(r"c@1 score:([\d.]+)")


def generate_summary(cse_log_file):
  trace2correct = defaultdict(lambda: 0)
  trace2total = defaultdict(lambda: 0)
  trace2unanswer = defaultdict(lambda: 0)
  for line in open(cse_log_file):
    m = TRACE_PATTERN.match(line.strip())
    if m:
      id = m.group(1)
      idx = len(m.group(3).split('>')) + 1
      trace = m.group(3) + '>' + str(idx) + "|" + m.group(2)
      continue
    m = ACCURACY_PATTERN.match(line.strip())
    if m:
      correct, total = int(m.group(1)), int(m.group(2))
      trace2correct[trace] += correct
      trace2total[trace] += total
      continue
    m = CAT1_PATTERN.match(line.strip())
    if m:
      unanswer = int((float(m.group(1)) * total - correct) * total / correct)
      trace2unanswer[trace] += unanswer
      continue
  for trace in trace2correct:
    accuracy = float(trace2correct[trace]) / float(trace2total[trace])
    cat1 = float(trace2correct[trace] + trace2unanswer[trace] * accuracy) \
           / float(trace2total[trace])
    print '%s,%f,%f' % (trace, accuracy, cat1)


def main():
  args = sys.argv[1:]
  if "-h" in args or "--help" in args:
    usage()
    sys.exit(2)
  generate_summary(args[0])


def usage():
  print "Usage:  %s cse_log_file" % os.path.basename(sys.argv[0])


if __name__=='__main__':
  main()

