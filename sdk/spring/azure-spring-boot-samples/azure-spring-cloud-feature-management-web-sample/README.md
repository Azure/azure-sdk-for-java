# Feature Management Web Sample shared library for Java

This sample describes how to use [spring-cloud-feature-management](./../../../appconfiguration/azure-spring-cloud-feature-management/README.md) to manage features and how to get configurations from Azure Configuration Service to Spring Environment.

## Key concepts
## Getting started

### Prerequisite

* A [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable), version 8.
* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

### How to run without Azure Configuration Service
Start the application and check the resulting console output to check the returned value.

1. Build and run the sample.

```terminal
mvn package spring-boot:run
```

1. Goto localhost:8080
1. A website will load with headers; Home, Beta, and Privacy. The Beta tab will only show when Beta is true. When the Beta tab is selected in will bring you two one of two pages BetaA or BetaB which is determined by the RandomFilter. Selecting the Privacy tab show shows how FeatureManagementSnapshot works. Again the RandomFilter is used, but each section has it called individually, but it will return the same result for each.
1. Goto localhost:8080/?User=Jeff
1. The same homepage will show, but a new tab is shown Target. The Target tab shows when the TargetingFilter returns true for the target feature flag. The TargetingFilter conditions are configured in `application.yml`. In this example the Target tab will show for users Jeff and Alicia, and groups Ring0 and Ring1. No one else will see the targeting tab.
1. Going to `TargetingContextAccessor` you will see that the current `TargetingContext` is configured to have the UserId and groups to match the `requestContext.getParameter("User");` and `requestContext.getParameter("Group");` respectively. `TargetingContextAccessor` is user defined so any method can be used to set these values.

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
