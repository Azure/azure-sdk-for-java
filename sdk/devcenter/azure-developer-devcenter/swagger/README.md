## Generate autorest code

```yaml
use: '@autorest/java@4.1.7'
input-file:
  - https://github.com/Azure/azure-rest-api-specs/blob/9685ef961fe7931fccdd9bf86afac8bca0b3ae88/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/devcenter.json
  - https://github.com/Azure/azure-rest-api-specs/blob/9685ef961fe7931fccdd9bf86afac8bca0b3ae88/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/devbox.json
  - https://github.com/Azure/azure-rest-api-specs/blob/9685ef961fe7931fccdd9bf86afac8bca0b3ae88/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/environments.json
output-folder: ../
java: true
regenerate-pom: false
security: AADToken
security-scopes: https://devcenter.azure.com/.default
data-plane: true
generate-tests: true
artifact-id: azure-developer-devcenter
generate-samples: true
namespace: com.azure.developer.devcenter
service-versions:
- 2022-03-01-preview
directive:
- from: swagger-document
  where: $.parameters["ProjectNameParameter"]
  transform: $["x-ms-parameter-location"] = "method"
```
