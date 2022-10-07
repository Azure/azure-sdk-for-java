## Generate autorest code

```yaml
require: C:/Users/chrismiller/source/repos/azure-devtest-center/src/sdk/specification/devcenter/data-plane/README.md
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
```
