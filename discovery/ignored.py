import csv
from time import time

with open('http_log.csv', mode ='r' ) as file:
  csvFile = csv.DictReader(file)

  timestamp = 0
  ignored = 0

  for lines in csvFile:
    newTimestamp = int(lines["date"])
    if newTimestamp > timestamp:
      timestamp = newTimestamp
    elif (newTimestamp < timestamp):
      ignored += 1

  print(f"Total {csvFile.line_num}. Skipped {ignored}. Percentage: {ignored / csvFile.line_num}")