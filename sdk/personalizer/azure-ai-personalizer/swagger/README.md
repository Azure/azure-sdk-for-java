# Azure Cognitive Service - Personalizer for Java

> see https://aka.ms/autorest

### Setup

```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>****
autorest
```

### Code generation settings
```yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cognitiveservices/data-plane/Personalizer/preview/v1.1-preview.3/Personalizer.json
java: true
use: '@autorest/java@4.1.52'
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.personalizer
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
custom-types-subpackage: administration.models
custom-types: PersonalizerDateRange,PersonalizerEvaluation,PersonalizerEvaluationOptions,PersonalizerEvaluationJobStatus,PersonalizerCreateEvaluationOperation,PersonalizerEvaluationType,PersonalizerLearningMode,PersonalizerLogProperties,PersonalizerLogPropertiesDateRange,PersonalizerModelProperties,PersonalizerPolicy,PersonalizerPolicyResult,PersonalizerPolicyResultSummary,PersonalizerPolicyResultTotalSummary,PersonalizerPolicySource,PersonalizerServiceProperties,EvaluationsCreateHeaders,PersonalizerPolicyReferenceOptions
customization-class: src/main/java/PersonalizerCustomization.java
directive:
- rename-model:
    from: DateRange
    to: PersonalizerDateRange
- rename-model:
    from: Evaluation
    to: PersonalizerEvaluation
- rename-model:
    from: EvaluationContract
    to: PersonalizerEvaluationOptions
- rename-model:
    from: EvaluationJobStatus
    to: PersonalizerEvaluationJobStatus
- rename-model:
    from: EvaluationsCreateResponse
    to: PersonalizerCreateEvaluationOperation
- rename-model:
    from: EvaluationType
    to: PersonalizerEvaluationType
- rename-model:
    from: LearningMode
    to: PersonalizerLearningMode
- rename-model:
    from: LogsProperties
    to: PersonalizerLogProperties
- rename-model:
    from: LogsPropertiesDateRange
    to: PersonalizerLogPropertiesDateRange
- rename-model:
    from: ModelProperties
    to: PersonalizerModelProperties
- rename-model:
    from: MultiSlotRankRequest
    to: PersonalizerRankMultiSlotOptions
- rename-model:
    from: MultiSlotRankResponse
    to: PersonalizerRankMultiSlotResult
- rename-model:
    from: MultiSlotRewardRequest
    to: PersonalizerRewardMultiSlotOptions
- rename-model:
    from: PolicyContract
    to: PersonalizerPolicy
- rename-model:
    from: PolicyReferenceContract
    to: PersonalizerPolicyReferenceOptions
- rename-model:
    from: PolicyResult
    to: PersonalizerPolicyResult
- rename-model:
    from: PolicyResultSummary
    to: PersonalizerPolicyResultSummary
- rename-model:
    from: PolicyResultTotalSummary
    to: PersonalizerPolicyResultTotalSummary
- rename-model:
    from: PolicySource
    to: PersonalizerPolicySource
- rename-model:
    from: RankableAction
    to: PersonalizerRankableAction
- rename-model:
    from: RankedAction
    to: PersonalizerRankedAction
- rename-model:
    from: RankRequest
    to: PersonalizerRankOptions
- rename-model:
    from: RankResponse
    to: PersonalizerRankResult
- rename-model:
    from: RewardRequest
    to: PersonalizerRewardOptions
- rename-model:
    from: ServiceConfiguration
    to: PersonalizerServiceProperties
- rename-model:
    from: SlotRequest
    to: PersonalizerSlotOptions
- rename-model:
    from: SlotResponse
    to: PersonalizerSlotResult
- rename-model:
    from: SlotReward
    to: PersonalizerSlotReward
- generate-samples: true
```

### Replace Uri with Url

```yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    $.PersonalizerServiceProperties.properties.logMirrorSasUri["x-ms-client-name"] = "logMirrorSasUrl";
```

### Rename enableOfflineExperimentation to offlineExperimentationEnabled

```yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    $.PersonalizerEvaluationOptions.properties.enableOfflineExperimentation["x-ms-client-name"] = "offlineExperimentationEnabled";
```

### Fix for ApiVersion in parameterized host

```yaml
directive:
- from: swagger-document
  where: $["x-ms-parameterized-host"]
  transform: >
    $.hostTemplate = "{Endpoint}/personalizer/{ApiVersion}";
    $.parameters.push({
      "name": "ApiVersion",
      "description": "Supported Cognitive Services API version.",
      "x-ms-parameter-location": "client",
      "required": true,
      "type": "string",
      "in": "path",
      "x-ms-skip-url-encoding": true
    });
```

```yaml
directive:
- from: swagger-document
  where: $
  transform: >
    delete $.basePath;
```
