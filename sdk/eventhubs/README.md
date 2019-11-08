<p align="center">
  <img src="event-hubs.png" alt="Microsoft Azure Event Hubs" width="100"/>
</p>

<h1 align="center">Microsoft Azure Event Hubs Client for Java
<p align="center">
  <a href="#star-our-repo">
        <img src="https://img.shields.io/github/stars/azure/azure-event-hubs-java.svg?style=social&label=Stars"
            alt="star our repo"></a>
  <a href="https://twitter.com/intent/follow?screen_name=azureeventhubs">
        <img src="https://img.shields.io/twitter/url/http/shields.io.svg?style=social&label=Follow%20@azureeventhubs"
            alt="follow on Twitter"></a>
</p></h1>

|Branch|Status|
|------|-------------|
|master|[![Build status](https://ci.appveyor.com/api/projects/status/dq8qyu2k3wu2uexd/branch/master?svg=true)](https://ci.appveyor.com/project/sabeegrewal/azure-event-hubs-java/branch/master)|
|dev|[![Build status](https://ci.appveyor.com/api/projects/status/dq8qyu2k3wu2uexd/branch/dev?svg=true)](https://ci.appveyor.com/project/sabeegrewal/azure-event-hubs-java/branch/dev)|

Azure Event Hubs is a hyper-scale data ingestion service, fully-managed by Microsoft, that enables you to collect, store and process trillions of events from websites, apps, IoT devices, and any stream of data.

Refer to the [online documentation](https://azure.microsoft.com/services/event-hubs/) to learn more about Event Hubs in general and [General Overview document](Overview.md) for an overview of Event Hubs Client for Java.

## Using the library

### Samples

Code samples are [here](https://github.com/Azure/azure-event-hubs/tree/master/samples/Java).

### Referencing the library

Two java packages are released to Maven Central Repository from this GitHub repository.

#### Microsoft Azure EventHubs Java Client

This library exposes the send and receive APIs. This library will in turn pull further required dependencies, specifically
the required versions of Apache Qpid Proton-J, and the cryptography library BCPKIX by the Legion of Bouncy Castle.

|Package|Package Version|
|--------|------------------|
|azure-eventhubs|[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs)

```XML
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>azure-eventhubs</artifactId>
        <version>2.3.1</version>
    </dependency>
```

#### Microsoft Azure EventHubs Java Event Processor Host library

This library exposes an out-of-the-box distributed partition processor for Event Hubs.
It pulls the required versions of Event Hubs, Azure Storage and GSon libraries.

|Package|Package Version|
|--------|------------------|
|azure-eventhubs-eph|[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs-eph/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs-eph)

```XML
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>azure-eventhubs-eph</artifactId>
        <version>2.5.1</version>
    </dependency>
```

## How to provide feedback

First, if you experience any issues with the runtime behavior of the Azure Event Hubs service, please consider filing a support request
right away. Your options for [getting support are enumerated here](https://azure.microsoft.com/support/options/). In the Azure portal,
you can file a support request from the "Help and support" menu in the upper right hand corner of the page.

If you find issues in this library or have suggestions for improvement of code or documentation, you can [file an issue in the project's
GitHub repository](https://github.com/Azure/azure-event-hubs/issues) or send across a pull request - see our [Contribution Guidelines](./.github/CONTRIBUTING.md).

Issues related to runtime behavior of the service, such as sporadic exceptions or apparent service-side performance or reliability issues can not be handled here.

Generally, if you want to discuss Azure Event Hubs or this client library with the community and the maintainers, you can turn to
[stackoverflow.com under the #azure-eventhub tag](http://stackoverflow.com/questions/tagged/azure-eventhub) or the
[MSDN Service Bus Forum](https://social.msdn.microsoft.com/Forums/en-US/home?forum=servbus).

## Build & contribute to the library

You will generally not have to build this client library yourself - this library is available on maven central.
If you have any specific requirement for which you want to contribute or need to generate a SNAPSHOT version, this section is for you.
**Your contributions are welcome and encouraged!**

We adopted maven build model and strive to keep the project model intuitive enough to developers.
If you need any help with any specific IDE or cannot get the build going in any environment - please open an issue.
Here are few general topics, which we thought developers would need help with:

### Running Integration tests

Set the following two Environment variables to be able to run unit tests targeting Microsoft Azure EventHubs service:

  * EVENT_HUB_CONNECTION_STRING - the event hub connection string to which the tests should target. the format of the connection string is: `Endpoint=----NAMESPACE_ENDPOINT------;EntityPath=----EVENTHUB_NAME----;SharedAccessKeyName=----KEY_NAME----;SharedAccessKey=----KEY_VALUE----`. [Here's how to create an Event Hub on Azure Portal and get the connection string](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create).

  * EPHTESTSTORAGE - the Microsoft Azure Storage account connection string to use while running EPH tests. The format of the connection string is: `DefaultEndpointsProtocol=https;AccountName=---STORAGE_ACCOUNT_NAME---;AccountKey=---ACCOUNT_KEY---;EndpointSuffix=---ENPOINT_SUFFIX---`. For more details on this visit - [how to create an Azure Storage account connection string](https://docs.microsoft.com/en-us/azure/storage/common/storage-configure-connection-string#create-a-connection-string-for-an-azure-storage-account).

### Explore the client library with IDEs

* If you see any Build Errors - make sure the Execution Environment is set to JDK version 1.8 or higher



![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2FREADME.png)
