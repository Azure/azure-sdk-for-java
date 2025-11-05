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

Here's how to create a test class for RECORD/PLAYBACK using ResourceManagerTestProxyTestBase:

### For azure-resourcemanager libraries

```java
public class AzureResourceManagerTests extends ResourceManagerTestProxyTestBase {
    private AzureResourceManager manager;
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
        // Set up correct delay configuration for recording/playback. In RECORD mode, LROs (long-running operations) need a configured delay between each polling. While in PLAYBACK, no delay is needed.
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        // Set up namer context for recording/playback, in case random names are used for implicit resource creation. Otherwise, PLAYBACK won't be able to pick up the names during RECORD.
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        
        // Initialize your service manager and any other required clients
        resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        manager = AzureResourceManager.authenticate(httpPipeline, profile);
        
        // Reflectively set the test context for Manager classes
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

### For libraries not listed in azure-resourcemanager

Mostly same as [For azure-resourcemanager libraries](#for-azure-resourcemanager-libraries). 
Except that in `initializeClient`, you'll need to set the defaultPollInterval in the entry class(XXManager).
```java
ContainerAppsApiManager manager = ContainerAppsApiManager
    .configure()
    .withDefaultPollInterval(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(30)))
    .withHttpClient(httpPipeline.getHttpClient())
    .authenticate(new DefaultAzureCredentialBuilder().build(), profile);

// instead of ContainerAppsApiManager.authenticate(httpPipeline, profile);
```

## Record test

When updating SDK for service api-version upgrade, you may want to run recording tests and update test recordings.

### Run tests in recording mode

Make sure you have `AZURE_TEST_MODE`, `AZURE_TENANT_ID` and `AZURE_SUBSCRIPTION_ID` environment variables properly set.

Either run mvn command:
```
mvn test -f sdk/<service>/azure-resourcemanager-<service>/pom.xml -DAZURE_TEST_MODE=RECORD -DAZURE_TENANT_ID=<tenant-id> -DAZURE_SUBSCRIPTION_ID=<subscription-id>
```

Or individual tests in your IDE. Local test recordings will be automatically updated.

### Push test recordings update

To update recording file (assets.json), refer to [Update test recordings](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/README.md#update-test-recordings).

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

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request
