## Generate autorest code
``` yaml
input-file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2020-12-01-preview/confidentialledger.json
java: true
output-folder: ../
namespace: com.azure.data.confidentialledger
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
```
