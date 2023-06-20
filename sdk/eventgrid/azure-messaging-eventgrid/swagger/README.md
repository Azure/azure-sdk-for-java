# Instructions for Generation
This file is used to generate the OpenAPI files for track 2 EventGrid
## Requirements/Installation
You need the following to start generating code
> NodeJS v10.x - v13.x
>
> Java 8+
>
> Maven 3.x

Install Autorest beta with NPM:

`npm i -g @autorest/autorest`

## Using

run `autorest readme.md`

If you are adding or updating swagger files, please make sure that they are included 
in the input file list. Also, if you are adding an additional service event, make sure that 
its `eventType` string is added as a constant and a mapping to the event data model in the
`SystemEventMappings` file.

```yaml
use: '@autorest/java@4.1.17'
java: true
title: EventGridPublisherClient
description: EventGrid Publisher Client
output-folder: ../
namespace: com.azure.messaging.eventgrid
license-header: MICROSOFT_MIT_SMALL
generate-client-as-impl: true
context-client-method-parameter: true
models-subpackage: systemevents
customization-class: src/main/java/EventGridCustomization.java
service-interface-as-public: true
url-as-string: true
directive:
    - rename-model:
        from: ResourceActionCancelData
        to: ResourceActionCancelEventData
    - rename-model:
        from: ResourceActionFailureData
        to: ResourceActionFailureEventData
    - rename-model:
        from: ResourceActionSuccessData
        to: ResourceActionSuccessEventData
    - rename-model:
        from: ResourceDeleteCancelData
        to: ResourceDeleteCancelEventData
    - rename-model:
        from: ResourceDeleteFailureData
        to: ResourceDeleteFailureEventData
    - rename-model:
        from: ResourceDeleteSuccessData
        to: ResourceDeleteSuccessEventData
    - rename-model:
        from: ResourceWriteCancelData
        to: ResourceWriteCancelEventData
    - rename-model:
        from: ResourceWriteFailureData
        to: ResourceWriteFailureEventData
    - rename-model:
        from: ResourceWriteSuccessData
        to: ResourceWriteSuccessEventData
    - rename-model:
        from: RedisImportRDBCompletedEventData
        to: RedisImportRdbCompletedEventData
    - rename-model:
        from: RedisExportRDBCompletedEventData
        to: RedisExportRdbCompletedEventData

custom-types-subpackage: implementation.models
custom-types: CloudEvent,EventGridEvent
model-override-setter-from-superclass: true

input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Storage/stable/2018-01-01/Storage.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.EventHub/stable/2018-01-01/EventHub.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Resources/stable/2018-01-01/Resources.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.EventGrid/stable/2018-01-01/EventGrid.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Devices/stable/2018-01-01/IotHub.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.ContainerRegistry/stable/2018-01-01/ContainerRegistry.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.ServiceBus/stable/2018-01-01/ServiceBus.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Media/stable/2018-01-01/MediaServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Maps/stable/2018-01-01/Maps.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.AppConfiguration/stable/2018-01-01/AppConfiguration.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.SignalRService/stable/2018-01-01/SignalRService.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.KeyVault/stable/2018-01-01/KeyVault.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.MachineLearningServices/stable/2018-01-01/MachineLearningServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Cache/stable/2018-01-01/RedisCache.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Web/stable/2018-01-01/Web.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.Communication/stable/2018-01-01/AzureCommunicationServices.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.PolicyInsights/stable/2018-01-01/PolicyInsights.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.ContainerService/stable/2018-01-01/ContainerService.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.ApiManagement/stable/2018-01-01/APIManagement.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.HealthcareApis/stable/2018-01-01/HealthcareApis.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0d41e635294dce73dfa99b07f3da4b68a9c9e29c/specification/eventgrid/data-plane/Microsoft.DataBox/stable/2018-01-01/DataBox.json
```

### KeyVault updates

```yaml
directive:
- from: swagger-document
  where: $.definitions.KeyVaultVaultAccessPolicyChangedEventData.properties
  transform: >
    $["NBF"]["x-ms-client-name"] = "Nbf";
    $["EXP"]["x-ms-client-name"] = "Exp";
      
- from: swagger-document
  where: $.definitions.KeyVaultCertificateNewVersionCreatedEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultCertificateNearExpiryEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultCertificateExpiredEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultKeyNewVersionCreatedEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultKeyNearExpiryEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultKeyExpiredEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultSecretNewVersionCreatedEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultSecretNearExpiryEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
- from: swagger-document
  where: $.definitions.KeyVaultSecretExpiredEventData.properties
  transform: >
      $["NBF"]["x-ms-client-name"] = "Nbf";
      $["EXP"]["x-ms-client-name"] = "Exp";
```

### Mark a discriminator property as "required"

Newer versions of AutoRest complain during validation about the discriminator property being required

```yaml
directive:
  - from: swagger-document
    where: $.definitions.MediaJobOutput
    transform: >
      $.required.push("@odata.type");
```

### Fixup ApiManagement comments

```yaml
directive:
    from: swagger-document
    where: $.definitions
    transform: >
        for (const definition in $) {
            if (definition.startsWith("ApiManagement")) {
              $[definition]["description"] = $[definition]["description"].replace("ApiManagement.API", "ApiManagement.Api");

            }
        }
```
