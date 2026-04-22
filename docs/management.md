# Management Libraries

> **Reference:** <https://aka.ms/azsdk/java/mgmt>

Management libraries (`com.azure.resourcemanager.*`) allow you to provision and manage Azure resources by mirroring the Azure Resource Manager (ARM) REST API.

---

## Quick Start

```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager</artifactId>
  <version>2.44.0</version>
</dependency>
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.13.0</version>
</dependency>
```

---

## Authentication

All management clients authenticate via `AzureProfile` + `TokenCredential`:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();

AzureResourceManager azure = AzureResourceManager
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

### Specific Subscription

```java
AzureResourceManager azure = AzureResourceManager
    .authenticate(credential, profile)
    .withSubscription("<subscription-id>");
```

### Government / Sovereign Cloud

```java
AzureProfile profile = new AzureProfile("<tenant-id>", "<subscription-id>",
    AzureEnvironment.AZURE_US_GOVERNMENT);
TokenCredential credential = new ClientSecretCredentialBuilder()
    .authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT)
    .tenantId("<tenant-id>")
    .clientId("<client-id>")
    .clientSecret("<client-secret>")
    .build();

AzureResourceManager azure = AzureResourceManager
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

---

## Client Configuration

### Logging

```java
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

### Retry Policy

```java
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withRetryPolicy(new RetryPolicy("Retry-After", ChronoUnit.SECONDS))
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

### Custom HTTP Client

```java
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withHttpClient(new OkHttpAsyncHttpClientBuilder().build())
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

---

## Synchronous Calls

Most management operations are synchronous by default:

```java
// Create a resource group
ResourceGroup resourceGroup = azure.resourceGroups()
    .define("myResourceGroup")
    .withRegion(Region.US_EAST)
    .create();

// Get a resource
ResourceGroup rg = azure.resourceGroups().getByName("myResourceGroup");

// List resources
azure.resourceGroups().list().forEach(group ->
    System.out.println(group.name()));

// Delete a resource
azure.resourceGroups().deleteByName("myResourceGroup");
```

---

## Asynchronous Calls

All management clients expose async variants via Project Reactor:

```java
// Create async
azure.resourceGroups()
    .define("myResourceGroup")
    .withRegion(Region.US_EAST)
    .createAsync()
    .subscribe(rg -> System.out.println("Created: " + rg.name()));

// List async
azure.resourceGroups()
    .listAsync()
    .subscribe(rg -> System.out.println(rg.name()));
```

---

## Long-Running Operations (LRO)

Most create/update/delete operations on Azure resources are LROs. The SDK handles polling automatically via its built-in `SyncPoller` / `PollerFlux`:

```java
// The .create() / .update() methods block until the LRO completes
VirtualMachine vm = azure.virtualMachines()
    .define("myVM")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup("myRG")
    // ...
    .create();  // blocks until VM is fully provisioned

// For non-blocking LRO:
azure.virtualMachines()
    .define("myVM")
    // ...
    .createAsync()      // returns Mono<VirtualMachine>
    .subscribe(v -> System.out.println("VM ready: " + v.name()));
```

---

## Generate Code from TypeSpec

Management SDK code is generated from TypeSpec specs in [azure-rest-api-specs](https://github.com/Azure/azure-rest-api-specs). For contributor workflow details, see the [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md).


---

## More Documentation

- [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt) — comprehensive user guide on docs.microsoft.com
- [azure-resourcemanager README](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/sdk/resourcemanager/azure-resourcemanager/README.md)

---

## See Also

- [Azure Identity Examples](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/identity-examples.md)
- [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md)
- [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md)
