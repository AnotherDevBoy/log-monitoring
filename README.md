# log-monitoring

## Introduction
Explain how to navigate this repo

## How to run the app?
### Setup steps
Before you can run this application, you will need to have installed:
* **Docker**. Follow the instructions from [here](https://docs.docker.com/get-docker/).
* **JDK 11**. Download from [here](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html) and follow [these instructions](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/windows-7-install.html) to install.
* **Maven**. Donwload from [here](https://maven.apache.org/download.cgi) and follow [these instructions](https://maven.apache.org/install.html) to install.

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
- [ ] Explain design
- [ ] Explain how to improve design
- [ ] Unit tests
- [ ] Review all requirements are met

### Optional
- [ ] More statistics
- [ ] More alert types
- [ ] Test the code with a large number of events

## Follow ups
- SQS
- [ ] InfluxDB batch writing
- Alert resilience
