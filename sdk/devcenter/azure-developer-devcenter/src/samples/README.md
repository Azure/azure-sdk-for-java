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

- `DEVCENTER_ENDPOINT` : Endpoint of the DevCenter resource

### Creating clients

The project is the top-level resource on the data plane. We'll create a dev center client first to access projects:

```java com.azure.developer.devcenter.readme.createDevCenterClient
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

The DevBoxes client is created in essentially the same manner:

```java com.azure.developer.devcenter.readme.createDevBoxClient
DevBoxesClient devBoxClient =
                new DevBoxesClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
```

Or it can be instantiated using DevCenter client, sharing same endpoint and credential:

```java com.azure.developer.devcenter.readme.getDevBoxClient
DevBoxesClient devBoxClient = devCenterClient.getDevBoxesClient();
```

### Fetching resources

Now we'll fetch available project and pool resources.

```java com.azure.developer.devcenter.readme.getProjectsAndPools
// Find available Projects and Pools
PagedIterable<DevCenterProject> projectListResponse = devCenterClient.listProjects();
for (DevCenterProject project: projectListResponse) {
    System.out.println(project.getName());
}

// Use the first project in the list
DevCenterProject project = projectListResponse.iterator().next();
String projectName = project.getName();

PagedIterable<DevBoxPool> poolListResponse = devBoxClient.listPools(projectName);
for (DevBoxPool pool: poolListResponse) {
    System.out.println(pool.getName());
}

// Use the first pool in the list
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
System.out.println("Dev Box web url is " + remoteConnection.getWebUrl());
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

- `DEVCENTER_ENDPOINT` : Endpoint of the DevCenter resource

### Creating clients

The project is the top-level resource on the data plane. We'll create a dev center client first to access projects:

```java com.azure.developer.devcenter.readme.createDevCenterClient
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

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

Or it can be instantiated using DevCenter client, sharing same endpoint and credential:

```java com.azure.developer.devcenter.readme.getEnvironmentsClient
DeploymentEnvironmentsClient environmentsClient = devCenterClient.getDeploymentEnvironmentsClient();
```

### Fetching resources

Now we'll fetch available catalogs, environment definitions, and environment type resources.
```java com.azure.developer.devcenter.readme.getEnvironmentDefinitionsAndTypes
// Fetch available environment definitions and environment types
PagedIterable<DevCenterCatalog> catalogs = environmentsClient.listCatalogs(projectName);
for (DevCenterCatalog catalog: catalogs) {
    System.out.println(catalog.getName());
}

// Use the first catalog in the list
String catalogName = catalogs.iterator().next().getName();

PagedIterable<EnvironmentDefinition> environmentDefinitions = environmentsClient.listEnvironmentDefinitionsByCatalog(projectName, catalogName);
for (EnvironmentDefinition environmentDefinition: environmentDefinitions) {
    System.out.println(environmentDefinition.getName());
}

// Use the first environment definition in the list
String envDefinitionName = environmentDefinitions.iterator().next().getName();

PagedIterable<DevCenterEnvironmentType> environmentTypes = environmentsClient.listEnvironmentTypes(projectName);
for (DevCenterEnvironmentType envType: environmentTypes) {
    System.out.println(envType.getName());
}

// Use the first environment type in the list
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
```

And finally, tear down the environment when we're finished:
```java com.azure.developer.devcenter.readme.deleteEnvironment
// Delete the environment when we're finished:
SyncPoller<DevCenterOperationDetails, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment(projectName, "me", environmentName);
environmentDeleteResponse.waitForCompletion();
System.out.println("Done deleting environment" + environmentName);
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

DevBoxesClient devBoxClient = devCenterClient.getDevBoxesClient();

// Find available Projects and Pools
PagedIterable<DevCenterProject> projectListResponse = devCenterClient.listProjects();
for (DevCenterProject project: projectListResponse) {
    System.out.println(project.getName());
}

// Use the first project in the list
DevCenterProject project = projectListResponse.iterator().next();
String projectName = project.getName();

PagedIterable<DevBoxPool> poolListResponse = devBoxClient.listPools(projectName);
for (DevBoxPool pool: poolListResponse) {
    System.out.println(pool.getName());
}

