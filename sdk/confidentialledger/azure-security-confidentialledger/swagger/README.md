## Generate autorest code
### To run, use `autorest --tag:confidential-ledger README.md`

``` yaml $(tag) == 'confidential-ledger'
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
java: true
output-folder: ../
namespace: com.azure.security.confidentialledger
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
title: ConfidentialLedgerClient
use: '@autorest/java@4.1.0'
generate-samples: true
generate-tests: true
```


## Identity Service
### To run, use `autorest --tag:confidential-ledger-certificate README.md`
``` yaml $(tag) == 'confidential-ledger-certificate'
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/identityservice.json
java: true
output-folder: ../
namespace: com.azure.security.confidentialledger.certificate
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
title: ConfidentialLedgerCertificateClient
use: '@autorest/java@4.1.0'
generate-samples: true
generate-tests: true
```