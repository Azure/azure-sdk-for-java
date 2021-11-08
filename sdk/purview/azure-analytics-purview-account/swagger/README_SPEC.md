## Generate autorest code

```yaml
input-file: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/preview/2019-11-01-preview/account.json
java: true
output-folder: ../
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.analytics.purview.account
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
service-versions:
  - '2019-11-01-preview'
```
