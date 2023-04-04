# Introduction
These files give samples of how to perform simple operations using DevCenter clients. 
You'll need to create backing resources in order to take full advantage of these scenarios.
You can find documentation on these resources [here](https://azure.microsoft.com/services/dev-box/).

The minimum requirements to create Dev Box resources using this SDK are to create DevCenter, Project, and Pool resources.
The minimum requirements to create Environment resources using this SDK are to create DevCenter, Project, EnvironmentType, and CatalogItem resources.

# Examples
## Dev Box samples

## Setup
Set the following environment variables for easy consumption in client code:

- `AZURE_TENANT_ID`: GUID identifier for the Azure tenant
- `DEVCENTER_NAME`: Name of the DevCenter resource


### Creating clients

The project is the top-level resource on the data plane. We'll create a dev center client first to access projects:

```java com.azure.developer.devcenter.readme.createDevCenterClient
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

// Build our clients
DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

The DevBoxes client is created in essentially the same manner:

```java com.azure.developer.devcenter.readme.createDevCenterClient
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

// Build our clients
DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

### Fetching resources

Now we'll fetch available project and pool resources.

```java com.azure.developer.devcenter.readme.getProjectsAndPools
// Find available Projects and Pools
PagedIterable<BinaryData> projectListResponse = devCenterClient.listProjects(null);
for (BinaryData p: projectListResponse) {
    System.out.println(p);
}

PagedIterable<BinaryData> poolListResponse = devBoxClient.listPools("myProject", null);
for (BinaryData p: poolListResponse) {
    System.out.println(p);
}
```

Once we've decided on an available pool, we can provision a Dev Box in the pool, then fetch our connection URL to access the Dev Box.

```java com.azure.developer.devcenter.readme.createAndConnectToDevBox
// Provision a Dev Box
BinaryData devBoxBody = BinaryData.fromString("{\"poolName\":\"MyPool\"}");
SyncPoller<BinaryData, BinaryData> devBoxCreateResponse =
        devBoxClient.beginCreateDevBox("myProject", "me", "MyDevBox", devBoxBody, null);
devBoxCreateResponse.waitForCompletion();


Response<BinaryData> remoteConnectionResponse =
                devBoxClient.getRemoteConnectionWithResponse("myProject", "me", "MyDevBox", null);
System.out.println(remoteConnectionResponse.getValue());
```

And finally, tear down the Dev Box when we're finished:

```java com.azure.developer.devcenter.readme.deleteDevBox
// Tear down the Dev Box when we're finished:
SyncPoller<BinaryData, Void> devBoxDeleteResponse =
                devBoxClient.beginDeleteDevBox("myProject", "me", "MyDevBox", null);
devBoxDeleteResponse.waitForCompletion();        
```

## Environment samples

### Setup
Set the following environment variables for easy consumption in client code:

- `AZURE_TENANT_ID`: GUID identifier for the Azure tenant
- `DEVCENTER_NAME`: Name of the DevCenter resource


### Creating clients

The project is the top-level resource on the data plane. We'll create a dev center client first to access projects:

```java com.azure.developer.devcenter.readme.createDevCenterClient
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

// Build our clients
DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

Environments clients are created in essentially the same manner:


```java com.azure.developer.devcenter.readme.createEnvironmentsClient
DeploymentEnvironmentsClient environmentsClient =
                new DeploymentEnvironmentsClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

### Fetching resources

Now we'll fetch available catalog item, and environment type resources.
```java com.azure.developer.devcenter.readme.getEnvironmentDefinitionsAndEnvironmentTypes
// Fetch available catalog items and environment types
PagedIterable<BinaryData> environmentDefinitionListResponse = environmentsClient.listEnvironmentDefinitions("myProject", null);
for (BinaryData p: environmentDefinitionListResponse) {
    System.out.println(p);
}

PagedIterable<BinaryData> environmentTypesListResponse = environmentsClient.listEnvironmentTypes("myProject", null);
for (BinaryData p: environmentTypesListResponse) {
    System.out.println(p);
}
```

Once we've decided on which catalog item and environment type to use, we can create an environment.

```java com.azure.developer.devcenter.readme.createEnvironment
// Create an environment
BinaryData environmentBody = BinaryData.fromString("{\"catalogItemName\":\"MyCatalogItem\", \"environmentType\":\"MyEnvironmentType\"}");
SyncPoller<BinaryData, BinaryData> environmentCreateResponse =
        environmentsClient.beginCreateOrUpdateEnvironment("myProject", "me", "TestEnvironment", environmentBody, null);
environmentCreateResponse.waitForCompletion();
```

And finally, tear down the environment when we're finished:
```java com.azure.developer.devcenter.readme.deleteEnvironment
// Delete the environment when we're finished:
SyncPoller<BinaryData, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment("myProject", "me", "TestEnvironment", null);
environmentDeleteResponse.waitForCompletion();
```

## Full Examples
### Dev Boxes
```java com.azure.developer.devcenter.readme.devboxes
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

// Build our clients
DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();

DevBoxesClient devBoxClient =
                new DevBoxesClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();

// Find available Projects and Pools
PagedIterable<BinaryData> projectListResponse = devCenterClient.listProjects(null);
for (BinaryData p: projectListResponse) {
    System.out.println(p);
}

PagedIterable<BinaryData> poolListResponse = devBoxClient.listPools("myProject", null);
for (BinaryData p: poolListResponse) {
    System.out.println(p);
}

// Provision a Dev Box
BinaryData devBoxBody = BinaryData.fromString("{\"poolName\":\"MyPool\"}");
SyncPoller<BinaryData, BinaryData> devBoxCreateResponse =
        devBoxClient.beginCreateDevBox("myProject", "me", "MyDevBox", devBoxBody, null);
devBoxCreateResponse.waitForCompletion();


Response<BinaryData> remoteConnectionResponse =
                devBoxClient.getRemoteConnectionWithResponse("myProject", "me", "MyDevBox", null);
System.out.println(remoteConnectionResponse.getValue());

// Tear down the Dev Box when we're finished:
SyncPoller<BinaryData, Void> devBoxDeleteResponse =
                devBoxClient.beginDeleteDevBox("myProject", "me", "MyDevBox", null);
devBoxDeleteResponse.waitForCompletion();        
```

### Environments
```java com.azure.developer.devcenter.readme.environments
DeploymentEnvironmentsClient environmentsClient =
                new DeploymentEnvironmentsClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();

// Fetch available catalog items and environment types
PagedIterable<BinaryData> environmentDefinitionListResponse = environmentsClient.listEnvironmentDefinitions("myProject", null);
for (BinaryData p: environmentDefinitionListResponse) {
    System.out.println(p);
}

PagedIterable<BinaryData> environmentTypesListResponse = environmentsClient.listEnvironmentTypes("myProject", null);
for (BinaryData p: environmentTypesListResponse) {
    System.out.println(p);
}

// Create an environment
BinaryData environmentBody = BinaryData.fromString("{\"catalogItemName\":\"MyCatalogItem\", \"environmentType\":\"MyEnvironmentType\"}");
SyncPoller<BinaryData, BinaryData> environmentCreateResponse =
        environmentsClient.beginCreateOrUpdateEnvironment("myProject", "me", "TestEnvironment", environmentBody, null);
environmentCreateResponse.waitForCompletion();

// Delete the environment when we're finished:
SyncPoller<BinaryData, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment("myProject", "me", "TestEnvironment", null);
environmentDeleteResponse.waitForCompletion();
```

# Troubleshooting

Errors will be returned on the long running operation (LRO) with a descriptive code and message indicating how to resolve the problem.

# Next Steps

Start using the SDK! 

# Contributing

You can learn more about contributing to the SDK [here](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).
