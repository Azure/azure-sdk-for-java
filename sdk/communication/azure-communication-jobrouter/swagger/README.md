# Azure Communication Service Job Router client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Job Router Client, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

There is one swagger for JobRouter management APIs.

```ps
cd <swagger-folder>
autorest README.md --java --service-name Router
```

## Update generated files for Job Router service
To update generated files for JobRouter service, run the following command

> autorest README.md --java

## Code generation settings
``` yaml
tag: package-jobrouter-2022-07-18-preview
require:
- https://raw.githubusercontent.com/williamzhao87/azure-rest-api-specs/9d0184f60868f175aa515e34cffdc3bd6235768f/specification/communication/data-plane/JobRouter/readme.md
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.jobrouter
service-name: JobRouter
data-plane: true
generate-models: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
generate-client-as-impl: true
generate-sync-async-clients: false
generate-builder-per-client: false
service-interface-as-public: true
customization-class: src/main/java/JobRouterCustomization.java
title: Azure Communication Job Router Service
partial-update: true
directive:
- rename-model:
    from: BestWorkerMode
    to: BestWorkerModeInternal
- rename-model:
    from: CancelExceptionAction
    to: CancelExceptionActionInternal
- rename-model:
    from: ChannelConfiguration
    to: ChannelConfigurationInternal
- rename-model:
    from: ClassificationPolicy
    to: ClassificationPolicyInternal
- rename-model:
    from: ClassificationPolicyCollection
    to: ClassificationPolicyCollectionInternal
- rename-model:
    from: ClassificationPolicyItem
    to: ClassificationPolicyItemInternal
- rename-model:
    from: ConditionalQueueSelectorAttachment
    to: ConditionalQueueSelectorAttachmentInternal
- rename-model:
    from: ConditionalWorkerSelectorAttachment
    to: ConditionalWorkerSelectorAttachmentInternal
- rename-model:
    from: DirectMapRule
    to: DirectMapRuleInternal
- rename-model:
    from: DistributionMode
    to: DistributionModeInternal
- rename-model:
    from: DistributionPolicy
    to: DistributionPolicyInternal
- rename-model:
    from: DistributionPolicyCollection
    to: DistributionPolicyCollectionInternal
- rename-model:
    from: DistributionPolicyItem
    to: DistributionPolicyItemInternal
- rename-model:
    from: ExceptionAction
    to: ExceptionActionInternal
- rename-model:
    from: ExceptionPolicy
    to: ExceptionPolicyInternal
- rename-model:
    from: ExceptionPolicyCollection
    to: ExceptionPolicyCollectionInternal
- rename-model:
    from: ExceptionPolicyItem
    to: ExceptionPolicyItemInternal
- rename-model:
    from: ExceptionRule
    to: ExceptionRuleInternal
- rename-model:
    from: ExpressionRule
    to: ExpressionRuleInternal
- rename-model:
    from: FunctionRule
    to: FunctionRuleInternal
- rename-model:
    from: FunctionRuleCredential
    to: FunctionRuleCredentialInternal
- rename-model:
    from: JobAssignment
    to: JobAssignmentInternal
- rename-model:
    from: JobCollection
    to: JobCollectionInternal
- rename-model:
    from: JobExceptionTrigger
    to: JobExceptionTriggerInternal
- rename-model:
    from: JobOffer
    to: JobOfferInternal
- rename-model:
    from: JobPositionDetails
    to: JobPositionDetailsInternal
- rename-model:
    from: JobQueue
    to: JobQueueInternal
- rename-model:
    from: JobQueueItem
    to: JobQueueItemInternal
- rename-model:
    from: JobQueueItem
    to: JobQueueItemInternal
- rename-model:
    from: LongestIdleMode
    to: LongestIdleModeInternal
- rename-model:
    from: ManualReclassifyExceptionAction
    to: ManualReclassifyExceptionActionInternal
- rename-model:
    from: Oauth2ClientCredential
    to: Oauth2ClientCredentialInternal
- rename-model:
    from: Oauth2ClientCredential
    to: Oauth2ClientCredentialInternal
- rename-model:
    from: PassThroughQueueSelectorAttachment
    to: PassThroughQueueSelectorAttachmentInternal
- rename-model:
    from: PassThroughWorkerSelectorAttachment
    to: PassThroughWorkerSelectorAttachmentInternal
- rename-model:
    from: QueueCollection
    to: QueueCollectionInternal
- rename-model:
    from: QueueLengthExceptionTrigger
    to: QueueLengthExceptionTriggerInternal
- rename-model:
    from: QueueSelector
    to: QueueSelectorInternal
- rename-model:
    from: QueueSelectorAttachment
    to: QueueSelectorAttachmentInternal
- rename-model:
    from: QueueStatistics
    to: QueueStatisticsInternal
- rename-model:
    from: QueueWeightedAllocation
    to: QueueWeightedAllocationInternal
- rename-model:
    from: ReclassifyExceptionAction
    to: ReclassifyExceptionActionInternal
- rename-model:
    from: RoundRobinMode
    to: RoundRobinModeInternal
- rename-model:
    from: RouterJob
    to: RouterJobInternal
- rename-model:
    from: RouterJobItem
    to: RouterJobItemInternal
- rename-model:
    from: RouterRule
    to: RouterRuleInternal
- rename-model:
    from: RouterWorker
    to: RouterWorkerInternal
- rename-model:
    from: RouterWorkerItem
    to: RouterWorkerItemInternal
- rename-model:
    from: RuleEngineQueueSelectorAttachment
    to: RuleEngineQueueSelectorAttachmentInternal
- rename-model:
    from: RuleEngineWorkerSelectorAttachment
    to: RuleEngineWorkerSelectorAttachmentInternal
```

### Rename JobStateSelector to JobStateSelectorInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.JobStateSelector
    transform: >
      $["x-ms-enum"].name = "JobStateSelectorInternal";
```
### Rename RouterJobStatus to RouterJobStatusInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RouterJobStatus
    transform: >
      $["x-ms-enum"].name = "RouterJobStatusInternal";
```
### Rename LabelOperator to LabelOperatorInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.LabelOperator
    transform: >
      $["x-ms-enum"].name = "LabelOperatorInternal";
```
### Rename RouterWorkerState to RouterWorkerStateInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RouterWorkerState
    transform: >
      $["x-ms-enum"].name = "RouterWorkerStateInternal";
```
