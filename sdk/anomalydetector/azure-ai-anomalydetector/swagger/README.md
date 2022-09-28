## Generate autorest code

```yaml
azure-arm: false

require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/feature/cognitiveservices/anomalydetector/mvad/specification/cognitiveservices/data-plane/AnomalyDetector/readme.md
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
