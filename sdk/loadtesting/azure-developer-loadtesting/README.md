# Azure Load Testing client library for Java

Azure Load Testing provides client library in Java to the user by which they can interact natively with Azure Load Testing service. Azure Load Testing is a fully managed load-testing service that enables you to generate high-scale load. The service simulates traffic for your applications, regardless of where they're hosted. Developers, testers, and quality assurance (QA) engineers can use it to optimize application performance, scalability, or capacity

This package contains Microsoft Azure Developer LoadTesting client library.

## Documentation

Various documentation is available to help you get started

- [Source code][source_code]
- [API reference documentation][api_reference_doc]
- [Product Documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- Azure Load Testing resource

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-developer-loadtesting;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-developer-loadtesting</artifactId>
    <version>1.0.17</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java java-readme-sample-auth
// ensure the user, service principal or managed identity used has Loadtesting Contributor role for the resource
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .build();
// create client using DefaultAzureCredential
LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
        .credential(credential)
        .endpoint("<Enter Azure Load Testing Data-Plane URL>")
        .buildClient();
LoadTestRunClient testRunClient = new LoadTestRunClientBuilder()
        .credential(credential)
        .endpoint("<Enter Azure Load Testing Data-Plane URL>")
        .buildClient();

RequestOptions reqOpts = new RequestOptions()
    .addQueryParam("orderBy", "lastModifiedDateTime")
    .addQueryParam("maxPageSize", "10");
adminClient.listTests(reqOpts);

reqOpts = new RequestOptions()
    .addQueryParam("orderBy", "lastModifiedDateTime")
    .addQueryParam("status", "EXECUTING,DONE")
    .addQueryParam("maxPageSize", "10");
testRunClient.listTestRuns(reqOpts);
```

## Key concepts

The following components make up the Azure Load Testing service. The Azure Load Test client library for Java allows you to interact with each of these components through the use of clients. There are two top-level clients which are the main entry points for the library

- `LoadTestingClient`

- `LoadTestingAsyncClient`

The two clients have similar methods in them except the methods in the async client are async as well.

The top-level clients have two sub-clients

- `LoadTestAdministration`

- `TestRun`

These sub-clients are used for managing and using different components of the service.

### Load Test Administration Client

The `LoadTestAdministration` sub-clients is used to administer and configure the load tests, app components and metrics.

#### Test

A test specifies the test script, and configuration settings for running a load test. You can create one or more tests in an Azure Load Testing resource.

#### App Component

When you run a load test for an Azure-hosted application, you can monitor resource metrics for the different Azure application components (server-side metrics). While the load test runs, and after completion of the test, you can monitor and analyze the resource metrics in the Azure Load Testing dashboard.

#### Metrics

During a load test, Azure Load Testing collects metrics about the test execution. There are two types of metrics:

1. Client-side metrics give you details reported by the test engine. These metrics include the number of virtual users, the request response time, the number of failed requests, or the number of requests per second.

2. Server-side metrics are available for Azure-hosted applications and provide information about your Azure application components. Metrics can be for the number of database reads, the type of HTTP responses, or container resource consumption.

### Test Run Client

The `TestRun` sub-clients is used to start and stop test runs corresponding to a load test. A test run represents one execution of a load test. It collects the logs associated with running the Apache JMeter script, the load test YAML configuration, the list of app components to monitor, and the results of the test.

### Data-Plane Endpoint

Data-plane of Azure Load Testing resources is addressable using the following URL format:

`00000000-0000-0000-0000-000000000000.aaa.cnt-prod.loadtesting.azure.com`

The first GUID `00000000-0000-0000-0000-000000000000` is the unique identifier used for accessing the Azure Load Testing resource. This is followed by  `aaa` which is the Azure region of the resource.

The data-plane endpoint is obtained from Control Plane APIs.

**Example:** `1234abcd-12ab-12ab-12ab-123456abcdef.eus.cnt-prod.loadtesting.azure.com`

In the above example, `eus` represents the Azure region `East US`.

## Examples

### Creating a Load Test

```java java-readme-sample-createTest
LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .endpoint("<endpoint>")
        .buildClient();

// construct Test object using nested String:Object Maps
Map<String, Object> testMap = new HashMap<String, Object>();
testMap.put("displayName", "Sample Display Name");
testMap.put("description", "Sample Description");

// loadTestConfig describes the number of test engines to generate load
Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
loadTestConfigMap.put("engineInstances", 1);
testMap.put("loadTestConfiguration", loadTestConfigMap);

// environmentVariables are plain-text data passed to test engines
Map<String, Object> envVarMap = new HashMap<String, Object>();
envVarMap.put("a", "b");
envVarMap.put("x", "y");
testMap.put("environmentVariables", envVarMap);

// secrets are secure data sent using Azure Key Vault
Map<String, Object> secretMap = new HashMap<String, Object>();
Map<String, Object> sampleSecretMap = new HashMap<String, Object>();
sampleSecretMap.put("value", "https://samplevault.vault.azure.net/secrets/samplesecret/f113f91fd4c44a368049849c164db827");
sampleSecretMap.put("type", "AKV_SECRET_URI");
secretMap.put("sampleSecret", sampleSecretMap);
testMap.put("secrets", secretMap);

// passFailCriteria define the conditions to conclude the test as success
Map<String, Object> passFailMap = new HashMap<String, Object>();
Map<String, Object> passFailMetrics = new HashMap<String, Object>();
Map<String, Object> samplePassFailMetric = new HashMap<String, Object>();
samplePassFailMetric.put("clientmetric", "response_time_ms");
samplePassFailMetric.put("aggregate", "percentage");
samplePassFailMetric.put("condition", ">");
samplePassFailMetric.put("value", "20");
samplePassFailMetric.put("action", "continue");
passFailMetrics.put("fefd759d-7fe8-4f83-8b6d-aeebe0f491fe", samplePassFailMetric);
passFailMap.put("passFailMetrics", passFailMetrics);
testMap.put("passFailCriteria", passFailMap);

// convert the object Map to JSON BinaryData
BinaryData test = BinaryData.fromObject(testMap);

// receive response with BinaryData content
Response<BinaryData> testOutResponse = adminClient.createOrUpdateTestWithResponse("test12345", test, null);
System.out.println(testOutResponse.getValue().toString());
```

### Uploading .jmx file to a Load Test

```java java-readme-sample-uploadTestFile
LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<endpoint>")
    .buildClient();

// extract file contents to BinaryData
BinaryData fileData = BinaryData.fromFile(new File("path/to/file").toPath());

// receive response with BinaryData content
Response<BinaryData> fileUrlOut = adminClient.uploadTestFileWithResponse("test12345", "sample-file.jmx", fileData, null);
System.out.println(fileUrlOut.getValue().toString());
```

### Running a Load Test

```java java-readme-sample-runTest
LoadTestRunClient testRunClient = new LoadTestRunClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<endpoint>")
    .buildClient();

// construct Test Run object using nested String:Object Maps
Map<String, Object> testRunMap = new HashMap<String, Object>();
testRunMap.put("testId", "test12345");
testRunMap.put("displayName", "SDK-Created-TestRun");

// convert the object Map to JSON BinaryData
BinaryData testRun = BinaryData.fromObject(testRunMap);

// start test with poller
SyncPoller<BinaryData, BinaryData> poller = testRunClient.beginTestRun("testrun12345", testRun, null);
Duration pollInterval = Duration.ofSeconds(5);
poller = poller.setPollInterval(pollInterval);

// wait for test to reach terminal state
JsonNode testRunJson = null;
String testStatus;
PollResponse<BinaryData> pollResponse = poller.poll();
while (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED) {
    try {
        testRunJson = new ObjectMapper().readTree(pollResponse.getValue().toString());
        testStatus = testRunJson.get("status").asText();
        System.out.println("Test run status: " + testStatus);
    } catch (JsonProcessingException e) {
        System.out.println("Error processing JSON response");
        // handle error condition
    }

    // wait and check test status every 5 seconds
    try {
        Thread.sleep(pollInterval.toMillis());
    } catch (InterruptedException e) {
        // handle interruption
    }

    pollResponse = poller.poll();
}

poller.waitForCompletion();
BinaryData testRunBinary = poller.getFinalResult();
try {
    testRunJson = new ObjectMapper().readTree(testRunBinary.toString());
    testStatus = testRunJson.get("status").asText();
} catch (JsonProcessingException e) {
    System.out.println("Error processing JSON response");
    // handle error condition
}

String startDateTime = testRunJson.get("startDateTime").asText();
String endDateTime = testRunJson.get("endDateTime").asText();

// get list of all metric namespaces and pick the first one
Response<BinaryData> metricNamespacesOut = testRunClient.getMetricNamespacesWithResponse("testrun12345", null);
String metricNamespace = null;
// parse JSON and read first value
try {
    JsonNode metricNamespacesJson = new ObjectMapper().readTree(metricNamespacesOut.getValue().toString());
    metricNamespace = metricNamespacesJson.get("value").get(0).get("metricNamespaceName").asText();
} catch (JsonProcessingException e) {
    System.out.println("Error processing JSON response");
    // handle error condition
}

// get list of all metric definitions and pick the first one
Response<BinaryData> metricDefinitionsOut = testRunClient.getMetricDefinitionsWithResponse("testrun12345", metricNamespace, null);
String metricName = null;
// parse JSON and read first value
try {
    JsonNode metricDefinitionsJson = new ObjectMapper().readTree(metricDefinitionsOut.getValue().toString());
    metricName = metricDefinitionsJson.get("value").get(0).get("name").get("value").asText();
} catch (JsonProcessingException e) {
    System.out.println("Error processing JSON response");
    // handle error condition
}

// fetch client metrics using metric namespace and metric name
PagedIterable<BinaryData> clientMetricsOut = testRunClient.listMetrics("testrun12345", metricName, metricNamespace, startDateTime + '/' + endDateTime, null);
clientMetricsOut.forEach((clientMetric) -> {
    System.out.println(clientMetric.toString());
});
```

## Troubleshooting

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

Azure Loading Testing Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered.
See [Azure Load Testing samples][sample_code].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src
[sample_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-developer-loadtesting/src/samples
[api_reference_doc]: https://docs.microsoft.com/rest/api/loadtesting/
[product_documentation]: https://azure.microsoft.com/services/load-testing/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
