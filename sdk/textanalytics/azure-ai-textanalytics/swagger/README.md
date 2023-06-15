# Azure Cognitive Service - Text Analytics for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Text Analytics.

---
## Getting Started
To build the SDK for Text Analytics, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`


### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

### Code Generation
```yaml
use: '@autorest/java@4.1.17'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/53240ebc58b3c4e99de723194032064db1d97e63/specification/cognitiveservices/data-plane/Language/stable/2023-04-01/analyzetext.json
java: true
output-folder: ../
namespace: com.azure.ai.textanalytics
models-subpackage: implementation.models
custom-types-subpackage: models
enable-sync-stack: true
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
generic-response-type: true
custom-types: HealthcareEntityRelationType,ExtractiveSummarySentencesOrder,HealthcareEntityCategory
```

### Renames
```yaml
directive:
  - from: swagger-document
    where: $.definitions.HealthcareRelation.properties.relationType
    transform: >
      $["x-ms-enum"].name = "HealthcareEntityRelationType";
  - from: swagger-document
    where: $.definitions.ExtractiveSummarizationSortingCriteria
    transform: >
        $["x-ms-enum"].name = "ExtractiveSummarySentencesOrder";
```
