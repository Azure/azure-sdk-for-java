## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/f26f6b7fa8a774c505b138d34b861b3d9bd7d07c/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/preview/0.1-preview/confidentialledger.json
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
