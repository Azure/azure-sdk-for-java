## Generate autorest code
``` yaml
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
# - https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
- https://github.com/Azure/azure-rest-api-specs/blob/c079fbd05764f33476a74bc6e6e3d51c564e70e2/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
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
use: '@autorest/java@4.0.62'
generate-samples: true
generate-tests: true
```