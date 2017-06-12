# Finam data loader

Loads a data from Finam export service (in append mode).
Used for a building of a continuous asset price history.

## Requirements

* Java8
* Maven3

## Building

```bash
mvn clean package
```

## Running
```bash
java -jar target/finam-data-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.ontslab.data.finam.app.Application /home/sherman/si-5
```

