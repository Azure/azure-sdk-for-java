
# Azure Event Hubs client library for Java

> Please note, a newer package [com.azure:azure-messaging-eventhubs](https://search.maven.org/artifact/com.azure/azure-messaging-eventhubs) for [Azure Event Hubs](https://azure.microsoft.com/services/event-hubs/) is available as of February 2020. While this package will continue to receive critical bug fixes, we strongly encourage you to upgrade. Read the [migration guide](https://aka.ms/azsdk/java/migrate/eh) for more details.

Azure Event Hubs is a hyper-scale data ingestion service, fully-managed by Microsoft, that enables you to collect, store
and process trillions of events from websites, apps, IoT devices, and any stream of data.

Refer to the [online documentation](https://azure.microsoft.com/services/event-hubs/) to learn more about Event Hubs in
general and for an overview of Event Hubs Client for Java.

## Getting started

## Key concepts

- An **Event Hub producer** is a source of telemetry data, diagnostics information, usage logs, or other log data, as
  part of an embedded device solution, a mobile device application, a game title running on a console or other device,
  some client or server based business solution, or a website.

- An **Event Hub consumer** picks up such information from the Event Hub and processes it. Processing may involve
  aggregation, complex computation, and filtering. Processing may also involve distribution or storage of the
  information in a raw or transformed fashion. Event Hub consumers are often robust and high-scale platform
  infrastructure parts with built-in analytics capabilities, like Azure Stream Analytics, Apache Spark, or Apache Storm.

- A **partition** is an ordered sequence of events that is held in an Event Hub. Azure Event Hubs provides message
  streaming through a partitioned consumer pattern in which each consumer only reads a specific subset, or partition, of
  the message stream. As newer events arrive, they are added to the end of this sequence. The number of partitions is
  specified at the time an Event Hub is created and cannot be changed.

- A **consumer group** is a view of an entire Event Hub. Consumer groups enable multiple consuming applications to each
  have a separate view of the event stream, and to read the stream independently at their own pace and from their own
  position. There can be at most 5 concurrent readers on a partition per consumer group; however it is recommended
  there is only one active consumer for a given partition and consumer group pairing. Each active reader receives all
  the events from its partition; if there are multiple readers on the same partition, then they will receive duplicate
  events.

For more concepts and deeper discussion, see: 
[Event Hubs Features](https://docs.microsoft.com/azure/event-hubs/event-hubs-features). Also, the concepts for AMQP
are well documented in [OASIS Advanced Messaging Queuing Protocol (AMQP) Version 
1.0](https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html).

### Referencing the library

Two java packages are released to Maven Central Repository from this GitHub repository.

#### Microsoft Azure EventHubs Java Client

This library exposes the send and receive APIs. This library will in turn pull further required dependencies, specifically
the required versions of Apache Qpid Proton-J, and the cryptography library BCPKIX by the Legion of Bouncy Castle.

|Package|Package Version|
|--------|------------------|
|azure-eventhubs|[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-eventhubs)

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-eventhubs</artifactId>
    <version>3.2.3</version>
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

### How to provide feedback

First, if you experience any issues with the runtime behavior of the Azure Event Hubs service, please consider filing a
support request right away. Your options for [getting support are enumerated
here](https://azure.microsoft.com/support/options/). In the Azure portal, you can file a support request from the "Help
and support" menu in the upper right-hand corner of the page.

If you find issues in this library or have suggestions for improvement of code or documentation, you can [file an issue
in the project's GitHub repository](https://github.com/Azure/azure-sdk-for-java/issues) or send across a pull request -
see our [Contribution Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/CONTRIBUTING.md).

Issues related to runtime behavior of the service, such as sporadic exceptions or apparent service-side performance or
reliability issues cannot be handled here.

Generally, if you want to discuss Azure Event Hubs or this client library with the community and the maintainers, you
can turn to [stackoverflow.com under the #azure-eventhub tag](https://stackoverflow.com/questions/tagged/azure-eventhub)
or the [MSDN Service Bus Forum](https://social.msdn.microsoft.com/Forums/home?forum=servbus).

## Examples

Code samples are [here](https://github.com/Azure/azure-event-hubs/tree/master/samples/Java).

## Troubleshooting

## Next steps

## Contributing

You will generally not have to build this client library yourself - this library is available on maven central. If you
have any specific requirement for which you want to contribute or need to generate a SNAPSHOT version, this section is
**for** you. **Your contributions are welcome and encouraged!**

We adopted maven build model and strive to keep the project model intuitive enough to developers.
If you need any help with any specific IDE or cannot get the build going in any environment - please open an issue.
Here are few general topics, which we thought developers would need help with:

### Running Integration tests

Set the following two Environment variables to be able to run unit tests targeting Microsoft Azure EventHubs service:

* EVENT_HUB_CONNECTION_STRING - the event hub connection string to which the tests should target. the format of the
  connection string is:
  `Endpoint=----NAMESPACE_ENDPOINT------;EntityPath=----EVENTHUB_NAME----;SharedAccessKeyName=----KEY_NAME----;SharedAccessKey=----KEY_VALUE----`.
  [Here's how to create an Event Hub on Azure Portal and get the connection
  string](https://docs.microsoft.com/azure/event-hubs/event-hubs-create).

* EPHTESTSTORAGE - the Microsoft Azure Storage account connection string to use while running EPH tests. The format of
  the connection string is:
  `DefaultEndpointsProtocol=https;AccountName=---STORAGE_ACCOUNT_NAME---;AccountKey=---ACCOUNT_KEY---;EndpointSuffix=---ENPOINT_SUFFIX---`.
  For more details on this visit - [how to create an Azure Storage account connection
  string](https://docs.microsoft.com/azure/storage/common/storage-configure-connection-string#create-a-connection-string-for-an-azure-storage-account).

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fmicrosoft-azure-eventhubs%2FREADME.png)
