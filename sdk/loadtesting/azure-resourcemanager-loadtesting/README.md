# Azure Resource Manager LoadTest client library for Java

Azure Resource Manager LoadTest client library for Java.

This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## We'd love to hear your feedback

We're always working on improving our products and the way we communicate with our users. So we'd love to learn what's working and how we can do better.

If you haven't already, please take a few minutes to [complete this short survey][survey] we have put together.

Thank you in advance for your collaboration. We really appreciate your time!

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-loadtesting;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-loadtesting</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java readme-sample-authn
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
LoadTestManager manager = LoadTestManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

### Create a new Azure Load Testing resource

Create an Azure Load Testing resource.

```java readme-sample-createloadtestresource-basic
LoadTestResource resource = manager
    .loadTests()
    .define("sample-loadtesting-resource")
    .withRegion(Region.US_WEST2)
    .withExistingResourceGroup("sample-rg")
    .create();
```

Create an Azure Load Testing resource configured with CMK encryption.

```java readme-sample-createloadtestresource-encryption
// map of user-assigned managed identities to be assigned to the loadtest resource
Map<String, UserAssignedIdentity> map = new HashMap<String, UserAssignedIdentity>();
map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1", new UserAssignedIdentity());

// encryption identity must be assigned to the load test resource, before using it
LoadTestResource resource = manager
    .loadTests()
    .define("sample-loadtesting-resource")
    .withRegion(Region.US_WEST2)
    .withExistingResourceGroup("sample-rg")
    .withIdentity(
        new ManagedServiceIdentity()
        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
        .withUserAssignedIdentities(map)
    )
    .withEncryption(
        new EncryptionProperties()
        .withIdentity(
            new EncryptionPropertiesIdentity()
            .withResourceId("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1")
            .withType(Type.USER_ASSIGNED)
        )
        .withKeyUrl("https://sample-kv.vault.azure.net/keys/cmkkey/2d1ccd5c50234ea2a0858fe148b69cde")
    )
    .create();
```

### Get details of an Azure Load Testing resource

```java readme-sample-getloadtestresource
LoadTestResource resource = manager
    .loadTests()
    .getByResourceGroup("sample-rg", "sample-loadtesting-resource");
```

### Update an Azure Load Testing resource

Update an Azure Load Testing resource to configure CMK encryption using system-assigned managed identity.

```java readme-sample-updateloadtestresource-encryption
LoadTestResource resource = manager
    .loadTests()
    .getByResourceGroup("sample-rg", "sample-loadtesting-resource");

LoadTestResource resourcePostUpdate = resource
    .update()
    .withIdentity(
        new ManagedServiceIdentity()
        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
    )
    .withEncryption(
        new EncryptionProperties()
        .withIdentity(
            new EncryptionPropertiesIdentity()
            .withResourceId(null)
            .withType(Type.SYSTEM_ASSIGNED)
            // make sure that system-assigned managed identity is enabled on the resource and the identity has been granted required permissions to access the key.
        )
        .withKeyUrl("https://sample-kv.vault.azure.net/keys/cmkkey/2d1ccd5c50234ea2a0858fe148b69cde")
    )
    .apply();
```

Update an Azure Load Testing resource to update user-assigned managed identities.

```java readme-sample-updateloadtestresource-mi
Map<String, UserAssignedIdentity> map = new HashMap<String, UserAssignedIdentity>();
// Note: the value of <identity1> set to null, removes the previously assigned managed identity from the load test resource
map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1", null);
map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity2", new UserAssignedIdentity());

LoadTestResource resource = manager
    .loadTests()
    .getByResourceGroup("sample-rg", "sample-loadtesting-resource");

LoadTestResource resourcePostUpdate = resource
    .update()
    .withIdentity(
        new ManagedServiceIdentity()
        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
        .withUserAssignedIdentities(map)
    )
    .apply();
```

### Delete an Azure Load Testing resource

```java readme-sample-deleteloadtestresource
manager
    .loadTests()
    .deleteByResourceGroup("sample-rg", "sample-loadtesting-resource");
```

### Quota Operations

Get quota values for all quota buckets.

```java readme-sample-list-all-quota-buckets
PagedIterable<QuotaResource> resource = manager
    .quotas()
    .list("westus2");

for (QuotaResource quotaResource : resource) {
    // use the quotaResource
    System.out.println(quotaResource.limit());
}
```

Get quota values for a particular quota bucket.

```java readme-sample-get-quota-bucket
QuotaResource resource = manager
    .quotas()
    .get("westus2", "maxConcurrentTestRuns");
System.out.println(resource.limit());
```

Check quota availability.

```java readme-sample-check-quota-availability
QuotaResource resource = manager
    .quotas()
    .get("westus2", "maxConcurrentTestRuns");

QuotaBucketRequestPropertiesDimensions dimensions = new QuotaBucketRequestPropertiesDimensions()
    .withLocation("westus2")
    .withSubscriptionId(manager.serviceClient().getSubscriptionId());

QuotaBucketRequest request = new QuotaBucketRequest()
    .withCurrentQuota(resource.limit())
    .withCurrentUsage(resource.usage())
    .withNewQuota(resource.limit())
    .withDimensions(dimensions);

CheckQuotaAvailabilityResponse availability = manager
    .quotas()
    .checkAvailability("westus2", "maxConcurrentTestRuns", request);

System.out.println(availability.isAvailable());
```

[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/loadtesting/azure-resourcemanager-loadtesting/SAMPLE.md)


## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[survey]: https://microsoft.qualtrics.com/jfe/form/SV_ehN0lIk2FKEBkwd?Q_CHL=DOCS
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
