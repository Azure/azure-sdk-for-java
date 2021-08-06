<p align="center">
  <img src="service-bus.png" alt="Microsoft Azure Service Bus" width="100"/>
</p>

# Microsoft Azure Service Bus Client for Java

> Please note, a newer package [com.azure:azure-messaging-servicebus](https://search.maven.org/artifact/com.azure/azure-messaging-servicebus) for [Azure Service Bus](https://azure.microsoft.com/services/service-bus/) is available as of December 2020. While this package will continue to receive critical bug fixes, we strongly encourage you to upgrade. Read the [migration guide](https://aka.ms/azsdk/java/migrate/sb) for more details.

This is the Java client library for Azure Service Bus that focuses on Queues & Topics. If you are looking for Event Hubs and Relay clients, follow the below links:
* [Event Hubs](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/microsoft-azure-eventhubs)
* [Relay](https://github.com/azure/azure-relay-dotnet)

Azure Service Bus is an asynchronous messaging cloud platform that enables you to send and receive messages between decoupled systems. Microsoft offers this feature as a service, which means that you do not need to host any of your own hardware in order to use it.

Refer to [azure.com](https://azure.microsoft.com/services/service-bus/) to learn more about Service Bus.

The package can be downloaded from [Maven](https://search.maven.org/artifact/com.microsoft.azure/azure-servicebus)

[//]: # ({x-version-update-start;com.microsoft.azure:azure-servicebus;current})
```
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-servicebus</artifactId>
  <version>3.6.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

## How to provide feedback

See our [Contribution Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

## How to get support

## Support resources to check prior to raising issues

1. Azure Service Bus [samples](https://github.com/Azure/azure-service-bus/tree/master/samples)
1. Already [resolved issues](https://github.com/Azure/azure-service-bus-java/issues?q=is%3Aissue+is%3Aclosed)
1. [StackOverflow](https://stackoverflow.com/questions/tagged/azureservicebus)

## Known issues

### Java client 1.0.0

There where a set of problems with message locks getting lost and then causing issues in message processing. Those issues are addressed in version 1.1.0.

### Java client 1.1.0

If you send messages from a client any other than the Java client itself in any other format than a stream you may not be able to receive the message body content. So if you are using multiple clients make sure you send and receive stream data. We are working on fixing this currently and evaluating releasing a version 1.1.1 which should contain this fix as soon as possible. If you send and receive with the Jave client you should not experience this issue.

## FAQ

### Where is the API document?
Click [here](https://docs.microsoft.com/java/api/overview/azure/servicebus?view=azure-java-legacy).

### Where can I find examples that use this library?

The samples are located in this repo. [Java Samples](https://github.com/Azure/azure-service-bus/tree/master/samples/Java).

### Can I manage Service Bus entities with this library?

Yes, this client library now has the management functionality built into it. This is made available through the [ManagementClient](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/microsoft-azure-servicebus/src/main/java/com/microsoft/azure/servicebus/management/ManagementClient.java) which now enables create, read(exists), update and delete Queues, Topics, Subscriptions, Rules.

### How do I run the unit tests?

Tests are simple JUnit tests. They can be run from the command line or any IDE that supports running JUnit tests.
Only prerequisite to running tests is setting an environment variable named 'AZURE_SERVICEBUS_CONNECTION_STRING' to the connection string
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2FREADME.png)
