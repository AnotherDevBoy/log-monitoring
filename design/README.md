# Log Monitoring Design
In this document you will find more information about how the application was designed.

The following [C4 Container diagram](https://c4model.com/) shows a high level picture of the core components of the application.

<img src="container.png" alt="C4 Container diagram" width="300"/>

## App Configuration
The app is configured via command line parameters. At the moment, there are only two parameters that can be provided:
* `log`: the path to the `csv` file containing the HTTP logs.
* `alert_threshold`: the average RPS threshold that will be used to decide when the alert should be fired or mitigated.

## Input processing
The `LogFileProcessor` reads the file line by line and notifies the event listeners, one event at a time.

The two event listeners are the internal `Clock` and the `EventProducer`.

## Internal clock
Based on the `timestamp` of every event read, the `Clock` class will estimate the application internal time. 

To do so, every time a new value that's higher than the current time stored, it will increase the time of the clock and notify any components that rely on the internal time.

The two components that rely on this internal time are the `StatisticsManager` and the `AlertManager`.

## HTTP events persistence
When the `EventProducer` receives a new event, it will notify the `EventConsumer` through an in-memory queue.

Currently, the `EventProducer` and the `EventConsumer` communicate through a `BlockingQueue` with a size of 1. 

The `EventConsumer` will read the events one by one and persist them in `InfluxDB`.

To optimize query performance for statistics and alerts, structure of the events in InfluxDB will be:
* **tags**: `path`, `status_code`, `http_method`, `id`.
* **fields**: everything else.

Since InfluxDB will consider as duplicate (and ovewrite) any events that have the exact same tags, an `id` field was introduced to force InfluxDB to treat every event as a separate entry.

This could have negative performance implications in a real-life application due to the cardinality that's introduced by the `id` field. A more performant approach could be achieved by ensuring a higher precision of the event `timestamp` (for example, in nanoseconds).

# Statistics and Alerting
Every 10 internal seconds, the `StatisticsManager` will query the `InfluxDB` database to produce:
* The Average RPS.
* The count of status code per class.

The `StatisticsManager` will rely on the `Reporter` interface to display the information back to the user. At the moment, the application uses the `ConsoleReporter` to display the information in console. With that said, the abstraction was introduced to enable different reporting approaches (email, Slack, etc).

The `AlertManager` runs every internal second to re-evaluate the state of the high traffic alert. If the alert was fired, the `AlertManager` will keep track of it in its internal state. This internal state will be used to determine whether the alert can be mitigated at some point.

On the reporting side, the `AlertManager` also relies on the `Reporter` interface to notify when the alert is fired or mitigated. 

> Note: the `EventRepository` abstraction and its in-memory implementation allowed me to test the persistence, statistics and alerting reporting before I developed the integration layer with `InfluxDB`. 

> This was particularly useful to set the baseline of how the application should behave and allowed me to compare against that when I was developing the integration with `InfluxDB`, which I wasn't familiar with before I started this exercise.

## Thread model
To separate concerns and facilitate future decoupling, some of the application components run on separate threads.

In particular, the `EventConsumer`, the `StatisticsManager` and the `AlertManager` all run on separate threads.

The `main` thread is still used to read from the `csv` file as mentioned above.

Thanks to the usage of `BlockingQueue` as a communication & synchronization mechanism between the `main` thread and the other threads, the application guarantees that a new event (and potentially a new tick) won't be produced until the previous event has been processed by all components.