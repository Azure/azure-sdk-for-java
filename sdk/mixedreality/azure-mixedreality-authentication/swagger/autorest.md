# Azure Mixed Reality Authentication Service client library for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for Mixed Reality Authentication.

---
## Getting Started
To build the SDK for Mixed Reality Authentication, simply [Install Autorest](https://aka.ms/autorest) and in this folder, run:

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

## Configuration

```yaml
use: '@autorest/java@4.1.22'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/aa19725fe79aea2a9dc580f3c66f77f89cc34563/specification/mixedreality/data-plane/Microsoft.MixedReality/preview/2019-02-28-preview/mr-sts.json
title: MixedRealityStsRestClient
namespace: com.azure.mixedreality.authentication
models-subpackage: implementation.models
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
sync-methods: none
stream-style-serialization: true
```
