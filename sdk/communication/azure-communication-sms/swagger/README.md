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
autorest README.md --java
```

### Code generation settings

``` yaml
tag: package-sms-2026-01-23
use: '@autorest/java@4.1.59'
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/ee1579c284a9d032eaa70b1a183b661813decd41/specification/communication/data-plane/Sms/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.sms
generate-client-as-impl: true
service-interface-as-public: true
custom-types: SmsSendOptions
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication SMS Service
internal-constructors: BadRequestErrorResponse,StandardErrorResponse,ErrorDetail
internal-types: BadRequestErrorResponse,StandardErrorResponse,ErrorDetail
models-subpackage: implementation.models
```

### Directive renaming "id" property to "identifier"

``` yaml
directive:
    from: swagger-document
    where: '$.definitions.SmsSendOptions.properties.enableDeliveryReport'
    transform: >
        $["x-ms-client-name"] = "deliveryReportEnabled";
```

### Directive to exclude error responses from SMS, OptOuts, and Delivery Reports operations

``` yaml
directive:
  # Update SMS send operation to only expect 202 (success) responses
  # Remove 400 and 401 error responses so they become unexpected and throw HttpResponseException
  - from: swagger-document
    where: '$.paths["/sms"].post.responses'
    transform: >
        const successResponse = $["202"];
        return { "202": successResponse };
  
  # Update OptOuts add operation to only expect 200 (success) responses
  # Remove 400 and 401 error responses so they become unexpected and throw HttpResponseException
  - from: swagger-document
    where: '$.paths["/sms/optouts:add"].post.responses'
    transform: >
        const successResponse = $["200"];
        return { "200": successResponse };
  
  # Update OptOuts remove operation to only expect 200 (success) responses
  # Remove 400 and 401 error responses so they become unexpected and throw HttpResponseException
  - from: swagger-document
    where: '$.paths["/sms/optouts:remove"].post.responses'
    transform: >
        const successResponse = $["200"];
        return { "200": successResponse };
  
  # Update OptOuts check operation to only expect 200 (success) responses
  # Remove 400 and 401 error responses so they become unexpected and throw HttpResponseException
  - from: swagger-document
    where: '$.paths["/sms/optouts:check"].post.responses'
    transform: >
        const successResponse = $["200"];
        return { "200": successResponse };
  
  # Update Delivery Reports get operation to only expect 200 (success) responses
  # Remove 404 error responses so they become unexpected and throw HttpResponseException
  - from: swagger-document
    where: '$.paths["/deliveryReports/{outgoingMessageId}"].get.responses'
    transform: >
        const successResponse = $["200"];
        return { "200": successResponse };
```

### Directive to make error models package-private

``` yaml
directive:
  # Make BadRequestErrorResponse package-private 
  - from: swagger-document
    where: '$.definitions.BadRequestErrorResponse'
    transform: >
        $["x-accessibility"] = "package";
  
  # Make StandardErrorResponse package-private
  - from: swagger-document
    where: '$.definitions.StandardErrorResponse'
    transform: >
        $["x-accessibility"] = "package";
  
  # Make ErrorDetail package-private
  - from: swagger-document
    where: '$.definitions.ErrorDetail'
    transform: >
        $["x-accessibility"] = "package";
```
