# Release History

## 2.0.0-beta.1 (Unreleased)

### Features Added

- Added `MaxConcurrentOffers` to RouterWorker and CreateWorkerOptions
- Added createJobWithClassificationPolicy and createJobWithClassificationPolicyWithResponse to JobRouterClient and JobRouterAsyncClient
- Added updateXX methods that accept and return XX in JobRouterClient, JobRouterAdministrationClient, JobRouterAsyncClient and JobRouterAdministrationAsyncClient

### Breaking Changes

- Added value to RouterWorkerSelector/RouterQueueSelector constructor, remove setter
- Change return type of createJobWithResponse to RouterJob instead of BinaryData in JobRouterClient and JobRouterAsyncClient

### Bugs Fixed

- Fix Jackson annotations for all models
- JobRouterClient and JobRouterAdminClient no longer throws 401 errors when HmacAuthenticationPolicy is not provided

### Other Changes


## 1.1.6 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-communication-common` from `1.3.3` to version `1.3.4`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.1.5 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.3` to version `1.3.4`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.


## 1.1.4 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.
- Upgraded `azure-communication-common` from `1.3.2` to version `1.3.3`.


## 1.1.3 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.


## 1.1.2 (2024-03-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.
- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-communication-common` from `1.3.0` to version `1.3.1`.


## 1.1.1 (2024-02-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to `1.3.0`
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.

## 1.1.0 (2024-01-04)

### Bugs Fixed
* NullPointerException in routerQueue.getLabels()
* createJob() deserialization error.

## 1.0.0 (2023-12-05)

### Breaking Changes

#### RouterAdministrationClient
- 
- `listQueues` returns `PagedIterable<RouterQueue>` rather than `PagedIterable<RouterQueueItem>`
- `listDistributionPolicies` returns `PagedIterable<DistributionPolicy>` rather than `PagedIterable<DistributionPolicyItem>`
- `listClassificationPolicies` returns `PagedIterable<ClassificationPolicy>` rather than `PagedIterable<ClassificationPolicyItem>`
- `listExceptionPolicies` returns `PagedIterable<ExceptionPolicy>` rather than `PagedIterable<ExceptionPolicyItem>`

- Response objects of create, update, get and list methods are changed to return BinaryData.
- update, get, list and delete methods are updated to take RequestOptions in request instead of entity options classes like UpdateClassificationPolicyOptions.

#### RouterClient
- `listJobs` returns `PagedIterable<RouterJob>` rather than `PagedIterable<RouterJobItem>`
- `listWorkers` returns `PagedIterable<RouterWorker>` rather than `PagedIterable<RouterJobWorker>`

- Response objects of create, update, get and list methods are changed to return BinaryData.
- update, get, list and delete methods are updated to take RequestOptions in request instead of entity options classes like UpdateWorkerOptions.

##### RouterJobNote
- Changed constructor from `RouterJobNote()` to `RouterJobNote(string message)`
- Removed setter from `Message`

#### RouterWorker && CreateWorkerOptions
- Rename property `QueueAssignments` -> `Queues`
- `Queues` - Changed `Map<String, RouterQueueAssignment>` -> `List<String>`
- Rename property `TotalCapacity` -> `Capacity`
- Rename property `ChannelConfigurations` -> `Channels`
- `Channels` - Changed `Map<String, ChannelConfiguration>` -> `List<RouterChannel>`

#### ClassificationPolicy && CreateClassificationPolicyOptions
- Property `List<QueueSelectorAttachment> QueueSelectors` changed to `List<QueueSelectorAttachment> QueueSelectorAttachments`
- Property `List<WorkerSelectorAttachment> WorkerSelectors` changed to `List<WorkerSelectorAttachment> WorkerSelectorAttachments`

#### ExceptionPolicy && CreateExceptionPolicyOptions
- Property `ExceptionRules` - Changed from `Map<String, ExceptionRule>` -> `List<ExceptionRule>`

##### ExceptionRule
- `Actions` - Changed `Map<String, ExceptionAction>` -> `List<ExceptionAction>`

##### CancelExceptionAction
- Changed constructor from `CancelExceptionAction(String note = null, String dispositionCode = null)` to `CancelExceptionAction()`

##### ReclassifyExceptionAction
- Changed constructor from `ReclassifyExceptionAction(String classificationPolicyId, Map<String, LabelValue> labelsToUpsert = null)` to `ReclassifyExceptionAction()`
- Removed setter from `LabelsToUpsert`

#### BestWorkerMode
- Removed constructor `BestWorkerMode(RouterRule scoringRule = null, List<ScoringRuleParameterSelector> scoringParameterSelectors = null, bool allowScoringBatchOfWorkers = false, int? batchSize = null, bool descendingOrder = true, bool bypassSelectors = false)`

##### ScoringRuleOptions
- Rename property `AllowScoringBatchOfWorkers` -> `IsBatchScoringEnabled`

#### FunctionRouterRuleCredential
- Removed properties `AppKey` and `FunctionKey`

#### OAuth2WebhookClientCredential
- Removed property `ClientSecret`

#### RouterQueueStatistics
- Changed `Map<String, Double> EstimatedWaitTimeMinutes` to `Map<Integer, TimeSpan> EstimatedWaitTimes`

#### LabelOperator
- Renamed `GreaterThanEqual` to `GreaterThanOrEqual`
- Renamed `LessThanEqual` to `LessThanOrEqual`

#### Renames
- `ChannelConfiguration` -> `RouterChannel`
- `Oauth2ClientCredential` -> `OAuth2WebhookClientCredential`
- `LabelValue` -> `RouterValue`

#### Deletions
- `ClassificationPolicyItem`
- `DistributionPolicyItem`
- `ExceptionPolicyItem`
- `RouterQueueItem`
- `RouterWorkerItem`
- `RouterJobItem`
- `RouterQueueAssignment`
- `UpdateClassificationPolicyOptions`
- `UpdateDistributionPolicyOptions`
- `UpdateExceptionPolicyOptions`
- `UpdateQueueOptions`
- `UpdateWorkerOptions`
- `UpdateJobOptions`

### Other Changes

#### ClassificationPolicy
- Add `ETag`
- Added setters to `FallbackQueueId`, `Name`, and `PrioritizationRule`

#### DistributionPolicy
- Add `ETag`
- Added setters to `Mode` and `Name`

#### ExceptionPolicy
- Added `ETag`
- Added setter to `Name`

##### ExceptionRule
- Added `Id`

##### ExceptionAction
- Added `Id`. Property is read-only. If not provided, it will be generated by the service.

##### ReclassifyExceptionAction
- Added setter to `ClassificationPolicyId`

#### RouterChannel
- Added `ChannelId`

#### RouterJob
- Added `ETag`
- Added setters for `ChannelId`, `ChannelReference`, `ClassificationPolicyId`, `DispositionCode`, `MatchingMode`, `Priority`, `QueueId`

#### RouterQueue
- Added `ETag`
- Added setters for `DistributionPolicyId`, `ExceptionPolicyId` and `Name`

#### RouterWorker
- Added `ETag`

#### BestWorkerMode
- Added setters to `ScoringRule` and `ScoringRuleOptions`

## 1.0.0-beta.1 (2023-07-27)
This is the beta release of Azure Communication Service JobRouter Java SDK. For more information, please see the [README][read_me].

This is a public preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo][issues].

### Features Added
- Using `JobRouterAdministrationClient`
    - Create, update, get, list and delete `DistributionPolicy`.
    - Create, update, get, list and delete `RouterQueue`.
    - Create, update, get, list and delete `ClassificationPolicy`.
    - Create, update, get, list and delete `ExceptionPolicy`.
- Using `JobRouterClient`
    - Create, update, get, list and delete `RouterJob`.
    - `RouterJob` can be created and updated with different matching modes: `QueueAndMatchMode`, `ScheduleAndSuspendMode` and `SuspendMode`.
    - Re-classify a `RouterJob`.
    - Close a `RouterJob`.
    - Complete a `RouterJob`.
    - Cancel a `RouterJob`.
    - Un-assign a `RouterJob`, with option to suspend matching.
    - Get the position of a `RouterJob` in a queue.
    - Create, update, get, list and delete `RouterWorker`.
    - Accept an offer.
    - Decline an offer.
    - Get queue statistics.

### Breaking Changes

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-jobrouter/README.md
[issues]: https://github.com/Azure/azure-sdk-for-java/issues
