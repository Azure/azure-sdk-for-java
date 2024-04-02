# Azure DevCenter client library for Java

This package contains Microsoft Azure DevCenter client library.

## Documentation

Various documentation is available to help you get started

- [Product documentation: Azure Deployment Environments][environments_documentation]
- [Product documentation: Microsoft Dev Box][devbox_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- The minimum requirements to create Dev Box resources using this SDK are to create DevCenter, Project, and Pool resources.
- The minimum requirements to create Environment resources using this SDK are to create DevCenter, Project, EnvironmentType, and CatalogItem resources.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-developer-devcenter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-developer-devcenter</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

## Examples
### Dev Box Scenarios
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

### Environments Scenarios
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
[environments_documentation]: https://learn.microsoft.com/azure/deployment-environments/
[devbox_documentation]: https://learn.microsoft.com/azure/dev-box/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
