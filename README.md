# log-monitoring

## How to navigate this repository?
* This **README** contains instructions on how to build and run the log-monitoring application.
* The application **code** can found under `/src/main/java`.
* The application **tests** can be found under `/src/main/test`.
* If you want to know more about **thought process** I followed to come up with a solution before I wrote any code, you can read [this](/discovery/README.md).
* If you want to know more about the **design** of the log-monitoring application, you can read [this](/design/README.md).
* If you want to hear some ideas on how **improve this app** even further, you can read [this](/future/README.md).

## How to run the app?
### Setup steps
Before you can run this application, you will need to have installed:
* **Docker**. Follow the instructions from [here](https://docs.docker.com/get-docker/).
* **JDK 11**. Download from [here](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html) and follow [these instructions](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/windows-7-install.html) to install.
* **Maven**. Donwload from [here](https://maven.apache.org/download.cgi) and follow [these instructions](https://maven.apache.org/install.html) to install.
* **InfluxDB**. Download InfluxDB Docker image by running `docker pull influxdb:1.8.10`

### Build
Go to the root folder and run.

```bash
mvn clean install
```

### Run
Once you have built the package, go to the `/target` folder and run:

```bash
java -jar log-monitoring.jar --log <path to csv file>
```

Also, if you want to configure the alert threshold, you can run:

```bash
java -jar log-monitoring.jar --log <path to csv file> --alert_threshold <new_threshold>
```

## TODO
### MUST
- [ ] Write a test for the alerting logic
- [ ] Unit tests
- [ ] Design: Explain the use of Testcontainers
- [ ] Future: alert conditions, sensitivity
- [ ] Grammar check
- [ ] Package and send

