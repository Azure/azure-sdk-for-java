git # Azure Cognitive Service - Text Analytics for Java

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

## Configuration
```yaml
namespace: com.azure.ai.textanalytics
input-file: 
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/53240ebc58b3c4e99de723194032064db1d97e63/specification/cognitiveservices/data-plane/Language/stable/2023-04-01/analyzetext.json
models-subpackage: implementation.models
custom-types-subpackage: models
```

### Code Generation
``` yaml
output-folder: ..\
java: true
use: '@autorest/java@4.1.17'
enable-sync-stack: true
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
generic-response-type: true
```

### Renames
```yaml
directive:
  - rename-model:
      from: RelationType
      to: HealthcareEntityRelationType
```
