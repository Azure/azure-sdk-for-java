# Feature Management Web Sample shared library for Java

This sample describes how to use [spring-cloud-feature-management](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/spring-cloud-azure-feature-management/README.md) to manage features and how to get configurations from Azure Configuration Service to Spring Environment.

## Key concepts
## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

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

Please refer to this [README](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/spring-cloud-starter-azure-appconfiguration-config/README.md) about more usage details. 

## Examples
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
