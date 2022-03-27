import csv
import sys

delay = int(sys.argv[1]) if len(sys.argv) > 1 and sys.argv[1] else 0

print(f"Allowed delay {delay}")

with open('http_log.csv', mode ='r' ) as file:
  csvFile = csv.DictReader(file)

  timestamp = 0
  ignored = 0

  for lines in csvFile:
    newTimestamp = int(lines["date"])
    if newTimestamp > timestamp + delay:
      timestamp = newTimestamp - delay
    
    if (newTimestamp < timestamp):
      print(f"Ignoring entry with timestamp {newTimestamp}. Current timestamp {timestamp}")
      ignored += 1

  print(f"Total {csvFile.line_num}. Ignored {ignored}. Percentage: {ignored / csvFile.line_num}")