---
page_type: sample
languages:
- java
  products:
- azure
- azure-ai-contentsafety
  urlFragment: developer-loadtesting-samples
---

# Azure AI Content Safety Samples client library for Java

This document explains samples and how to use them.

## Examples

Following section document various examples.

### Analyze Text Sample

* [AnalyzeText.java][sample_helloWorld] - Contains samples for following scenarios:
    * Authenticate client
    * Analyze text

### Analyze Image Samples

* [AnalyzeImage.java][sample_list] - Contains samples for following scenarios:
    * Authenticate client
    * Analyze image

### Manage text blocklist Samples

* [ManageTextBlocklist.java][sample_longRunning] - Contains samples for following scenarios:
    * Authenticate client
    * Analyze image

## Troubleshooting

### General

Load Testing clients raise exceptions. For example, if you try to get a load test or test run resource after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    testRunClient.getTestRunWithResponse("FAKE_TEST_RUN_ID", null);
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

<!-- LINKS -->
<!-- FIX LINK BRANCH AFTER PR MERGE -->
[AnalyzeText.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/AnalyzeText.java
[AnalyzeImage.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/AnalyzeImage.java
[ManageTextBlocklist.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentsafety/azure-ai-contentsafety/src/samples/java/com/azure/ai/contentsafety/ManageTextBlocklist.java
