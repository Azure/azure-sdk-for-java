# Release History

## 1.1.0-beta.1 (2026-04-13)

### Features Added

* `models.AcsChatRetentionPolicy` was added

* `models.AcsChatThreadDeletedReasonType` was added

* `models.AcsChatRetentionPolicyKind` was added

#### `models.AcsRouterJobExceptionTriggeredEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsRouterJobQueuedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsRouterJobCancelledEventData` was modified

* `getTags()` was added
* `getLabels()` was added

#### `models.AcsChatMessageEventInThreadBaseProperties` was modified

* `getSequenceId()` was added

#### `models.DeviceTelemetryEventProperties` was modified

* `DeviceTelemetryEventProperties(java.util.Map)` was added

#### `models.AcsChatMessageEventBaseProperties` was modified

* `getSequenceId()` was added

#### `models.AcsChatThreadPropertiesUpdatedPerUserEventData` was modified

* `getRetentionPolicy()` was added

#### `models.AcsChatMessageReceivedEventData` was modified

* `getSequenceId()` was added

#### `models.AcsRouterJobDeletedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.IotHubDeviceTelemetryEventData` was modified

* `getProperties()` was added
* `getSystemProperties()` was added

#### `models.AcsChatMessageDeletedEventData` was modified

* `getSequenceId()` was added

#### `models.AcsRouterJobReceivedEventData` was modified

* `getTags()` was added
* `getLabels()` was added

#### `models.AcsChatTypingIndicatorReceivedInThreadEventData` was modified

* `getSequenceId()` was added

#### `models.AcsChatMessageEditedInThreadEventData` was modified

* `getSequenceId()` was added

#### `models.AcsChatAzureBotCommandReceivedInThreadEventData` was modified

* `getSequenceId()` was added

#### `models.AcsChatThreadPropertiesUpdatedEventData` was modified

* `getRetentionPolicy()` was added

#### `models.AcsRouterJobClosedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsIncomingCallEventData` was modified

* `getOnBehalfOf()` was added

#### `models.AcsChatThreadCreatedEventData` was modified

* `getRetentionPolicy()` was added

#### `models.AcsChatThreadCreatedWithUserEventData` was modified

* `getRetentionPolicy()` was added

#### `models.AcsRouterJobWaitingForActivationEventData` was modified

* `getTags()` was added
* `getLabels()` was added

#### `models.AcsChatMessageDeletedInThreadEventData` was modified

* `getSequenceId()` was added

#### `models.AcsRouterJobClassifiedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsMessageReceivedEventData` was modified

* `getFromBSUId()` was added

#### `models.AcsChatMessageEditedEventData` was modified

* `getSequenceId()` was added

#### `models.AcsRouterJobUnassignedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsRouterJobEventData` was modified

* `AcsRouterJobEventData(java.lang.String)` was added

#### `models.AcsMessageDeliveryStatusUpdatedEventData` was modified

* `getToBSUId()` was added

#### `models.AcsRouterJobCompletedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsChatThreadDeletedEventData` was modified

* `getReason()` was added

#### `models.AcsRouterJobSchedulingFailedEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsRouterJobClassificationFailedEventData` was modified

* `getTags()` was added
* `getLabels()` was added

#### `models.AcsRouterJobWorkerSelectorsExpiredEventData` was modified

* `getLabels()` was added
* `getTags()` was added

#### `models.AcsChatMessageReceivedInThreadEventData` was modified

* `getSequenceId()` was added

## 1.0.0 (2025-06-26)

### Features Added
- First stable release of the azure-messaging-eventgrid-systemevents client library for Java, providing strongly-typed models for Azure Event Grid system events.

## 1.0.0-beta.2 (2025-05-16)

### Features Added
- Added new values to `SystemEventNames` related to Azure Communication Services and Azure Edge.

## 1.0.0-beta.1 (2025-02-26)

### Features Added
- Azure SystemEvents client library for Java. This package contains Microsoft Azure SystemEvents client library.
- Initial beta release of azure-messaging-eventgrid-systemevents.
