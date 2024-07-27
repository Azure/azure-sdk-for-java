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
- [Container Registry Create][container_registry_create_cli]

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-containers-containerregistry</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
[//]: # ({x-version-update-start;com.azure:azure-containers-containerregistry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-containers-containerregistry</artifactId>
  <version>1.2.10</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate clients

The [Azure Identity library][identity] provides easy Azure Active Directory support for authentication.
Note all the below samples assume you have an endpoint, which is the URL including the name of the login server and the `https://` prefix.
More information at [Azure Container Registry portal][container_registry_create_portal]

```java readme-sample-createClient
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();
```

```java readme-sample-createAsyncClient
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryAsyncClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildAsyncClient();
```

For more information on using AAD with Azure Container Registry, please see the service's [Authentication Overview](https://docs.microsoft.com/azure/container-registry/container-registry-authentication).

#### Authenticating with ARM AAD token

By default, Container Registry SDK for Java uses ACR access tokens. If you want to authenticate with ARM AAD token and have corresponding policy enabled,
make sure to set audience when building container Registry client.
Please refer to [ACR CLI reference](https://learn.microsoft.com/cli/azure/acr/config/authentication-as-arm?view=azure-cli-latest) for information
on how to check ARM authentication policy configuration.

`ContainerRegistryAudience` value is specific to the cloud:

```java readme-sample-armTokenPublic
ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(getEndpoint())
    .credential(credential)
    .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
    .buildClient();

registryClient
    .listRepositoryNames()
    .forEach(name -> System.out.println(name));
```

#### National Clouds

To authenticate with a registry in a [National Cloud](https://docs.microsoft.com/azure/active-directory/develop/authentication-national-cloud), you will need to make the following additions to your client configuration:
- Set the `authorityHost` in the credential builder following [Identity client library documentation](https://learn.microsoft.com/java/api/overview/azure/identity-readme) 
- If ACR access token authentication is disabled for yourcontainer Registry resource, you need to configure the audience on the Container Registry client builder.

```java readme-sample-armTokenChina
ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(getEndpoint())
    .credential(credential)
    // only if ACR access tokens are disabled or not supported
    .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_CHINA)
    .buildClient();

registryClient
    .listRepositoryNames()
    .forEach(name -> System.out.println(name));
```

#### Anonymous access support
If the builder is instantiated without any credentials, the SDK creates the service client for the anonymous pull mode.
The user must use this setting on a registry that has been enabled for anonymous pull.
In this mode, the user can only call listRepositoryNames method and its overload. All the other calls will fail. 
For more information please read [Anonymous Pull Access](https://docs.microsoft.com/azure/container-registry/container-registry-faq#how-do-i-enable-anonymous-pull-access)

```java readme-sample-createAnonymousAccessClient
ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildClient();
```

```java readme-sample-createAnonymousAsyncAccessClient
ContainerRegistryAsyncClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildAsyncClient();
```

## Key concepts

A **registry** stores Docker images and [OCI Artifacts](https://opencontainers.org/).  An image or artifact consists of a **manifest** and **layers**.  An image's manifest describes the layers that make up the image, and is uniquely identified by its **digest**.  An image can also be "tagged" to give it a human-readable alias.  An image or artifact can have zero or more **tags** associated with it, and each tag uniquely identifies the image.  A collection of images that share the same name but have different tags, is referred to as a **repository**.

For more information please see [Container Registry Concepts](https://docs.microsoft.com/azure/container-registry/container-registry-concepts).

## Examples

### Sync examples

- Registry operations:
  - [List repository names](#list-repository-names)
  - [List artifact tags with anonymous access](#list-artifact-tags-with-anonymous-access)
  - [Set artifact properties](#set-artifact-properties)
  - [Delete images](#delete-images)
  - [Delete repository with anonymous access throws](#delete-a-repository-with-anonymous-access-throws)
- Blob and manifest operations:
  - [Upload images](#upload-images)
  - [Download images](#download-images)
  - [Delete manifest](#delete-manifest)
  - [Delete blob](#delete-blob) 

### Registry operations

This section contains `ContainerRegistryClient` samples.

#### List repository names

Iterate through the collection of repositories in the registry.

```java readme-sample-listRepositoryNames
registryClient.listRepositoryNames().forEach(repository -> System.out.println(repository));
```

#### List artifact tags with anonymous access

```java readme-sample-listTagProperties
RegistryArtifact image = anonymousClient.getArtifact(repositoryName, digest);

PagedIterable<ArtifactTagProperties> tags = image.listTagProperties();

System.out.printf(String.format("%s has the following aliases:", image.getFullyQualifiedReference()));

for (ArtifactTagProperties tag : tags) {
    System.out.printf(String.format("%s/%s:%s", anonymousClient.getEndpoint(), repositoryName, tag.getName()));
}
```

#### Set artifact properties

```java readme-sample-setArtifactProperties
RegistryArtifact image = registryClient.getArtifact(repositoryName, digest);

image.updateTagProperties(
    tag,
    new ArtifactTagProperties()
        .setWriteEnabled(false)
        .setDeleteEnabled(false));
```


#### Delete Images

```java readme-sample-deleteImages
final int imagesCountToKeep = 3;
for (String repositoryName : registryClient.listRepositoryNames()) {
    final ContainerRepository repository = registryClient.getRepository(repositoryName);

    // Obtain the images ordered from newest to oldest
    PagedIterable<ArtifactManifestProperties> imageManifests =
        repository.listManifestProperties(
            ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING,
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

#### Delete a repository with anonymous access throws

```java readme-sample-anonymousClientThrows
final String endpoint = getEndpoint();
final String repositoryName = getRepositoryName();

ContainerRegistryClient anonymousClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .buildClient();

try {
    anonymousClient.deleteRepository(repositoryName);
    System.out.println("Unexpected Success: Delete is not allowed on anonymous access");
} catch (ClientAuthenticationException ex) {
    System.out.println("Expected exception: Delete is not allowed on anonymous access");
}
```

### Blob and manifest operations

This section contains samples for `ContainerRegistryContentClient` that show how to upload and download images. 

First, we need to create blob client.

```java readme-sample-createContentClient
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .repositoryName(repository)
    .buildClient();
```

#### Upload Images

To upload a full image, we need to upload individual layers and configuration. After that we can upload a manifest 
which describes an image or artifact and assign it a tag.  

```java readme-sample-uploadImage
BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

UploadRegistryBlobResult configUploadResult = contentClient.uploadBlob(configContent);
System.out.printf("Uploaded config: digest - %s, size - %s\n", configUploadResult.getDigest(), configContent.getLength());

OciDescriptor configDescriptor = new OciDescriptor()
    .setMediaType("application/vnd.unknown.config.v1+json")
    .setDigest(configUploadResult.getDigest())
    .setSizeInBytes(configContent.getLength());

BinaryData layerContent = BinaryData.fromString("Hello Azure Container Registry");
UploadRegistryBlobResult layerUploadResult = contentClient.uploadBlob(layerContent);
System.out.printf("Uploaded layer: digest - %s, size - %s\n", layerUploadResult.getDigest(), layerContent.getLength());

OciImageManifest manifest = new OciImageManifest()
    .setConfiguration(configDescriptor)
    .setSchemaVersion(2)
    .setLayers(Collections.singletonList(
        new OciDescriptor()
            .setDigest(layerUploadResult.getDigest())
            .setSizeInBytes(layerContent.getLength())
            .setMediaType("application/octet-stream")));

SetManifestResult manifestResult = contentClient.setManifest(manifest, "latest");
System.out.printf("Uploaded manifest: digest - %s\n", manifestResult.getDigest());
```

#### Download Images

To download a full image, we need to download its manifest and then download individual layers and configuration. 

```java readme-sample-downloadImage
GetManifestResult manifestResult = contentClient.getManifest("latest");

OciImageManifest manifest = manifestResult.getManifest().toObject(OciImageManifest.class);
System.out.printf("Got manifest:\n%s\n", PRETTY_PRINT.writeValueAsString(manifest));

String configFileName = manifest.getConfiguration().getDigest() + ".json";
contentClient.downloadStream(manifest.getConfiguration().getDigest(), createFileChannel(configFileName));
System.out.printf("Got config: %s\n", configFileName);

for (OciDescriptor layer : manifest.getLayers()) {
    contentClient.downloadStream(layer.getDigest(), createFileChannel(layer.getDigest()));
    System.out.printf("Got layer: %s\n", layer.getDigest());
}
```

#### Delete blob

```java readme-sample-deleteBlob
GetManifestResult manifestResult = contentClient.getManifest("latest");

OciImageManifest manifest = manifestResult.getManifest().toObject(OciImageManifest.class);
for (OciDescriptor layer : manifest.getLayers()) {
    contentClient.deleteBlob(layer.getDigest());
}
```

#### Delete manifest

```java readme-sample-deleteManifest
GetManifestResult manifestResult = contentClient.getManifest("latest");
contentClient.deleteManifest(manifestResult.getDigest());
```

## Troubleshooting

See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/containerregistry/azure-containers-containerregistry/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

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
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/containerregistry/azure-containers-containerregistry/src
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free
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
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/containerregistry/azure-containers-containerregistry/src/samples/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcontainerregistry%2Fazure-contianers-containerregistry%2FREADME.png)
