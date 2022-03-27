import csv
from time import time

with open('http_log.csv', mode ='r' ) as file:
  csvFile = csv.DictReader(file)

  timestamp = 0
  skipped = 0

  for lines in csvFile:
    newTimestamp = int(lines["date"])
    if newTimestamp > timestamp:
      timestamp = newTimestamp
    elif (newTimestamp < timestamp):
      skipped += 1

  print(f"Total {csvFile.line_num}. Skipped {skipped}. Percentage: {skipped / csvFile.line_num}")