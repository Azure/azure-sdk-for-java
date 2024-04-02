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
PagedIterable<DevCenterProject> projectListResponse = devCenterClient.listProjects();
for (DevCenterProject project: projectListResponse) {
    System.out.println(project.getName());
}
DevCenterProject project = projectListResponse.iterator().next();
String projectName = project.getName();

PagedIterable<DevBoxPool> poolListResponse = devBoxClient.listPools(projectName);
for (DevBoxPool pool: poolListResponse) {
    System.out.println(pool.getName());
}
DevBoxPool pool = poolListResponse.iterator().next();
String poolName = pool.getName();
```

Once we've decided on an available pool, we can provision a Dev Box in the pool, then fetch our connection URL to access the Dev Box.

```java com.azure.developer.devcenter.readme.createAndConnectToDevBox
// Provision a Dev Box
SyncPoller<DevCenterOperationDetails, DevBox> devBoxCreateResponse =
                devBoxClient.beginCreateDevBox(projectName, "me", new DevBox("MyDevBox", poolName));
devBoxCreateResponse.waitForCompletion();
DevBox devBox = devBoxCreateResponse.getFinalResult();
String devBoxName = devBox.getName();
System.out.println("DevBox " + devBoxName + "finished provisioning with status " + devBox.getProvisioningState());

RemoteConnection remoteConnection =
                devBoxClient.getRemoteConnection(projectName, "me", devBoxName);
System.out.println(remoteConnection.getWebUrl());
```

And finally, tear down the Dev Box when we're finished:

```java com.azure.developer.devcenter.readme.deleteDevBox
// Tear down the Dev Box when we're finished:
SyncPoller<DevCenterOperationDetails, Void> devBoxDeleteResponse =
                devBoxClient.beginDeleteDevBox(projectName, "me", devBoxName);
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

Now we'll fetch available catalogs, environment definitions, and environment type resources.
```java com.azure.developer.devcenter.readme.getEnvironmentDefinitionsAndTypes
// Fetch available environment definitions and environment types
PagedIterable<DevCenterCatalog> catalogs = environmentsClient.listCatalogs(projectName);
for (DevCenterCatalog catalog: catalogs) {
    System.out.println(catalog.getName());
}
String catalogName = catalogs.iterator().next().getName();

PagedIterable<EnvironmentDefinition> environmentDefinitions = environmentsClient.listEnvironmentDefinitionsByCatalog(projectName, catalogName);
for (EnvironmentDefinition environmentDefinition: environmentDefinitions) {
    System.out.println(environmentDefinition.getName());
}
String envDefinitionName = environmentDefinitions.iterator().next().getName();

PagedIterable<DevCenterEnvironmentType> environmentTypes = environmentsClient.listEnvironmentTypes(projectName);
for (DevCenterEnvironmentType envType: environmentTypes) {
    System.out.println(envType.getName());
}
String envTypeName = environmentTypes.iterator().next().getName();
```

Once we've decided on which environment definition and environment type to use, we can create an environment.

```java com.azure.developer.devcenter.readme.createEnvironment
// Create an environment
SyncPoller<DevCenterOperationDetails, DevCenterEnvironment> environmentCreateResponse 
            = environmentsClient.beginCreateOrUpdateEnvironment(projectName, "me",
                new DevCenterEnvironment("myEnvironmentName", envTypeName, catalogName, envDefinitionName));
environmentCreateResponse.waitForCompletion();
DevCenterEnvironment environment = environmentCreateResponse.getFinalResult();
String environmentName = environment.getName();
```

And finally, tear down the environment when we're finished:
```java com.azure.developer.devcenter.readme.deleteEnvironment
// Delete the environment when we're finished:
SyncPoller<DevCenterOperationDetails, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment(projectName, "me", environmentName);
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
PagedIterable<DevCenterProject> projectListResponse = devCenterClient.listProjects();
for (DevCenterProject project: projectListResponse) {
    System.out.println(project.getName());
}
DevCenterProject project = projectListResponse.iterator().next();
String projectName = project.getName();

PagedIterable<DevBoxPool> poolListResponse = devBoxClient.listPools(projectName);
for (DevBoxPool pool: poolListResponse) {
    System.out.println(pool.getName());
}
DevBoxPool pool = poolListResponse.iterator().next();
String poolName = pool.getName();

// Provision a Dev Box
SyncPoller<DevCenterOperationDetails, DevBox> devBoxCreateResponse =
                devBoxClient.beginCreateDevBox(projectName, "me", new DevBox("MyDevBox", poolName));
devBoxCreateResponse.waitForCompletion();
DevBox devBox = devBoxCreateResponse.getFinalResult();
String devBoxName = devBox.getName();
System.out.println("DevBox " + devBoxName + "finished provisioning with status " + devBox.getProvisioningState());

RemoteConnection remoteConnection =
                devBoxClient.getRemoteConnection(projectName, "me", devBoxName);
System.out.println(remoteConnection.getWebUrl());

// Tear down the Dev Box when we're finished:
SyncPoller<DevCenterOperationDetails, Void> devBoxDeleteResponse =
                devBoxClient.beginDeleteDevBox(projectName, "me", devBoxName);
devBoxDeleteResponse.waitForCompletion();
```

### Environments
```java com.azure.developer.devcenter.readme.environments
DeploymentEnvironmentsClient environmentsClient =
                    new DeploymentEnvironmentsClientBuilder()
                            .endpoint(endpoint)
                            .credential(new DefaultAzureCredentialBuilder().build())
                            .buildClient();

// Fetch available environment definitions and environment types
PagedIterable<DevCenterCatalog> catalogs = environmentsClient.listCatalogs(projectName);
for (DevCenterCatalog catalog: catalogs) {
    System.out.println(catalog.getName());
}
String catalogName = catalogs.iterator().next().getName();

PagedIterable<EnvironmentDefinition> environmentDefinitions = environmentsClient.listEnvironmentDefinitionsByCatalog(projectName, catalogName);
for (EnvironmentDefinition environmentDefinition: environmentDefinitions) {
    System.out.println(environmentDefinition.getName());
}
String envDefinitionName = environmentDefinitions.iterator().next().getName();

PagedIterable<DevCenterEnvironmentType> environmentTypes = environmentsClient.listEnvironmentTypes(projectName);
for (DevCenterEnvironmentType envType: environmentTypes) {
    System.out.println(envType.getName());
}
String envTypeName = environmentTypes.iterator().next().getName();

// Create an environment
SyncPoller<DevCenterOperationDetails, DevCenterEnvironment> environmentCreateResponse 
            = environmentsClient.beginCreateOrUpdateEnvironment(projectName, "me",
                new DevCenterEnvironment("myEnvironmentName", envTypeName, catalogName, envDefinitionName));
environmentCreateResponse.waitForCompletion();
DevCenterEnvironment environment = environmentCreateResponse.getFinalResult();
String environmentName = environment.getName();

// Delete the environment when we're finished:
SyncPoller<DevCenterOperationDetails, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment(projectName, "me", environmentName);
environmentDeleteResponse.waitForCompletion();
```

# Troubleshooting

Errors will be returned on the long running operation (LRO) with a descriptive code and message indicating how to resolve the problem.

# Next Steps

Start using the SDK! 

# Contributing

You can learn more about contributing to the SDK [here](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).
