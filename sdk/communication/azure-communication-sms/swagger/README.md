# Azure Communication SMS Service client library for Java

> see https://aka.ms/autorest
## Getting Started
To build the SDK for SMS Client, simply Install AutoRest and in this folder, run the below:

### Generation
There is one swagger for SMS management APIs.

```ps
cd <swagger-folder>
autorest README.md
```

## Update generated files for sms service
To update generated files for sms service, run the following command

> autorest README.md

### Code generation settings
``` yaml
tag: package-sms-2021-03-07
use: '@autorest/java@4.1.42'
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/896d05e37dbb00712726620b8d679cc3c3be09fb/specification/communication/data-plane/Sms/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL	
namespace: com.azure.communication.sms	
generate-client-as-impl: true7
custom-types: SmsSendOptions
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all7
title: Azure Communication SMS Service
```
### Directive renaming "id" property to "identifier"
``` yaml
directive:
    from: swagger-document
    where: '$.definitions.SmsSendOptions.properties.enableDeliveryReport'
    transform: >
        $["x-ms-client-name"] = "deliveryReportEnabled";
```
