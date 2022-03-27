# Technical Discovery
The goal of this document is to encapsulate the reasoning behind the solution and the different options considered.

## How to read this document?
The document will be structured in 3 sections:
* **Product requirements**. This section will capture my product requirements analysis, the assumptions made based on the wording and the core problems identified. Non-functional requirements will be skipped, since this section will focus on the **what** instead of the **how**. 
* **Discovery**. Once the requirements are clear, this section captures my research in order to inform the different solutions to explore.
* **Proposal**. Finally, once the requirements are clear and there is a good understanding of the domain, this section will include a breakdown a few solutions and the trade-offs identified.

## Product requirements
After scanning the problem statement, there are 3 areas of focus:

### Data Ingestion
There are a couple of requirements that will affect how this problem can be approached:

> Avoid using system time in your program
> Try to only make only one pass over the file overall, for both the statistics and the alerting, as if you were reading it in real time.
> Consider the efficiency of your solution and how it would scale to process high volumes of log lines - don't assume you can read the entire file into memory

As mentioned, the solution won't be able to read the entire file in memory and should treat each line as events that are received over time. Additionally, the solution will have to use the timestamp included in the logs to emulate the system clock.

### Statistics reporting
This area is fairly vague in the problem statement. Therefore, we will make a few assumptions around what the statistics should include based on personal experience and the information provided in the `csv` file (remotehost, rfc931, authuser, date, request, status, bytes).

There are only a couple of requirements:
> For every 10 seconds of log lines, display stats about the traffic during those 10s: the sections of the web site with the most hits, as well as statistics that might be useful for debugging.
> Make a reasonable assumption about how to handle the 10-second intervals, there are a couple of valid options here. Make a similar assumption about how frequently stats should be displayed as you process (but don't just print them at the end!).

As a bare minimum (MVP), this report should include: 
* **RPS (requests per second)**. Since we are looking at an interval of 10 seconds, we can display the average and P99 in that interval to highlight whether the traffic is stable or spiky. 
* **HTTP status code**. This could be a count of 2xx, 3xx, 4xx and 5xx errors observed in the 10 seconds interval.

Based on time availability, there are a few areas where this could be expanded:
* We can include a breakdown per endpoint and remote host on the **RPS** statistics.
* We can include a breakdown per endpoint on the **HTTP status code** statistics.
* We can include a summary of the bytes transferred in the 10 seconds interval.
* We can include a top 5 of the users that hit the service the most in the 10 seconds interval.

### Alerting
On the alerting front, there is a bit more clarity on the problem statement:

> Whenever total traffic for the past 2 minutes exceeds a certain number on average, print a message to the console saying that “High traffic generated an alert - hits = {value}, triggered at {time}”. The default threshold should be 10 requests per second but should be configurable.

The **total traffic** wording is the key here. This will simplify our alerting logic since we won't have to look into a per endpoint granularity. 

However, the problem statement is vague on how the **alert threshold** can be configured. Therefore, we have a few options based on time availability:
* Hardcode the threshold in the code.
* Include it as a command line parameter.
* Define the alerting thresholds in a `JSON` file. This build on the idea of **alerting as code**, which is a pattern that facilitates the management of alerts since it enables the tracking alerts in source control and building deployment automation around it.

> Whenever the total traffic drops again below that value on average for the past 2 minutes, print another message detailing when the alert recovered, +/- a second.
> The time in the alert message can be formatted however you like (using a timestamp or something more readable are both fine), but the time cited must be in terms of when the alert or recovery was triggered in the log file, not the current time.

Again, here the problem statement is reenforcing the idea of emulating the system clock based on the logs as highlighted in the **Data ingestion** section.

> Duplicate alerts should not be triggered - a second alert should not be triggered before a recovery of the first.

This means that we will have to keep track of the alerts that are currently active.

> The alerting state does not need to persist across program runs

Finally, this means that every time the app runs it should produce the same or very similar results.

## Discovery
### HTTP access log
In order to understand the type of data that the application will be processed, I spent some time analysing the `csv` file provided.

The first issue that was encountered was the fact that the events can be out of order. Example:

```
"10.0.0.5","-","apache",1549573860,"GET /api/help HTTP/1.0",200,1234
"10.0.0.4","-","apache",1549573859,"GET /api/help HTTP/1.0",200,1234
"10.0.0.5","-","apache",1549573860,"POST /report HTTP/1.0",500,1307
```

This has an implication in both, the **statistics and the alerting accuracy**, since there isn't a deterministic way to know when all the events for a particular second (UNIX timestamps) will be processed.

There are a few paths we could take:
* **Ignore out of order events**. This is the simplest solution but it has the potential of missing alerts that should be fired when the total traffic is very close to the threshold.
* **Delay the reporting of statistics/alerts by a *well defined* number of seconds**. This will reduce the risk of missing alerts that are close to the threshold at the cost of delaying the alert firing, which has an impact on the ability to respond to the alert quickly.

In order to facilidate the decision, I have assumed that the `csv` file contains a fair representation of what the real-life events will be like for this application and wrote a few scripts to understand the impact of either of the options highlighted above.

**Ignoring out of order events**
The `ignored.py` script iterates through the `csv` file, increasing the internal timestamp every time a higher value is observed and tracking the number of events that would be ignored.

For this particular `csv` file, there are a total of `4831` events from which `2817` would be ignored following this approach. In other words, 58.3% of the events would be dropped if we follow this approach.

**Delay the reporting of statistics/alerts by a *well defined* number of seconds**
In order to determine the right number of seconds that we should delay the reporting of statistics and alerts to increase the overall accuracy, I have written another script (`delay.py`) that will ...

TBD

## Proposal
### Option A
TBD

### Option B
TBD