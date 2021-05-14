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

Fork and clone the autorest.java repo (https://github.com/Azure/autorest.java)
and checkout the v4 branch. Then run `mvn clean package -Dlocal` to generate build files.

Then fork and clone the Azure rest API specs from the repo 
(https://github.com/Azure/azure-rest-api-specs) and run `autorest --java readme.md` 
in this folder, with the following tags:

`--use=<path to autorest clone>`

`--api-spec-location=<path to rest-api-spec clone>`

If you are adding or updating swagger files, please make sure that they are included 
in the input file list. Also, if you are adding an additional service event, make sure that 
its `eventType` string is added as a constant and a mapping to the event data model in the
`SystemEventMappings` file.

``` yaml $(java)
title: EventGridPublisherClient
description: EventGrid Publisher Client
openapi-type: data-plane
output-folder: ../
namespace: com.azure.messaging.eventgrid
license-header: MICROSOFT_MIT_SMALL
generate-client-as-impl: true
context-client-method-parameter: true
models-subpackage: systemevents
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
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Storage/stable/2018-01-01/Storage.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.EventHub/stable/2018-01-01/EventHub.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Resources/stable/2018-01-01/Resources.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.EventGrid/stable/2018-01-01/EventGrid.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Devices/stable/2018-01-01/IotHub.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.ContainerRegistry/stable/2018-01-01/ContainerRegistry.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.ServiceBus/stable/2018-01-01/ServiceBus.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Media/stable/2018-01-01/MediaServices.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Maps/stable/2018-01-01/Maps.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.AppConfiguration/stable/2018-01-01/AppConfiguration.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.SignalRService/stable/2018-01-01/SignalRService.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.KeyVault/stable/2018-01-01/KeyVault.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.MachineLearningServices/stable/2018-01-01/MachineLearningServices.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Cache/stable/2018-01-01/RedisCache.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Web/stable/2018-01-01/Web.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.Communication/stable/2018-01-01/AzureCommunicationServices.json
- $(api-spec-location)/specification/eventgrid/data-plane/Microsoft.PolicyInsights/stable/2018-01-01/PolicyInsights.json
```
