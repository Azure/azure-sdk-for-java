# Release log of Microsoft Azure Event Hubs Client for Java

### 0.8.2

* This client update primarily targets
	- fixing a receive stuck issue in Event Hubs client `PartitionReceiveHandler.onReceive` API & `EventProcessorHost` library
* List of Issues fixed in this release: https://github.com/Azure/azure-event-hubs/milestone/7?closed=1

#### API Changes
##### Breaking Changes
* `PartitionReceiver.setReceiveHandler` - returns `CompletableFuture<Void>` (used to return `void`)

### 0.8.1

* The client update primarily involves
	- fix retrycount accounting issue in error handling code
	- modifies the experience of `PartitionReceiveHandler.onReceive` - doesn't invoke the callback with no events (default behavior change)
	- Adds client and platform information to the outgoing `AmqpConnection.Properties` to EventHubs Service
* List of Issues & PR's of this release: https://github.com/Azure/azure-event-hubs/milestone/4?closed=1

#### API Changes
##### New API
* `PartitionReceiver.setReceiveHandler(receiveHandler, invokeWhenNoEvents)`

### 0.8.0

* The release primarily targets exposing `MessageAnnotations` & `AmqpProperties` via `EventData` on a Received `AMQPMessage`.
* List of Issues & PR's of this release: https://github.com/Azure/azure-event-hubs/milestone/5?closed=1

#### API Changes
##### New API
* `EventData.getBodyOffset()` & `EventData.getBodyLength()`
* `EventData.getSystemProperties().getPublisher()`

##### Deprecated API
* ~~EventData.setProperties()~~ - use `.put()` on EventData.getProperties() instead

#### Breaking Changes
* `MessageAnnotations` on a received `AMQPMessage` are moved to `EventData.getSystemProperties()` as opposed to `EventData.getProperties()`
* `EventData.SystemProperties` class now derives from `HashSet<String, Object>`. This can break serialized EventData.