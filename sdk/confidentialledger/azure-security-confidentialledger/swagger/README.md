## Generate autorest code
``` yaml
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/identityservice.json
java: true
output-folder: ../
namespace: com.azure.security.confidentialledger
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
title: ConfidentialLedgerClient
```