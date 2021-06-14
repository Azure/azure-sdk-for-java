## Generate autorest code
``` yaml
input-file: 
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/preview/0.1-preview/common.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/preview/0.1-preview/confidentialledger.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/preview/0.1-preview/identityservice.json
java: true
output-folder: ../
namespace: com.azure.security.confidentialledger
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
title: ConfidentialLedgerClient
```
