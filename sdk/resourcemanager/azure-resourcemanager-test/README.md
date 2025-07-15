# Azure Resource Manager Test shared library for Java

Azure Resource Manager test library for Java

For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-test;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-test</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### (Optional) Install TestProxy

TestProxy will be automatically downloaded when running tests. Though you may need to install it to push the recordings to remote repository.
Refer to [TestProxy documentation](https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md).

## Key concepts

The Azure Resource Manager Test library provides a specialized test base class and utilities for testing Azure Resource Manager SDKs. It extends the core testing framework with ARM-specific functionality.

### Core Components

* **ResourceManagerTestProxyTestBase**: The main test base class that provides Azure Resource Manager-specific testing capabilities. It extends `TestProxyTestBase` from azure-core-test and adds:
  - Azure credential management using `DefaultAzureCredential`
  - Azure profile configuration with tenant, subscription, and environment
  - HTTP pipeline configuration with Azure-specific policies
  - Built-in sanitizers for common Azure secrets and sensitive data
  - Resource naming utilities for generating unique test resource names

* **Test Modes**: Like the core test framework, supports three test modes:
  - **LIVE**: Tests run against actual Azure resources
  - **RECORD**: Tests run against Azure resources and HTTP interactions are recorded
  - **PLAYBACK**: Tests use recorded HTTP interactions without hitting Azure services

* **Azure-specific Sanitization**: Automatically sanitizes common Azure secrets including:
  - Subscription IDs, tenant IDs, and client IDs
  - Storage account keys and connection strings
  - Database passwords and connection strings
  - Service principal secrets and certificates
  - API keys and access tokens

### Test Environment Setup

For LIVE/RECORD tests, you need:
1. Azure CLI authentication (`az login`)
2. Environment variables:
   - `AZURE_TENANT_ID`: Your Azure tenant ID
   - `AZURE_SUBSCRIPTION_ID`: Your target subscription ID
   - `AZURE_CLIENT_ID` and `AZURE_CLIENT_SECRET`: Service principal credentials (optional if using Azure CLI)

### Utility Methods

The base class provides several utility methods:
- `generateRandomResourceName(prefix, maxLen)`: Creates unique resource names for tests
- `generateRandomUuid()`: Generates random UUIDs
- `password()`: Generates random passwords for test resources
- `sshPublicKey()`: Generates SSH public keys for VM tests
- `profile()`: Access to the Azure profile being used
- `isPlaybackMode()`: Check if running in playback mode
- `skipInPlayback()`: Skip test execution in playback mode

## Examples

Here's how to create a test class using ResourceManagerTestProxyTestBase:

```java
public class MyServiceManagerTests extends ResourceManagerTestProxyTestBase {
    private MyServiceManager manager;
    private ResourceManager resourceManager;
    private String resourceGroupName;

    public MyServiceManagerTests() {
        // Add any custom sanitizers in constructor
        addSanitizers(
            new TestProxySanitizer("$.properties.customSecret", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY)
        );
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        // Build HTTP pipeline with your service-specific requirements
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        // Initialize your service manager and any other required clients
        resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        manager = MyServiceManager.authenticate(httpPipeline, profile);
        
        // Set up test context for consistent resource naming
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        setInternalContext(internalContext, manager, resourceManager);
        
        // Generate resource group name for tests
        resourceGroupName = generateRandomResourceName("rg", 20);
    }

    @Override
    protected void cleanUpResources() {
        // Clean up any resources created during tests
        resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
    }

    @Test
    public void testCreateResource() {
        // Write your own test case here
    }
}
```

### Adding Custom Sanitizers

To sanitize service-specific secrets:

```java
public MyServiceTests() {
    // Add custom sanitizers in the constructor
    addSanitizers(
        // Sanitize a specific JSON property
        new TestProxySanitizer("$.properties.apiKey", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
        
        // Sanitize using regex pattern
        new TestProxySanitizer("(?:password=)([^&]+)", REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX),
        
        // Sanitize HTTP headers
        new TestProxySanitizer("X-API-Key", null, REDACTED_VALUE, TestProxySanitizerType.HEADER)
    );
}
```

## Troubleshooting

### Common Issues

**Authentication Errors in Live/Record Mode**
- Ensure you have run `az login` and have valid Azure credentials
- Verify that `AZURE_TENANT_ID` and `AZURE_SUBSCRIPTION_ID` environment variables are set
- Check that your account has sufficient permissions in the target subscription

**Recording Issues**
- Recordings may contain secrets if sanitizers are not properly configured

**Resource Naming Conflicts**
- Use `generateRandomResourceName()` to ensure unique resource names
- Be aware that some Azure resources require globally unique names
- Clean up resources properly in `cleanUpResources()` to avoid conflicts

### Debug Options

Enable detailed HTTP logging by setting the `AZURE_TEST_LOG_LEVEL` environment variable:
```bash
export AZURE_TEST_LOG_LEVEL=BODY_AND_HEADERS
```

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

- Fork it
- Create your feature branch (`git checkout -b my-new-feature`)
- Commit your changes (`git commit -am 'Add some feature'`)
- Push to the branch (`git push origin my-new-feature`)
- Create new Pull Request
