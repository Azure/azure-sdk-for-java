It has been a few months since we first previewed the new Azure management libraries for Java.

Here we will give an quick introduction on highlighted features.

## Prerequisites
- Azure Subscription ([get one for free](https://azure.microsoft.com/en-us/free/))
- Java 8 or later
- Java Resource Management Libraries ([start-up guide](https://aka.ms/azure-sdk-java-mgmt))

# Features

## Familiar Fluent interface

Fluent interface remains the same in new management libraries.

You can create a virtual machine instance, together with required virtual network and ip address created automatically.

```java
VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withNewPrimaryNetwork("10.0.0.0/28")
    .withPrimaryPrivateIPAddressDynamic()
    .withNewPrimaryPublicIPAddress("mylinuxvm")
    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
    .withRootUsername(username)
    .withSsh(sshKey)
    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
    .create();
```

You can create a function app, together with required storage account and app service plan created on specification.

```java
Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts()
    .define(storageAccountName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withGeneralPurposeAccountKindV2()
    .withSku(StorageAccountSkuType.STANDARD_LRS);

Creatable<AppServicePlan> creatableAppServicePlan = azure.appServicePlans()
    .define(appServicePlanName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withPricingTier(PricingTier.STANDARD_S1)
    .withOperatingSystem(OperatingSystem.LINUX);

FunctionApp linuxFunctionApp = azure.functionApps().define(functionAppName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withNewLinuxAppServicePlan(creatableAppServicePlan)
    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
    .withNewStorageAccount(creatableStorageAccount)
    .withHttpsOnly(true)
    .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", functionAppPackageUrl)
    .create();
```

You can batch create and delete managed disk instances.

```java
List<String> diskNames = Arrays.asList("datadisk1", "datadisk2");

List<Creatable<Disk>> creatableDisks = diskNames.stream()
    .map(diskName -> azure.disks()
        .define(diskName)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(rgName)
        .withData()
        .withSizeInGB(10)
        .withSku(DiskSkuTypes.STANDARD_LRS))
    .collect(Collectors.toList());

Collection<Disk> disks = azure.disks().create(creatableDisks).values();

azure.disks().deleteByIds(disks.stream().map(Disk::id).collect(Collectors.toList()));
```

## Authorization via MSAL

[MSAL](https://docs.microsoft.com/en-us/azure/active-directory/develop/msal-overview) integrates with the Microsoft identity platform (v2.0) endpoint.

In new management libraries, MSAL is supported via [Azure Identity](https://azuresdkdocs.blob.core.windows.net/$web/java/azure-identity/latest/index.html) library.

For user new to Azure Resource Manager, if you already logged-in via Azure CLI, you can quickly authenticate through CLI credential.

```java
Azure azure = Azure
    .authenticate(new AzureCliCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
    .withDefaultSubscription();
```

For professional user, a recommended approach is to configure `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH`, and `AZURE_SUBSCRIPTION_ID` as environment variable.

With these environment variables set, you can authenticate as [service principal](https://docs.microsoft.com/en-us/azure/active-directory/develop/app-objects-and-service-principals).

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);    // assume global cloud
TokenCredential credential = new EnvironmentCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();

Azure azure = Azure
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

## HTTP client as plug-in

Previously, OkHttp client is hard-wired as HTTP client of management libraries. This causes some difficulty for user who wishes to use a different HTTP client.

With the new management libraries, you are now free to choose a different HttpClient implementation, or even provide a customized one.

By default, management libraries use the HTTP client loaded in runtime. Specifically, you can configure and use OkHttp client.

```java
HttpClient httpClient = new OkHttpAsyncHttpClientBuilder()
    .addNetworkInterceptor(additionalNetworkInterceptor)
    .connectionTimeout(Duration.ofSeconds(15))
    .readTimeout(Duration.ofMinutes(2))
    .build();

Azure azure = Azure
    .configure()
    .withHttpClient(httpClient)
    ...
```

## Better control over long-running operation

[Long-running operation](https://docs.microsoft.com/en-us/azure/architecture/patterns/async-request-reply) is complicated and previously completely abstracted-away by Fluent interface.

New management libraries offer better control over the provisioning of some frequently used Azure resources.

Assume you have an [ARM template](https://docs.microsoft.com/en-us/azure/azure-resource-manager/templates/) to be deployed to a resource group, and you know it usually takes a bit more than 2 minutes. It is more economical to begin deployment, then delay 2 minutes before polling the result.

```java
final Duration delayedInterval = Duration.ofMinutes(2);
final Duration pollInterval = Duration.ofSeconds(10);

// begin deployment
Accepted<Deployment> acceptedDeployment = azure.deployments()
    .define(deploymentName)
    .withExistingResourceGroup(rgName)
    .withTemplateLink(templateUri, contentVersion)
    .withParametersLink(parametersUri, contentVersion)
    .withMode(DeploymentMode.COMPLETE)
    .beginCreate();
Deployment provisioningDeployment = acceptedDeployment.getActivationResponse().getValue();

LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
long delayInMills = delayedInterval.toMillis();
while (!pollStatus.isComplete()) {
    Thread.sleep(delayInMills);

    // poll
    PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
    pollStatus = pollResponse.getStatus();
    delayInMills = pollInterval.toMillis();
}
// pollStatus == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, if successful

// final result
Deployment deployment = acceptedDeployment.getFinalResult();
```

And it is also common use case, to begin deleting a resource group, then check it back later.

```java
Accepted<Void> acceptedDelete = azure.resourceGroups().beginDeleteByName(rgName);

// do other stuffs

acceptedDelete.getFinalResult();
```

## Consistent exception handling for server error

When server reports error, new management libraries throw `ManagementException`.

`ManagementException.getValue().getCode()` and `ManagementException.getValue().getMessage()` provides error code and error message from the server.

`ManagementException.getResponse()` provides the raw HTTP response.

Technical notes: Error on connection itself is not a `ManagementException`. Its type depends on the HttpClient implementation used.

## New services

New management libraries support [Spring Cloud](https://docs.microsoft.com/en-us/azure/spring-cloud/) service.

```java
SpringService service = azure.springServices().define(serviceName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .create();
```

## Documentation

See [Azure Management Libraries for Java](https://aka.ms/azure-sdk-java-mgmt)

