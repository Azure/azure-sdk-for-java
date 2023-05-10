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
autorest README.md --java
```

## Update generated files for Job Router service
To update generated files for JobRouter service, run the following command

> autorest README.md --java

## Code generation settings
``` yaml
tag: package-jobrouter-2022-07-18-preview
require:
- https://raw.githubusercontent.com/williamzhao87/azure-rest-api-specs/17ac729b6e3e6fe173efccf9822e6d5d7338031b/specification/communication/data-plane/JobRouter/readme.md
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.jobrouter
custom-types-subpackage: models
generate-client-as-impl: true
service-interface-as-public: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
customization-class: src/main/java/JobRouterCustomization.java
directive:
- rename-model:
    from: AcceptJobOfferResult
    to: AcceptJobOfferResultInternal
- rename-model:
    from: AzureFunctionRule
    to: AzureFunctionRuleInternal
- rename-model:
    from: AzureFunctionRuleCredential
    to: AzureFunctionRuleCredentialInternal
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
    from: ConditionalQueueSelector
    to: ConditionalQueueSelectorInternal
- rename-model:
    from: ConditionalQueueSelectorAttachment
    to: ConditionalQueueSelectorAttachmentInternal
- rename-model:
    from: ConditionalWorkerSelector
    to: ConditionalWorkerSelectorInternal
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
    from: JobRouterError
    to: JobRouterErrorInternal
- rename-model:
    from: JobStateSelector
    to: JobStateSelectorInternal
- rename-model:
    from: JobStatus
    to: JobStatusInternal
- rename-model:
    from: LabelOperator
    to: LabelOperatorInternal
- rename-model:
    from: LabelValue
    to: LabelValueInternal
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
    from: PagedWorkerState
    to: PagedWorkerStateInternal
- rename-model:
    from: PassThroughQueueSelector
    to: PassThroughQueueSelectorInternal
- rename-model:
    from: PassThroughQueueSelectorAttachment
    to: PassThroughQueueSelectorAttachmentInternal
- rename-model:
    from: PassThroughWorkerSelector
    to: PassThroughWorkerSelectorInternal
- rename-model:
    from: PassThroughWorkerSelectorAttachment
    to: PassThroughWorkerSelectorAttachmentInternal
- rename-model:
    from: QueueAssignment
    to: QueueAssignmentInternal
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
- renmae-model:
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
    from: RouterJobStatus
    to: RouterJobStatusInternal
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
    from: RouterWorkerState
    to: RouterWorkerStateInternal
- rename-model:
    from: RuleEngineQueueSelector
    to: RuleEngineQueueSelectorInternal
- rename-model:
    from: RuleEngineQueueSelectorAttachment
    to: RuleEngineQueueSelectorAttachmentInternal
- rename-model:
    from: RuleEngineWorkerSelector
    to: RuleEngineWorkerSelectorInternal
- rename-model:
    from: RuleEngineWorkerSelectorAttachment
    to: RuleEngineWorkerSelectorAttachmentInternal
- rename-model:
    from: ScoringRuleOptions
    to: ScoringRuleOptionsInternal
- rename-model:
    from: ScoringRuleParameterSelector
    to: ScoringRuleParameterSelectorInternal
- rename-model:
    from: StaticQueueSelector
    to: StaticQueueSelectorInternal
- rename-model:
    from: StaticQueueSelectorAttachment
    to: StaticQueueSelectorAttachmentInternal
- rename-model:
    from: StaticRule
    to: StaticRuleInternal
- rename-model:
    from: StaticWorkerSelector
    to: StaticWorkerSelectorInternal
- rename-model:
    from: StaticWorkerSelectorAttachment
    to: StaticWorkerSelectorAttachmentInternal
- rename-model:
    from: UnassignJobResult
    to: UnassignJobResultInternal
- rename-model:
    from: WaitTimeExceptionTrigger
    to: WaitTimeExceptionTriggerInternal
- rename-model:
    from: WebhookRule
    to: WebhookRuleInternal
- rename-model:
    from: WeightedAllocationQueueSelector
    to: WeightedAllocationQueueSelectorInternal
- rename-model:
    from: WeightedAllocationQueueSelectorAttachment
    to: WeightedAllocationQueueSelectorAttachmentInternal
- rename-model:
    from: WeightedAllocationWorkerSelector
    to: WeightedAllocationWorkerSelectorInternal
- rename-model:
    from: WeightedAllocatioWorkerSelectorAttachment
    to: WeightedAllocationWorkerSelectorAttachmentInternal
- rename-model:
    from: WorkerAssignment
    to: WorkerAssignmentInternal
- rename-model:
    from: WorkerCollection
    to: WorkerCollectionInternal
- rename-model:
    from: WorkerSelector
    to: WorkerSelectorInternal
- rename-model:
    from: WorkerSelectorAttachment
    to: WorkerSelectorAttachmentInternal
- rename-model:
    from: WorkerSelectorState
    to: WorkerSelectorStateInternal
- rename-model:
    from: WorkerStateSelector
    to: WorkerStateSelectorInternal
- rename-model:
    from: WorkerWeightedAllocation
    to: WorkerWeightedAllocationInternal
```
