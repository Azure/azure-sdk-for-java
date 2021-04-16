# Azure Container Registry client library for Java

Azure Container Registry allows you to store and manage container images and artifacts in a private registry for all types of container deployments.

Use the client library for Azure Container Registry to:

- List images or artifacts in a registry
- Obtain metadata for images and artifacts, repositories and tags
- Set read/write/delete properties on registry items
- Delete images and artifacts, repositories and tags

[Source code][source_code] | [Package (Maven)][package] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [App Configuration Store][app_config_store]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-containers-containerregistry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-containers-containerregistry</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

The [Azure Identity library][identity] provides easy Azure Active Directory support for authentication.

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L21-L25 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L29-L33 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildAsyncClient();
```


Note that these samples assume you have an endpoint, which is the URL including the name of the login server and the `https://` prefix.

For more information on using AAD with Azure Container Registry, please see the service's [Authentication Overview](https://docs.microsoft.com/azure/container-registry/container-registry-authentication).

## Key concepts

A **registry** stores Docker images and [OCI Artifacts](https://opencontainers.org/).  An image or artifact consists of a **manifest** and **layers**.  An image's manifest describes the layers that make up the image, and is uniquely identified by its **digest**.  An image can also be "tagged" to give it a human-readable alias.  An image or artifact can have zero or more **tags** associated with it, and each tag uniquely identifies the image.  A collection of images that share the same name but have different tags, is referred to as a **repository**.

For more information please see [Container Registry Concepts](https://docs.microsoft.com/azure/container-registry/container-registry-concepts).

## Examples

### Sync examples

- [List repositories](#list-repositories)

### Async examples

- [List repositories asynchronously](#list-repositories-asynchronously)

### List repositories

Iterate through the collection of repositories in the registry.

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L37-L43 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();

client.listRepositories().forEach(repository -> System.out.println(repository));
```

### List repositories asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L47-L53 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildAsyncClient();

client.listRepositories().subscribe(repository -> System.out.println(repository));
```

## Troubleshooting

All container registry service operations will throw a
[HttpResponseException][HttpResponseException] on failure.

`<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L59-L69 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRepositoryClient client = new ContainerRepositoryClientBuilder()
    .endpoint(endpoint)
    .repository(repository)
    .credential(credential)
    .buildClient();
try {
    client.getProperties();
} catch (HttpResponseException exception) {
    // Do something with the exception.
}
```

## Next steps

- Go further with azure-containers-containerregistry and our [samples][samples]
- Watch a [demo or deep dive video](https://azure.microsoft.com/resources/videos/index/?service=container-registry)
- Read more about the [Azure Container Registry service](https://docs.microsoft.com/azure/container-registry/container-registry-intro)

## Contributing

This project welcomes contributions and suggestions.  Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution. For
details, visit [cla.microsoft.com][cla].

This project has adopted the [Microsoft Open Source Code of Conduct][coc].
For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any
additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/containerregistry/azure-containers-containerregistry/src
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://search.maven.org/artifact/com.azure/azure-containers-containerregisty
[api_documentation]: https://aka.ms/java-docs
[rest_docs]: https://docs.microsoft.com/rest/api/containerregistry/
[product_docs]:  https://docs.microsoft.com/azure/container-registry
[container_registry_docs]: https://docs.microsoft.com/azure/container-registry/container-registry-intro
[container_registry_create_ps]: https://docs.microsoft.com/azure/container-registry/container-registry-get-started-powershell
[container_registry_create_cli]: https://docs.microsoft.com/azure/container-registry/container-registry-get-started-azure-cli
[container_registry_create_portal]: https://docs.microsoft.com/azure/container-registry/container-registry-get-started-portal
[container_registry_concepts]: https://docs.microsoft.com/azure/container-registry/container-registry-concepts
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/containerregistry/azure-containers-containerregistry/src/samples/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcontainerregistry%2Fazure-contianers-containerregistry%2FREADME.png)
