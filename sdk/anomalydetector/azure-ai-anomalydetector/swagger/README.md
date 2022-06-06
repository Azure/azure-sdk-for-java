## Generate autorest code
``` yaml
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview.1/AnomalyDetector.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview.1/MultivariateAnomalyDetector.json
use: '@autorest/java@4.0.24'
java: true
output-folder: ../
namespace: com.azure.ai.anomalydetector
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
generate-sync-async-clients: true
context-client-method-parameter: true

directive:
  - from: swagger-document
    where: $.definitions.AlignPolicy.properties.fillNAMethod
    transform: $["x-ms-client-name"] = "fillNaMethod"
  - from: swagger-document
    where: $.definitions.AlignPolicy.properties.fillNAMethod
    transform: $["x-ms-enum"].name = "fillNaMethod"
```
