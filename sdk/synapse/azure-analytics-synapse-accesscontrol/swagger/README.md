# Azure Synapse Analytics Access Control for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Synapse Analytics Access Control.
---
## Getting Started

To build the SDK for Synapse Analytics Access Control, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

### Code generation settings

## Java

```yaml
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3d6211cf28f83236cdf78e7cfc50efd3fb7cba72/specification/synapse/data-plane/Microsoft.Synapse/stable/2020-12-01/checkAccessSynapseRbac.json
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3d6211cf28f83236cdf78e7cfc50efd3fb7cba72/specification/synapse/data-plane/Microsoft.Synapse/stable/2020-12-01/roleDefinitions.json
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3d6211cf28f83236cdf78e7cfc50efd3fb7cba72/specification/synapse/data-plane/Microsoft.Synapse/stable/2020-12-01/roleAssignments.json
java: true
use: '@autorest/java@4.1.34'
output-folder: ../
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.accesscontrol
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: models
context-client-method-parameter: true
required-parameter-client-methods: true
credential-types: tokencredential
credential-scopes: https://dev.azuresynapse.net/.default
```
