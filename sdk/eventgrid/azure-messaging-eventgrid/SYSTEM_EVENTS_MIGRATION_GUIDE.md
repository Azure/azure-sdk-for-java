# Migration Guide: System Events from azure-messaging-eventgrid to azure-messaging-eventgrid-systemevents

This guide is intended to assist in the migration of system event handling code from the deprecated system events in `azure-messaging-eventgrid` to the new dedicated `azure-messaging-eventgrid-systemevents` package.

## Table of contents

- [Migration Guide: System Events from azure-messaging-eventgrid to azure-messaging-eventgrid-systemevents](#migration-guide-system-events-from-azure-messaging-eventgrid-to-azure-messaging-eventgrid-systemevents)
  - [Table of contents](#table-of-contents)
  - [Migration benefits](#migration-benefits)
  - [Important changes](#important-changes)
    - [Package separation and dependency management](#package-separation-and-dependency-management)
    - [Import statement changes](#import-statement-changes)
    - [System event names and constants](#system-event-names-and-constants)
    - [Event deserialization](#event-deserialization)
  - [Step-by-step migration](#step-by-step-migration)
    - [Step 1: Update dependencies](#step-1-update-dependencies)
    - [Step 2: Update import statements](#step-2-update-import-statements)
    - [Step 3: Update SystemEventNames usage](#step-3-update-systemeventnames-usage)
    - [Step 4: Test your changes](#step-4-test-your-changes)
  - [Migration examples](#migration-examples)
    - [Example 1: Basic event handling](#example-1-basic-event-handling)
    - [Example 2: Event filtering and processing](#example-2-event-filtering-and-processing)
  - [Additional resources](#additional-resources)

## Migration benefits

The separation of system events into a dedicated package provides several benefits:

- **Reduced dependency footprint**: Applications that only need system event models don't need to include the full Event Grid publisher client
- **Better versioning**: System event models can be versioned independently from the publisher client
- **Improved maintainability**: Clear separation of concerns between event publishing and event model definitions
- **Enhanced discoverability**: Dedicated package makes it easier to find and use system event models

## Important changes

### Package separation and dependency management

**Before (Deprecated)**:
All system events were included in the main `azure-messaging-eventgrid` package.

**After (Recommended)**:
System events are now in a separate `azure-messaging-eventgrid-systemevents` package.

### Import statement changes

**Before (Deprecated)**:
```java
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.SystemEventNames;
```

**After (Recommended)**:
```java
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.SystemEventNames;
```

### System event names and constants

The `SystemEventNames` class has moved from the main package to the system events package:

**Before (Deprecated)**:
```java
import com.azure.messaging.eventgrid.SystemEventNames;

if (SystemEventNames.STORAGE_BLOB_CREATED.equals(event.getEventType())) {
    // Handle blob created event
}
```

**After (Recommended)**:
```java
import com.azure.messaging.eventgrid.systemevents.SystemEventNames;

if (SystemEventNames.STORAGE_BLOB_CREATED.equals(event.getEventType())) {
    // Handle blob created event
}
```

### Event deserialization

Event deserialization patterns remain the same, only import statements change:

**Before (Deprecated)**:
```java
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;

StorageBlobCreatedEventData eventData = event.getData().toObject(StorageBlobCreatedEventData.class);
```

**After (Recommended)**:
```java
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;

StorageBlobCreatedEventData eventData = event.getData().toObject(StorageBlobCreatedEventData.class);
```

## Step-by-step migration

### Step 1: Update dependencies

Add the new system events dependency to your project:

**Maven:**
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-systemevents</artifactId>
    <version>1.0.0</version>
</dependency>
```

Note: If you're only consuming events and not publishing them, you can remove the main `azure-messaging-eventgrid` dependency. If you need to publish events, keep both dependencies.

### Step 2: Update import statements

Replace all system event imports to use the new package:

**Find and replace across your codebase:**

1. Replace `import com.azure.messaging.eventgrid.SystemEventNames;` 
   with `import com.azure.messaging.eventgrid.systemevents.SystemEventNames;`

2. Replace all system event data class imports:
   - From: `import com.azure.messaging.eventgrid.systemevents.*EventData;`
   - To: `import com.azure.messaging.eventgrid.systemevents.models.*EventData;`

### Step 3: Update SystemEventNames usage

Update any usage of the `SystemEventNames` class to import from the new package:

**Before:**
```java
import com.azure.messaging.eventgrid.SystemEventNames;
```

**After:**
```java
import com.azure.messaging.eventgrid.systemevents.SystemEventNames;
```

### Step 4: Test your changes

After making the changes, ensure your application still compiles and runs correctly:

1. Build your project to verify there are no compilation errors
2. Run your existing tests to ensure functionality is preserved
3. Verify that event deserialization still works as expected

## Migration examples

### Example 1: Basic event handling

**Before (Deprecated):**
```java
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.SystemEventNames;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;

public void handleEvent(EventGridEvent event) {
    if (SystemEventNames.STORAGE_BLOB_CREATED.equals(event.getEventType())) {
        StorageBlobCreatedEventData eventData = event.getData()
            .toObject(StorageBlobCreatedEventData.class);
        System.out.println("Blob created: " + eventData.getUrl());
    }
}
```

**After (Recommended):**
```java
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.SystemEventNames;
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;

public void handleEvent(EventGridEvent event) {
    if (SystemEventNames.STORAGE_BLOB_CREATED.equals(event.getEventType())) {
        StorageBlobCreatedEventData eventData = event.getData()
            .toObject(StorageBlobCreatedEventData.class);
        System.out.println("Blob created: " + eventData.getUrl());
    }
}
```

## Additional resources

- [Azure Event Grid System Events Package README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid-systemevents/README.md)
- [Azure Event Grid Documentation](https://docs.microsoft.com/azure/event-grid/)
- [Azure Event Grid System Events Reference](https://docs.microsoft.com/azure/event-grid/system-topics)
- [Azure SDK for Java Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)

<!-- Links -->
[azure-messaging-eventgrid]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid
[azure-messaging-eventgrid-systemevents]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid-systemevents