// Use the first pool in the list
DevBoxPool pool = poolListResponse.iterator().next();
String poolName = pool.getName();

System.out.println("Starting to create dev box in project " + projectName + " and pool " + poolName);

// Provision a Dev Box
SyncPoller<DevCenterOperationDetails, DevBox> devBoxCreateResponse =
                devBoxClient.beginCreateDevBox(projectName, "me", new DevBox("MyDevBox", poolName));
devBoxCreateResponse.waitForCompletion();
DevBox devBox = devBoxCreateResponse.getFinalResult();

String devBoxName = devBox.getName();
System.out.println("DevBox " + devBoxName + "finished provisioning with status " + devBox.getProvisioningState());

RemoteConnection remoteConnection =
                devBoxClient.getRemoteConnection(projectName, "me", devBoxName);
System.out.println("Dev Box web url is " + remoteConnection.getWebUrl());

System.out.println("Start deleting dev box");
// Tear down the Dev Box when we're finished:
SyncPoller<DevCenterOperationDetails, Void> devBoxDeleteResponse =
                devBoxClient.beginDeleteDevBox(projectName, "me", devBoxName);
devBoxDeleteResponse.waitForCompletion();
System.out.println("Done deleting dev box");
```

### Environments
```java com.azure.developer.devcenter.readme.environments
String endpoint = Configuration.getGlobalConfiguration().get("DEVCENTER_ENDPOINT");

// Build our clients
DevCenterClient devCenterClient =
                new DevCenterClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
       
DeploymentEnvironmentsClient environmentsClient = devCenterClient.getDeploymentEnvironmentsClient();

// Find available Projects 
PagedIterable<DevCenterProject> projectListResponse = devCenterClient.listProjects();
for (DevCenterProject project: projectListResponse) {
    System.out.println(project.getName());
}

// Use the first project in the list
DevCenterProject project = projectListResponse.iterator().next();
String projectName = project.getName();

// Fetch available environment definitions and environment types
PagedIterable<DevCenterCatalog> catalogs = environmentsClient.listCatalogs(projectName);
for (DevCenterCatalog catalog: catalogs) {
    System.out.println(catalog.getName());
}

// Use the first catalog in the list
String catalogName = catalogs.iterator().next().getName();

PagedIterable<EnvironmentDefinition> environmentDefinitions = environmentsClient.listEnvironmentDefinitionsByCatalog(projectName, catalogName);
for (EnvironmentDefinition environmentDefinition: environmentDefinitions) {
    System.out.println(environmentDefinition.getName());
}

// Use the first environment definition in the list
String envDefinitionName = environmentDefinitions.iterator().next().getName();

PagedIterable<DevCenterEnvironmentType> environmentTypes = environmentsClient.listEnvironmentTypes(projectName);
for (DevCenterEnvironmentType envType: environmentTypes) {
    System.out.println(envType.getName());
}

// Use the first environment type in the list
String envTypeName = environmentTypes.iterator().next().getName();

System.out.println("Starting to create environment in project " + projectName + ", with catalog " + catalogName
    + ", environment definition " + envDefinitionName + ", environment type " + envTypeName);

// Create an environment
SyncPoller<DevCenterOperationDetails, DevCenterEnvironment> environmentCreateResponse 
            = environmentsClient.beginCreateOrUpdateEnvironment(projectName, "me",
                new DevCenterEnvironment("myEnvironmentName", envTypeName, catalogName, envDefinitionName));
environmentCreateResponse.waitForCompletion();
DevCenterEnvironment environment = environmentCreateResponse.getFinalResult();

String environmentName = environment.getName();
System.out.println("Environment " + environmentName + "finished provisioning with status " + environment.getProvisioningState());

System.out.println("Start deleting environment " + environmentName);
// Delete the environment when we're finished:
SyncPoller<DevCenterOperationDetails, Void> environmentDeleteResponse =
                environmentsClient.beginDeleteEnvironment(projectName, "me", environmentName);
environmentDeleteResponse.waitForCompletion();
System.out.println("Done deleting environment" + environmentName);
```

# Troubleshooting

Errors will be returned on the long running operation (LRO) with a descriptive code and message indicating how to resolve the problem.

# Next Steps

Start using the SDK! 

# Contributing

You can learn more about contributing to the SDK [here](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).
