# Release log of Microsoft Azure Event Hubs Client for Java

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