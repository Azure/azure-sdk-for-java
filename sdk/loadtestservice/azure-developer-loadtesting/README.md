# Azure Developer LoadTesting client library for Java

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

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-developer-loadtesting;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-developer-loadtesting</artifactId>
    <version>1.0.0-beta.1</version>
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

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
TestClientBuilder testClientBuilder = new TestClientBuilder();
TestClient testClient = testClientBuilder
    .credential(credential)
    .endpoint("<Enter Azure Load Testing Data-Plane URL>")
    .buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

Authentication in this SDK is similar to Azure Management Libraries. See [Authentication][authenticate] for more options.

An important distinction is that Azure Management libraries use `authenticate` method to accept credential along with Azure profile, where this SDK uses `credential` method and only accept the credential parameter.

## Key concepts

The following components make up the Azure Load Testing Service. The Azure Load Test client library for Python allows you to interact with each of these components through the use of a dedicated client object.

### Load testing resource

The Load testing resource is the top-level resource for your load-testing activities. This resource provides a centralized place to view and manage load tests, test results, and related artifacts. A load testing resource contains zero or more load tests.

### Test

A test specifies the test script, and configuration settings for running a load test. You can create one or more tests in an Azure Load Testing resource.

### Test Engine

A test engine is computing infrastructure that runs the Apache JMeter test script. You can scale out your load test by configuring the number of test engines. The test script runs in parallel across the specified number of test engines.

### Test Run

A test run represents one execution of a load test. It collects the logs associated with running the Apache JMeter script, the load test YAML configuration, the list of app components to monitor, and the results of the test.

### App Component

When you run a load test for an Azure-hosted application, you can monitor resource metrics for the different Azure application components (server-side metrics). While the load test runs, and after completion of the test, you can monitor and analyze the resource metrics in the Azure Load Testing dashboard.

### Metrics

During a load test, Azure Load Testing collects metrics about the test execution. There are two types of metrics:

1. Client-side metrics give you details reported by the test engine. These metrics include the number of virtual users, the request response time, the number of failed requests, or the number of requests per second.

2. Server-side metrics are available for Azure-hosted applications and provide information about your Azure application components. Metrics can be for the number of database reads, the type of HTTP responses, or container resource consumption.

## Examples

### Creating a Load Test

```java java-readme-sample-createTest
LoadTestingClient client = new LoadTestingClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<endpoint>")
    .buildClient();

Map<String, Object> testMap = new HashMap<String, Object>();
testMap.put("displayName", "Sample Display Name");
testMap.put("description", "Sample Description");

Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
loadTestConfigMap.put("engineInstances", 1);
testMap.put("loadTestConfig", loadTestConfigMap);

Map<String, Object> envVarMap = new HashMap<String, Object>();
envVarMap.put("a", "b");
envVarMap.put("x", "y");
testMap.put("environmentVariables", envVarMap);

BinaryData test = BinaryData.fromObject(testMap);

Response<BinaryData> testOutResponse = client.getLoadTestAdministration().createOrUpdateTestWithResponse("test12345", test, null);
System.out.println(testOutResponse.getValue().toString());
```

### Uploading .jmx file to a Load Test

```java java-readme-sample-uploadTestFile
LoadTestingClient client = new LoadTestingClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<endpoint>")
    .buildClient();

BinaryData fileData = BinaryData.fromFile(new File("path/to/file").toPath());
Response<BinaryData> fileUrlOut = client.getLoadTestAdministration().uploadTestFileWithResponse("test12345", "file12345", "sample-file.jmx", fileData, null);
System.out.println(fileUrlOut.getValue().toString());
```

### Running a Load Test

```java java-readme-sample-runTest
LoadTestingClient client = new LoadTestingClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("<endpoint>")
    .buildClient();

Map<String, Object> testRunMap = new HashMap<String, Object>();
testRunMap.put("testId", "test12345");
testRunMap.put("displayName", "SDK-Created-TestRun");

Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
loadTestConfigMap.put("engineInstances", 2);
testRunMap.put("loadTestConfig", loadTestConfigMap);

BinaryData testRun = BinaryData.fromObject(testRunMap);

Response<BinaryData> testRunOut = client.getLoadTestRun().createAndUpdateTestRunWithResponse("testrun12345", testRun, null);
System.out.println(testRunOut.getValue().toString());
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
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
