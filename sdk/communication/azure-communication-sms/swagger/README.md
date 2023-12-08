# Azure Communication SMS Service client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Sms Client, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

There is one swagger for Sms management APIs.

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

## Update generated files for Sms service
To update generated files for Sms service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.2

### Code generation settings
``` yaml
tag: package-2024-01-14-preview
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/4ad21c4cd5f024b520b77907b8ac15fb84c8413a/specification/communication/data-plane/Sms/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL	
namespace: com.azure.communication.sms	
generate-client-as-impl: true
service-interface-as-public: true
custom-types: SmsSendOptions,MmsContentType,MmsSendOptions,MmsAttachment
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication SMS Service
```
### Directive renaming "enableDeliveryReport" property to "deliveryReportEnabled" in SMS
``` yaml
directive:
    from: swagger-document
    where: '$.definitions.SmsSendOptions.properties.enableDeliveryReport'
    transform: >
        $["x-ms-client-name"] = "deliveryReportEnabled";
```
### Directive renaming "enableDeliveryReport" property to "deliveryReportEnabled" in MMS
``` yaml
directive:
    from: swagger-document
    where: '$.definitions.MmsSendOptions.properties.enableDeliveryReport'
    transform: >
        $["x-ms-client-name"] = "deliveryReportEnabled";
```
### Directive renaming "MmsSendRequestAttachment" property to "MmsAttachment" in MMS
``` yaml
directive:
    from: swagger-document
    where: '$.definitions.MmsSendRequestAttachment'
    transform: >
        $["x-ms-client-name"] = "MmsAttachment";
```
