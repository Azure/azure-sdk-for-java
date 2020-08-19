# Feature Management Sample shared library for Java

This sample describes how to use [spring-cloud-feature-management](./../../../appconfiguration/azure-spring-cloud-feature-management/README.md) to manage features and how to get configurations from Azure Configuration Service to Spring Environment.

## Key concepts
## Getting started

### Prerequisite

* A [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable), version 8.
* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

### How to run without Azure Configuration Service
Start the application and check the resulting console output to check the returned value.

1. Load features from application.yml
```
$ mvn spring-boot:run
```

2. Check the returned value. The feature `Beta` has one filter `Random` which defines

### How to run with Azure Configuration Service

#### Prepare data

1. Create a Configuration Store if not exist.

2. Import the data file src/main/resources/data/sample-data.json into the Configuration Store created above. Under `For language` select `Other`. Under `File type` select `Yaml`.

#### Configure the bootstrap.yaml

Change the connection-string value with the Access Key value of the Configuration Store created above.

#### Run the application

Start the application and access http://localhost:8080 to check the returned value. Different commands for different scenarios are listed below.

1. Load properties similar with from application.properties, i.e., keys starting with /application/
```
$ mvn spring-boot:run
```

2. Load properties similar with from application_dev.properties, i.e., keys starting with /application_dev
```
$ mvn -Dspring.profiles.active=dev spring-boot:run
```

3. Load properties similar with from foo.properties, i.e., keys starting with /foo/
```
$ mvn -Dspring.application.name=foo spring-boot:run
```

4. Load properties similar with from foo_dev.properties, i.e., keys starting with /foo_dev/
```
$ mvn -Dspring.application.name=foo -Dspring.profiles.active=dev spring-boot:run
```

### More details

Please refer to this [README](../../azure-spring-cloud-starter-appconfiguration-config/) about more usage details. 

## Examples
## Troubleshooting
## Next steps
## Contributing
