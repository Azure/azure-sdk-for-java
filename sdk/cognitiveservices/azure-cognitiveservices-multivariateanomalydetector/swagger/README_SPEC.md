## Generate autorest code

```yaml
input-file:
- /mnt/vss/_work/1/s/azure-rest-api-specs/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview.2/MultivariateAnomalyDetector.json
output-folder: ../
java: true
regenerate-pom: false
partial-update: true
model-override-setter-from-superclass: true
use-default-http-status-code-to-exception-type-mapping: true
generate-sync-async-clients: true
generate-client-as-impl: true
models-subpackage: implementation.models
generate-client-interfaces: false
generate-builder-per-client: true
add-context-parameter: true
generate-tests: true
artifact-id: azure-cognitiveservices-multivariateanomalydetector
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
client-logger: true
namespace: com.azure.cognitiveservices.multivariateanomalydetector
context-client-method-parameter: true
azure-arm: false
service-versions:
- 1.1-preview.2
```
