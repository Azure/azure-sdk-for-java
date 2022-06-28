## Generate autorest code
``` yaml $(tag) == 'confidential-ledger'
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
#- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
- https://github.com/Azure/azure-rest-api-specs/blob/f89be7447fd4b71e5271ad6ea0d9051ee2c4f01e/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/confidentialledger.json
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


## Identity Service

``` yaml $(tag) == 'confidential-ledger-identity'
input-file: 
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/common.json
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/confidentialledger/data-plane/Microsoft.ConfidentialLedger/stable/2022-05-13/identityservice.json
java: true
output-folder: ../
namespace: com.azure.security.confidentialledger
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: tokencredential
credential-scopes: https://confidential-ledger.azure.com/.default
title: ConfidentialLedgerIdentityClient
use: '@autorest/java@4.0.62'
generate-samples: true
generate-tests: true
```