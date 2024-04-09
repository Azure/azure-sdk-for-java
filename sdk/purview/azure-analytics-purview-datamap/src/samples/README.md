---
page_type: sample
languages:
    - java
products:
    - azure
urlFragment: purview-datamap-java-samples
---

# Azure Purview DataMap Samples client library for Java

This document explains samples and how to use them.

## Examples

Following section document various examples.

### Create Type Definition Client Sample

* [CreateTypeDefinitionClient.java][sample_CreateTypeDefinitionClient] - Contains samples for following scenarios:
    * Authenticate client by Client Secret

## Troubleshooting

### General

|Error Code	|Possible reasons	|Suggestions|
|-----------|-------------------|-----------|
|InvalidRequestBody	|One or more fields in the request body do not match the API definition.	|1. Check the API version you specified in the API call.<br>2. Check the corresponding API definition for the API version you selected.|
|InvalidResourceName	|The resource name you specified in the URL does not meet the requirements, like the blocklist name, blocklist term ID, etc.	|1. Check the API version you specified in the API call.<br>2. Check whether the given name has invalid characters according to the API definition.|
|ResourceNotFound	|The resource you specified in the URL may not exist, like the blocklist name.	|1. Check the API version you specified in the API call.<br>2. Double check the existence of the resource specified in the URL.|
|InternalError	|Some unexpected situations on the server side have been triggered.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2. Contact Azure Support if this issue persists.|
|ServerBusy	|The server side cannot process the request temporarily.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2.Contact Azure Support if this issue persists.|


<!-- LINKS -->
<!-- FIX LINK BRANCH AFTER PR MERGE -->
[sample_CreateTypeDefinitionClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-datamap/src/samples/java/com/azure/analytics/purview/datamap/ReadmeSamples.java
