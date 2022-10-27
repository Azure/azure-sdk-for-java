## Generate autorest code

```yaml
azure-arm: false

require: https://github.com/Azure/azure-rest-api-specs/blob/07e2c98d860320c1b12f966db7cd2191573e0b50/specification/cognitiveservices/data-plane/AnomalyDetector/readme.md
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-ai-anomalydetector
generate-samples: true
license-header: MICROSOFT_MIT_NO_CODEGEN
namespace: com.azure.ai.anomalydetector
service-versions:
- v1.1
```
