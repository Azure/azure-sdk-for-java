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
  <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

The [Azure Identity library][identity] provides easy Azure Active Directory support for authentication.
Note all the below samples assume you have an endpoint, which is the URL including the name of the login server and the `https://` prefix.
<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L29-L33 -->
```Java
    ContainerRegistryClient client = new ContainerRegistryClientBuilder()
        .endpoint(endpoint)
        .credential(credential)
        .buildClient();
}
```

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L37-L41 -->
```Java
    ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
        .endpoint(endpoint)
        .credential(credential)
        .buildAsyncClient();
}
```

For more information on using AAD with Azure Container Registry, please see the service's [Authentication Overview](https://docs.microsoft.com/azure/container-registry/container-registry-authentication).

### Anonymous access support
If the builder is instantiated without any credentials, the SDK creates the service client for the anonymous pull mode.
The user must use this setting on a registry that has been enabled for anonymous pull.
In this mode, the user can only call listRepositoryNames method and its overload. All the other calls will fail. 
For more information please read [Anonymous Pull Access](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-faq#how-do-i-enable-anonymous-pull-access)

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L80-L82 -->
```Java
ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L86-L88 -->
```Java
ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildAsyncClient();
```

## Key concepts

A **registry** stores Docker images and [OCI Artifacts](https://opencontainers.org/).  An image or artifact consists of a **manifest** and **layers**.  An image's manifest describes the layers that make up the image, and is uniquely identified by its **digest**.  An image can also be "tagged" to give it a human-readable alias.  An image or artifact can have zero or more **tags** associated with it, and each tag uniquely identifies the image.  A collection of images that share the same name but have different tags, is referred to as a **repository**.

For more information please see [Container Registry Concepts](https://docs.microsoft.com/azure/container-registry/container-registry-concepts).

## Examples

### Sync examples

- [List repository names](#samples)
- [Delete images](#samples)
- [Set artifact properties](#samples)
- [List tags with anonymous access](#samples)
- [Delete repository with anonymous access throws](#samples)

### Async examples

- [List repository names asynchronously](#samples)
- [Delete images asynchronously](#samples)
- [Set artifact properties asynchronously](#samples)
- [List tags with anonymous access asynchronously](#samples)
- [Delete repository with anonymous access asynchronously throws](#samples)

### List repository names

Iterate through the collection of repositories in the registry.

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L44-L50 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();

client.listRepositoryNames().forEach(repository -> System.out.println(repository));
```

### List repository names asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L54-L60 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildAsyncClient();

client.listRepositoryNames().subscribe(repository -> System.out.println(repository));
```

### Delete Images

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L92-L120 -->
```Java
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(defaultCredential)
    .buildClient();

final int imagesCountToKeep = 3;
for (String repositoryName : client.listRepositoryNames()) {
    final ContainerRepository repository = client.getRepository(repositoryName);

    // Obtain the images ordered from newest to oldest
    PagedIterable<ArtifactManifestProperties> imageManifests =
        repository.listManifests(
            ManifestOrderBy.LAST_UPDATED_ON_DESCENDING,
            Context.NONE);

    imageManifests.stream().skip(imagesCountToKeep)
        .forEach(imageManifest -> {
            System.out.printf(String.format("Deleting image with digest %s.%n", imageManifest.getDigest()));
            System.out.printf("    This image has the following tags: ");

            for (String tagName : imageManifest.getTags()) {
                System.out.printf("        %s:%s", imageManifest.getRepositoryName(), tagName);
            }

            repository.getArtifact(imageManifest.getDigest()).delete();
        });
}
```

### Delete images asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L124-L149 -->
```Java
nCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

ainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
.endpoint(endpoint)
.credential(defaultCredential)
.buildAsyncClient();

l int imagesCountToKeep = 3;
nt.listRepositoryNames()
.map(repositoryName -> client.getRepository(repositoryName))
.flatMap(repository -> repository.listManifests(
    ManifestOrderBy.LAST_UPDATED_ON_DESCENDING))
.skip(imagesCountToKeep).subscribe(imageManifest -> {
    System.out.printf(String.format("Deleting image with digest %s.%n", imageManifest.getDigest()));
    System.out.printf("    This image has the following tags: ");

    for (String tagName : imageManifest.getTags()) {
        System.out.printf("        %s:%s", imageManifest.getRepositoryName(), tagName);
    }

    client.getArtifact(
        imageManifest.getRepositoryName(),
        imageManifest.getDigest()).delete().subscribe();
}, error -> {
    System.out.println("Failed to delete older images.");
});
```

### Set artifact properties

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L155-L168 -->
```Java
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

ContainerRegistryClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(defaultCredential)
    .buildClient();

RegistryArtifact image = client.getArtifact(repositoryName, tagOrDigest);

image.setTagProperties(
    tag,
    new ArtifactTagProperties()
        .setWriteEnabled(false)
        .setDeleteEnabled(false));
```

### Set artifact properties asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L172-L187 -->
```Java
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

ContainerRegistryAsyncClient client = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(defaultCredential)
    .buildAsyncClient();

RegistryArtifactAsync image = client.getArtifact(repositoryName, tagOrDigest);

image.setTagProperties(tag, new ArtifactTagProperties()
    .setWriteEnabled(false)
    .setDeleteEnabled(false)).subscribe(artifactTagProperties -> {
        System.out.println("Tag properties are now read-only");
    }, error -> {
        System.out.println("Failed to make the tag properties read-only.");
    });
```

### List tags with anonymous access

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L195-L206 -->
```Java
ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildClient();

RegistryArtifact image = anonymousClient.getArtifact(repositoryName, tagOrDigest);
PagedIterable<ArtifactTagProperties> tags = image.listTags();

System.out.printf(String.format("%s has the following aliases:", image.getFullyQualifiedName()));

for (ArtifactTagProperties tag : tags) {
    System.out.printf(String.format("%s/%s:%s", anonymousClient.getName(), repositoryName, tag.getName()));
}
```

### List tags with anonymous access asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L210-L225 -->
```Java
final String endpoint = getEndpoint();
final String repositoryName = getRepositoryName();

ContainerRegistryAsyncClient anonymousClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildAsyncClient();

RegistryArtifactAsync image = anonymousClient.getArtifact(repositoryName, tagOrDigest);

System.out.printf(String.format("%s has the following aliases:", image.getFullyQualifiedName()));

image.listTags().subscribe(tag -> {
    System.out.printf(String.format("%s/%s:%s", anonymousClient.getName(), repositoryName, tag.getName()));
}, error -> {
    System.out.println("There was an error while trying to list tags" + error);
});
```

### Delete repository with anonymous access throws 
<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L229-L241 -->
```Java
final String endpoint = getEndpoint();
final String repositoryName = getRepositoryName();

ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildClient();

try {
    anonymousClient.deleteRepository(repositoryName);
    System.out.println("Unexpected Success: Delete is not allowed on anonymous access");
} catch (Exception ex) {
    System.out.println("Expected exception: Delete is not allowed on anonymous access");
}
```

### Delete repository with anonymous access asynchronously

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L245-L256 -->
```Java
final String endpoint = getEndpoint();
final String repositoryName = getRepositoryName();

ContainerRegistryAsyncClient anonymousClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildAsyncClient();

anonymousClient.deleteRepository(repositoryName).subscribe(deleteRepositoryResult -> {
    System.out.println("Unexpected Success: Delete is not allowed on anonymous access");
}, error -> {
    System.out.println("Expected exception: Delete is not allowed on anonymous access");
});
```

## Troubleshooting

All container registry service operations will throw a
[HttpResponseException][HttpResponseException] on failure.

<!-- embedme ./src/samples/java/com/azure/containers/containerregistry/ReadmeSamples.java#L66-L76 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient()
    .getRepository(repositoryName);
try {
    containerRepository.getProperties();
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
