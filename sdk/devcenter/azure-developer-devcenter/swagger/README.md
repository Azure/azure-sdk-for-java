## Generate autorest code

```yaml
use: '@autorest/java@4.1.5'
input-file:
  - https://github.com/Azure/azure-rest-api-specs/blob/75a8d8dcc9f6d0ec626bdeb32f5154f20c8c61cd/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/devcenter.json
  - https://github.com/Azure/azure-rest-api-specs/blob/75a8d8dcc9f6d0ec626bdeb32f5154f20c8c61cd/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/devbox.json
  - https://github.com/Azure/azure-rest-api-specs/blob/75a8d8dcc9f6d0ec626bdeb32f5154f20c8c61cd/specification/devcenter/data-plane/Microsoft.DevCenter/preview/2022-03-01-preview/environments.json
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
