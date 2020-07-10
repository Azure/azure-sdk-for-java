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
and checkout the v4 branch. Then run `mvn package -Dlocal` to generate build files.

Then fork and clone the Azure rest API specs from the repo 
(https://github.com/Azure/azure-rest-api-specs) and run `autorest --java` in this folder,
with the following tags:

`--use=<path to rest-api-spec clone>`

`--autorest-v4-location=<path to autorest clone>`

``` yaml $(java)
title: EventGridPublisherClient
description: EventGrid Publisher Client
openapi-type: data-plane
output-folder: ../
namespace: com.azure.messaging.eventgrid
license-header: MICROSOFT_MIT_NO_CODEGEN
generate-client-as-impl: true
input-file:
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Storage/stable/2018-01-01/Storage.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.EventHub/stable/2018-01-01/EventHub.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Resources/stable/2018-01-01/Resources.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.EventGrid/stable/2018-01-01/EventGrid.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Devices/stable/2018-01-01/IotHub.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.ContainerRegistry/stable/2018-01-01/ContainerRegistry.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.ServiceBus/stable/2018-01-01/ServiceBus.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Media/stable/2018-01-01/MediaServices.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Maps/stable/2018-01-01/Maps.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.AppConfiguration/stable/2018-01-01/AppConfiguration.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.SignalRService/stable/2018-01-01/SignalRService.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.KeyVault/stable/2018-01-01/KeyVault.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.MachineLearningServices/stable/2018-01-01/MachineLearningServices.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Cache/stable/2018-01-01/RedisCache.json
- $(azure-api-spec-location)/specification/eventgrid/data-plane/Microsoft.Web/stable/2018-01-01/Web.json
```



