---
page_type: sample
languages:
    - java
products:
    - azure
    - azure-cognitive-services
    - azure-ai-content-safety
urlFragment: ai-contentsafety-samples
---

# Azure AI Content Safety Samples client library for Java

This document explains samples and how to use them.

## Examples

Following section document various examples.

### Create Content Safety Client Sample

* [CreateContentSafetyClient.java][sample_CreateContentSafetyClient] - Contains samples for following scenarios:
    * Authenticate client by Key Credential
    * Authenticate client by Token Credential
  
### Analyze Text Sample

* [AnalyzeText.java][sample_AnalyzeText] - Contains samples for following scenarios:
    * Authenticate client
    * Analyze text

### Analyze Image Samples

* [AnalyzeImage.java][sample_AnalyzeImage] - Contains samples for following scenarios:
    * Authenticate client
    * Analyze image

### Manage text blocklist Samples

* [ManageTextBlocklist.java][sample_ManageTextBlocklist] - Contains samples for following scenarios:
    * Authenticate client
    * Create or update text blocklist
    * Add blockItems
    * Analyze text with blocklists
    * List text blocklists
    * Get text blocklist
    * List blockItems
    * Get blockItem
    * Remove blockItems
    * Delete text blocklist

## Troubleshooting

### General

|Error Code	|Possible reasons	|Suggestions|
|-----------|-------------------|-----------|
|InvalidRequestBody	|One or more fields in the request body do not match the API definition.	|1. Check the API version you specified in the API call.<br>2. Check the corresponding API definition for the API version you selected.|
|InvalidResourceName	|The resource name you specified in the URL does not meet the requirements, like the blocklist name, blocklist term ID, etc.	|1. Check the API version you specified in the API call.<br>2. Check whether the given name has invalid characters according to the API definition.|
|ResourceNotFound	|The resource you specified in the URL may not exist, like the blocklist name.	|1. Check the API version you specified in the API call.<br>2. Double check the existence of the resource specified in the URL.|
|InternalError	|Some unexpected situations on the server side have been triggered.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2. Contact Azure Support if this issue persists.|
|ServerBusy	|The server side cannot process the request temporarily.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2.Contact Azure Support if this issue persists.|
|TooManyRequests	|The current RPS has exceeded the quota for your current SKU.	|1. Check the pricing table to understand the RPS quota.<br>2.Contact Azure Support if you need more QPS.|


<!-- LINKS -->
<!-- FIX LINK BRANCH AFTER PR MERGE -->
[sample_CreateContentSafetyClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/AnalyzeText.java
[sample_AnalyzeText]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/AnalyzeText.java
[sample_AnalyzeImage]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/AnalyzeImage.java
[sample_ManageTextBlocklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/ManageTextBlocklist.java
