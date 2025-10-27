# Azure Event Grid System Events for Java

This package contains strongly typed model classes for Azure Event Grid System Events and utilities for deserializing system event data.

## Overview

This library provides:
- **System Event Data Models**: Strongly typed classes for all Azure Event Grid system events (e.g., 
  `StorageBlobCreatedEventData`, `AppConfigurationKeyValueDeletedEventData`)
- **Event Type Constants**: Pre-defined constants for all system event types via `SystemEventNames`
- **Event Mappings**: Automatic mapping between event type strings and their corresponding data model classes

System events are published by Azure services when resources change state. For example, when a blob is created in Azure Storage, a `Microsoft.Storage.BlobCreated` event is published with `StorageBlobCreatedEventData` as the event data.

[Sources][sources] |
[API Reference Documentation][javadocs] |
[Product Documentation][service_docs] |
[Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid-systemevents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-systemevents</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

**Note**: This package contains only the system event models and utilities. To send events, you'll also need the 
main [Event Grid SDK][azure-messaging-eventgrid]:

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid</artifactId>
    <version>4.31.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

**For Event Grid namespaces**: If you're working with Event Grid namespaces, consider using the [EventGrid namespaces 
package](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid-namespaces) for 
namespace-specific functionality:

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid-namespaces;dependency})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-namespaces</artifactId>
    <version>1.1.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

**System Events**: Events automatically published by Azure services when resource state changes occur.

**Event Data Models**: Strongly typed classes in the `com.azure.messaging.eventgrid.systemevents.models` package 
that represent the `data` payload of system events.

**SystemEventNames**: Utility class containing constants for all system event types and mappings to their corresponding data model classes.

## Examples

### Available System Events

This package provides models for system events from many Azure services, including:

- **Azure App Configuration**: `AppConfigurationKeyValueDeletedEventData`, `AppConfigurationKeyValueModifiedEventData`
- **Azure Blob Storage**: `StorageBlobCreatedEventData`, `StorageBlobDeletedEventData`
- **Azure Communication Services**: `AcsCallStartedEventData`, `AcsChatMessageReceivedEventData`
- **Azure Container Registry**: `ContainerRegistryImagePushedEventData`, `ContainerRegistryImageDeletedEventData`
- **Azure Event Hubs**: `EventHubCaptureFileCreatedEventData`
- **Azure IoT Hub**: `IotHubDeviceCreatedEventData`, `IotHubDeviceTelemetryEventData`
- **Azure Service Bus**: `ServiceBusActiveMessagesAvailableWithNoListenersEventData`
- See the [Azure services that support system events](https://learn.microsoft.com/azure/event-grid/system-topics#azure-services-that-support-system-topics) for additional supported services.
For a complete list, see the `com.azure.messaging.eventgrid.systemevents.models` package.

### Working with System Events

#### 1. Get System Event Type Constants

```java readme-sample-getSystemEventTypeConstants
// Access predefined event type constants
String blobCreatedEventType = SystemEventNames.STORAGE_BLOB_CREATED;
String keyVaultSecretExpiredEventType = SystemEventNames.KEY_VAULT_SECRET_NEAR_EXPIRY;
```

#### 2. Look up Event Data Model Class

```java readme-sample-lookupSystemEventClass
// Find the appropriate model class for an event type
Class<?> eventDataClass = SystemEventNames.getSystemEventMappings().get(eventType);
if (eventDataClass != null) {
    System.out.println("Event data should be deserialized to: " + eventDataClass.getSimpleName());
}
```

#### 3. Deserialize System Event Data

```java readme-sample-deserializeSystemEventData
// Assuming you have an EventGridEvent from the main EventGrid SDK and the event is Storage Blob Created event
StorageBlobCreatedEventData storageBlobCreatedEventData
    = StorageBlobCreatedEventData.fromJson(JsonProviders.createReader("payload"));
BinaryData data = storageBlobCreatedEventData.getStorageDiagnostics().get("batchId");

System.out.println("Blob URL: " + storageBlobCreatedEventData.getUrl());
System.out.println("Blob size: " + storageBlobCreatedEventData.getContentLength());
System.out.println("Content type: " + storageBlobCreatedEventData.getContentType());

```

## Troubleshooting

### Common Issues

- **Missing Event Type**: If `SystemEventNames.getSystemEventMappings().get(eventType)` returns null, the event type might be:
  - A custom event (not a system event)
  - A new system event is not yet supported in this version
  - Misspelled event type string

- **Deserialization Errors**: Ensure you're using the correct model class for the event type. Use `SystemEventNames` mappings to get the right class.

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

- Explore the [`azure-messaging-eventgrid-systemevents`][sources] package for available event data models
- Explore the [`azure-messaging-eventgrid`][azure-messaging-eventgrid] package for sending events
- Learn about [Azure Event Grid System Topics](https://learn.microsoft.com/azure/event-grid/system-topics)
- Review [Event Grid event schemas](https://learn.microsoft.com/azure/event-grid/event-schema) for different Azure services
- Check out [Event Grid samples][samples] for complete examples
- Additional Event Grid tutorials can be found [here][service_docs]

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information, see the
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/event-grid/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[azure-messaging-eventgrid]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid
[service_docs]: https://learn.microsoft.com/azure/event-grid/
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid
[sources]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid-systemevents/src
[javadocs]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid-systemevents/src
