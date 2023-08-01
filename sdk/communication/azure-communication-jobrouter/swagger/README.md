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
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/29159d148372f5f61cb04b76fc87252b13c62515/specification/communication/data-plane/JobRouter/readme.md
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
customization-class: JobRouterCustomizations
directive:
# Ensure that empty json passed for cancelJobRequest
- where-operation: JobRouter_CancelJobAction
  transform: $.parameters[2].required = true

- rename-model:
    from: AcceptJobOfferResult
    to: AcceptJobOfferResultInternal
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
    from: DirectMapRouterRule
    to: DirectMapRouterRuleInternal
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
    from: ExpressionRouterRule
    to: ExpressionRouterRuleInternal
- rename-model:
    from: FunctionRouterRule
    to: FunctionRouterRuleInternal
- rename-model:
    from: FunctionRouterRuleCredential
    to: FunctionRouterRuleCredentialInternal
- rename-model:
    from: RouterJobAssignment
    to: RouterJobAssignmentInternal
- rename-model:
    from: RouterJobCollection
    to: RouterJobCollectionInternal
- rename-model:
    from: ExceptionTrigger
    to: ExceptionTriggerInternal
- rename-model:
    from: RouterJobOffer
    to: RouterJobOfferInternal
- rename-model:
    from: RouterJobPositionDetails
    to: RouterJobPositionDetailsInternal
- rename-model:
    from: RouterQueue
    to: RouterQueueInternal
- rename-model:
    from: RouterQueueItem
    to: RouterQueueItemInternal
- rename-model:
    from: JobRouterError
    to: CommunicationError
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
    from: PassThroughQueueSelectorAttachment
    to: PassThroughQueueSelectorAttachmentInternal
- rename-model:
    from: PassThroughWorkerSelectorAttachment
    to: PassThroughWorkerSelectorAttachmentInternal
- rename-model:
    from: RouterQueueCollection
    to: RouterQueueCollectionInternal
- rename-model:
    from: QueueLengthExceptionTrigger
    to: QueueLengthExceptionTriggerInternal
- rename-model:
    from: RouterQueueSelector
    to: RouterQueueSelectorInternal
- rename-model:
    from: QueueSelectorAttachment
    to: QueueSelectorAttachmentInternal
- rename-model:
    from: RouterQueueStatistics
    to: RouterQueueStatisticsInternal
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
    from: RouterWorkerState
    to: RouterWorkerStateInternal
- rename-model:
    from: RuleEngineQueueSelectorAttachment
    to: RuleEngineQueueSelectorAttachmentInternal
- rename-model:
    from: RuleEngineWorkerSelectorAttachment
    to: RuleEngineWorkerSelectorAttachmentInternal
- rename-model:
    from: ScoringRuleOptions
    to: ScoringRuleOptionsInternal
- rename-model:
    from: StaticQueueSelectorAttachment
    to: StaticQueueSelectorAttachmentInternal
- rename-model:
    from: StaticRouterRule
    to: StaticRouterRuleInternal
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
    from: WebhookRouterRule
    to: WebhookRouterRuleInternal
- rename-model:
    from: WeightedAllocationQueueSelectorAttachment
    to: WeightedAllocationQueueSelectorAttachmentInternal
- rename-model:
    from: WeightedAllocationWorkerSelectorAttachment
    to: WeightedAllocationWorkerSelectorAttachmentInternal
- rename-model:
    from: RouterWorkerAssignment
    to: RouterWorkerAssignmentInternal
- rename-model:
    from: RouterWorkerCollection
    to: RouterWorkerCollectionInternal
- rename-model:
    from: RouterWorkerSelector
    to: RouterWorkerSelectorInternal
- rename-model:
    from: WorkerSelectorAttachment
    to: WorkerSelectorAttachmentInternal
- rename-model:
    from: WorkerWeightedAllocation
    to: WorkerWeightedAllocationInternal
- rename-model:
    from: JobMatchingMode
    to: JobMatchingModeInternal
- rename-model:
    from: ScheduleAndSuspendMode
    to: ScheduleAndSuspendModeInternal

- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "RouterJobStatusSelector")]
  transform: >
    $ = "RouterJobStatusSelectorInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "LabelOperator")]
  transform: >
    $ = "LabelOperatorInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "RouterJobStatus")]
  transform: >
    $ = "RouterJobStatusInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "RouterWorkerState")]
  transform: >
    $ = "RouterWorkerStateInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "ScoringRuleParameterSelector")]
  transform: >
    $ = "ScoringRuleParameterSelectorInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "RouterWorkerSelectorStatus")]
  transform: >
    $ = "RouterWorkerSelectorStatusInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "ExpressionRouterRuleLanguage")]
  transform: >
    $ = "ExpressionRouterRuleLanguageInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "RouterWorkerStateSelector")]
  transform: >
    $ = "RouterWorkerStateSelectorInternal";
- from: swagger-document
  where: $..["x-ms-enum"][?(@ == "JobMatchModeType")]
  transform: >
    $ = "JobMatchModeTypeInternal";

# Set reference to WorkerSelectorAttachment in ClassificationPolicy
- from: swagger-document
  where: "$.definitions.ClassificationPolicyInternal.properties.workerSelectors.items"
  transform: >
    $["$ref"] = "#/definitions/WorkerSelectorAttachmentInternal";
# Set reference to QueueSelectorAttachment in ClassificationPolicy  
- from: swagger-document
  where: "$.definitions.ClassificationPolicyInternal.properties.queueSelectors.items"
  transform: >
    $["$ref"] = "#/definitions/QueueSelectorAttachmentInternal";
# Set reference to ExceptionAction in ExceptionRule
- from: swagger-document
  where: "$.definitions.ExceptionRuleInternal.properties.actions"
  transform: >
    $.type = "object";
    $.additionalProperties["$ref"] = "#/definitions/ExceptionActionInternal";
```

### Customization
```java
import org.slf4j.Logger;

public class JobRouterCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.communication.jobrouter.implementation.models");
        ClassCustomization matchingModeInternal = implementationModels.getClass("JobMatchingModeInternal");
        matchingModeInternal.addAnnotation("JsonInclude(Include.ALWAYS)");
    }
}
```
