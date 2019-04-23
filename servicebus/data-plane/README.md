<p align="center">
  <img src="service-bus.png" alt="Microsoft Azure Service Bus" width="100"/>
</p>

|Build/Package|Status|
|------|-------------|
|master|[![Build status](https://ci.appveyor.com/api/projects/status/vx6o2sckac0p4jti?svg=true)](https://ci.appveyor.com/project/vinaysurya/azure-service-bus-java/branch/master) |
|dev|[![Build status](https://ci.appveyor.com/api/projects/status/vx6o2sckac0p4jti/branch/dev?svg=true)](https://ci.appveyor.com/project/vinaysurya/azure-service-bus-java/branch/dev) |
|maven|[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-servicebus/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-servicebus)|

# Microsoft Azure Service Bus Client for Java

This is the next generation Service Bus Java client library that focuses on Queues & Topics. If you are looking for Event Hubs and Relay clients, follow the below links:
* [Event Hubs](https://github.com/azure/azure-event-hubs-dotnet)
* [Relay](https://github.com/azure/azure-relay-dotnet)

Azure Service Bus is an asynchronous messaging cloud platform that enables you to send and receive messages between decoupled systems. Microsoft offers this feature as a service, which means that you do not need to host any of your own hardware in order to use it.

Refer to [azure.com](https://azure.microsoft.com/services/service-bus/) to learn more about Service Bus.

This library is build using:
* JDK / JRE 1.8
* Apache qpid - Proton J 0.22.0
* Bouncycastle - jdk15on 1.53
* Microsoft Azure - adal4j 1.3.0
* SLF4J - 1.7.0
* Junit 4.12

The package can be downloaded from [Maven](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus%22)

```
<dependency> 
  <groupId>com.microsoft.azure</groupId> 
  <artifactId>azure-servicebus</artifactId> 
  <version>1.2.8</version>
</dependency>
```

## How to provide feedback

See our [Contribution Guidelines](./.github/CONTRIBUTING.md).

## How to get support

See our [Support Guidelines](./.github/SUPPORT.md)

## Known issues

### Java client 1.0.0

There where a set of problems with message locks getting lost and then causing issues in message processing. Those issues are addressed in version 1.1.0.

### Java client 1.1.0

If you send messages from a client any other than the Java client itself in any other format than a stream you may not be able to receive the message body content. So if you are using multiple clients make sure you send and receive stream data. We are working on fixing this currently and evaluating releasing a version 1.1.1 which should contain this fix as soon as possible. If you send and receive with the Jave client you should not experience this issue.

## FAQ

### Where is the API document?
Click [here](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus/clientlibrary).

### Where can I find examples that use this library?

The samples are located in this repo. [Java Samples](https://github.com/Azure/azure-service-bus/tree/master/samples/Java).

### Can I manage Service Bus entities with this library?

Only rules management of subscription will be supported in this client library. This library focuses on Azure Service Bus Data Plane functionalities (e.g. send, receive).

The standard way to manage Azure resources is by using [Azure Resource Manager](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-overview). In order to use functionality that previously existed in the azure-servicebus Java library, this is [Azure Service Bus Management Library](https://mvnrepository.com/artifact/com.microsoft.azure/azure-mgmt-servicebus) which is available on Maven. And this is the [API document](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.management.servicebus). This management library will enable use cases that dynamically create/read/update/delete resources.

### How do I run the unit tests? 

Tests are simple JUnit tests. They can be run from the command line or any IDE that supports running JUnit tests.
Only prerequisite to running tests is setting an environment variable named 'AZURE_SERVICEBUS_JAVA_CLIENT_TEST_CONNECTION_STRING' to the connection string
 of the namespace in which the tests will create entities. The tests create entities in the namespace and run tests and delete the created entities.
And test classes also have methods to specify whether to create entities per test or once for all tests in a suite. Creating entities per test is better
as it keeps test independent of each other.

To use a proxy for unit tests, set an environment variable `RUN_WITH_PROXY` to `true`. Then set the environment variables `PROXY_HOSTNAME` and `PROXY_PORT` to your values.

#### Please see a sample using Eclipse below

1. First clone the repository to your local machine: git clone https://github.com/Azure/azure-service-bus-java.git
2. Select File > Open Projects from File System...
3. Click the Button "Directory..." and navigate to the folder in which you just cloned and select the cloned repo. Sample folder names could be "azure-service-bus-java" or just "java".
4. Import the project.
5. In the package explorer you should have two projects, navigate to java_azure-service bus and right click on it. Select Maven > Update Project...
6. Select Run > Run Configurations. Under JUnit click on + New Configuration.
7. Set the Test runner to Junit 4.
8. Select: Run all tests in the selected project, package or source folder. Click "Search..." and select: com.microsoft.azure.servicebus.
8. Go to environment and add above mentioned environment variable and the regarding connection string.
9. Click "Apply" and then "Run"
10. You should have a new view next to the package explorer called JUnit showing the running tests and see Console outputs depending on which test currently runs. If you do not see the JUnit tab go to Window > Show view > Other... > Java > JUnit
