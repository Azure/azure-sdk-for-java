---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-load-testing
urlFragment: developer-loadtesting-samples
---

# Azure Developer Loadtesting Samples client library for Java

This document explains samples and how to use them.

## Examples

   Following section document various examples.

### Hello World Samples

* [HelloWorld.java][sample_helloWorld] - Contains samples for following scenarios:
  * Authenticate client
  * Create Load Test
  * Upload Test File
  * Create and start Test Run, and get metrics

### List Operations Samples

* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
  * List Load Tests
  * List Test Runs
  * List Load Test files

### Long Running Operations Samples

* [LongRunningOperations.java][sample_longRunning] and [LongRunningOperationsAsync.java][sample_longRunningAsync] - Contains samples for following scenarios:
  * Upload and validate Load Test file
  * Start and monitor Test Run

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
[sample_helloWorld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples/java/com/azure/developer/loadtesting/HelloWorld.java
[sample_list]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples/java/com/azure/developer/loadtesting/ListOperations.java
[sample_listAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples/java/com/azure/developer/loadtesting/ListOperationsAsync.java
[sample_longRunning]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples/java/com/azure/developer/loadtesting/LongRunningOperations.java
[sample_longRunningAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples/java/com/azure/developer/loadtesting/LongRunningOperationsAsync.java
