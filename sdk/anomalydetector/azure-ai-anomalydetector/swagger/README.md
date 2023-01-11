## Generate autorest code

```yaml
azure-arm: false

require: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/cognitiveservices/data-plane/AnomalyDetector/readme.md
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-ai-anomalydetector
generate-samples: true
namespace: com.azure.ai.anomalydetector
service-versions:
- v1.1
```
