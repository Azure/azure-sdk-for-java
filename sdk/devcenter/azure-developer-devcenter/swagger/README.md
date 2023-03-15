## Generate autorest code

```yaml
use: '@autorest/java@4.1.7'
input-file:
  - https://github.com/Azure/azure-rest-api-specs/blob/af3f7994582c0cbd61a48b636907ad2ac95d332c/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-11-11-preview/devcenter.json
  - https://github.com/Azure/azure-rest-api-specs/blob/af3f7994582c0cbd61a48b636907ad2ac95d332c/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-11-11-preview/devbox.json
  - https://github.com/Azure/azure-rest-api-specs/blob/af3f7994582c0cbd61a48b636907ad2ac95d332c/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-11-11-preview/environments.json
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
- 2022-11-11-preview
directive:
- from: swagger-document
  where: $.parameters["ProjectNameParameter"]
  transform: $["x-ms-parameter-location"] = "method"
```
