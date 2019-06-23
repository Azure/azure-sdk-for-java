# Azure Event Hubs client library for Java

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream
them to multiple consumers. This lets you process and analyze the massive amounts of data produced by your connected
devices and applications. Once Event Hubs has collected the data, you can retrieve, transform, and store it by using any
real-time analytics provider or with batching/storage adapters. If you would like to know more about Azure Event Hubs,
you may wish to review: [What is Event Hubs](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-about)?

The Azure Event Hubs client library allows for publishing and consuming of Azure Event Hubs events and may be used to:

- Emit telemetry about your application for business intelligence and diagnostic purposes.
- Publish facts about the state of your application which interested parties may observe and use as a trigger for taking
  action.
- Observe interesting operations and interactions happening within your business or other ecosystem, allowing loosely
  coupled systems to interact without the need to bind them together.
- Receive events from one or more publishers, transform them to better meet the needs of your ecosystem, then publish
  the transformed events to a new stream for consumers to observe.

[Source code][source_code] | [Package (Maven) (coming soon)][package] | [API reference documentation][api_documentation]
| [Product documentation][event_hubs_product_docs]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Obtain a connection string

For the Event Hubs client library to interact with an Event Hub, it will need to understand how to connect and authorize
with it. The easiest means for doing so is to use a connection string, which is created automatically when creating an
Event Hubs namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the
step-by-step guide to [get an Event Hubs connection string][event_hubs_connection_string].

### Create an Event Hub client

Once the Event Hub and connection string are available, they can be used to create a client for interacting with Azure
Event Hubs. Create an `EventHubClient` using the `EventHubClientBuilder`:

```java
String connectionString = "<< CONNECTION STRING FOR THE EVENT HUBS NAMESPACE >>";
String eventHubName = "<< NAME OF THE EVENT HUB >>";
EventHubClientBuilder builder = new EventHubClientBuilder()
    .connectionString(connectionString, eventHubName);
EventHubClient client = builder.build();
```

## Key concepts

The concepts for AMQP are well documented in [OASIS Advanced Messaging Queuing Protocol (AMQP) Version
1.0](http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html).

## Examples

## Troubleshooting

## Next steps

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft
Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- Links -->
[api_documentation]: https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html
[event_hubs_connection_string]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
[event_hubs_create]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create
[event_hubs_product_docs]: https://docs.microsoft.com/en-us/azure/event-hubs/
[maven]: https://maven.apache.org/
[package]:not-valid-link
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/eventhubs/client/