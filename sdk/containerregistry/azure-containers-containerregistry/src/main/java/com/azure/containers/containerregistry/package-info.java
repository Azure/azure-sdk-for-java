// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
/**
 * <p><a href="https://learn.microsoft.com/azure/container-registry/">Azure Container Registry Service</a>
 * is a managed service provided by Microsoft Azure that allows developers to build, store, and manage
 * container images and artifacts in a private registry for all types of container deployments.</p>
 *
 * <p>Containerization is a technology that allows developers to package an application and its dependencies into a
 * single, ightweight container that can run consistently across different environments. Containers have become
 * increasingly popular for building, packaging, and deploying applications, and Azure Container Registry helps with
 * the management of the container images used in these applications.</p>
 *
 * <p>The Azure Container Registry library is a client library that provides Java developers with a simple and
 * easy-to-use interface for accessing and using the Azure Container Registry Service. This library allows developers to
 * easily manage their artifacts and repositories in the Azure Container Registry Service.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Container Registry service you'll need to create an instance of
 * {@link com.azure.containers.containerregistry.ContainerRegistryClient},
 * {@link com.azure.containers.containerregistry.ContainerRegistryContentClient} or their asynchronous counterparts.</p>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryClient} allows to list and delete repositories
 * within the registry or obtain an instance of {@link com.azure.containers.containerregistry.ContainerRepository}
 * or {@link com.azure.containers.containerregistry.RegistryArtifact} that can be used to perform operations on the
 * repository or artifact.</p>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryContentClient} allows to upload, download,
 * and delete artifacts in Azure Container Registry repository.</p>
 *
 * <p>To create one of these clients and communicate with the service, you'll need to use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a></p>.
 *
 * <p><strong>Sample: Construct Synchronous Container Registry Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.containers.containerregistry.ContainerRegistryClient}.</p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.instantiation -->
 * <pre>
 * ContainerRegistryClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.instantiation -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.containers.containerregistry.ContainerRegistryAsyncClient}.</p>
 *
 * <p>Container Registry Client allows to list and delete repositories and obtain instances of repository and
 * artifact client. See methods in {@link com.azure.containers.containerregistry.ContainerRegistryClient} class to
 * explore all capabilities that client provides.</p>
 *
 * <p><strong>Sample: Construct Synchronous Container Registry Content Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.containers.containerregistry.ContainerRegistryContentClient}.</p>
 *
 * <!-- src_embed readme-sample-createContentClient -->
 * <pre>
 * DefaultAzureCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * ContainerRegistryContentClient contentClient = new ContainerRegistryContentClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .repositoryName&#40;repository&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createContentClient -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.containers.containerregistry.ContainerRegistryContentAsyncClient}.</p>
 *
 * <p>Container Registry Content Client allows to upload and download registry artifacts.
 * See methods in {@link com.azure.containers.containerregistry.ContainerRegistryContentClient} class to explore all
 * capabilities that client provides.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List repositories within Container Registry</h2>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryClient#listRepositoryNames() listRepositoryNames}
 * method can be used to list repositories within Azure Container Registry.</p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames -->
 * <pre>
 * client.listRepositoryNames&#40;&#41;.stream&#40;&#41;.forEach&#40;name -&gt; &#123;
 *     System.out.printf&#40;&quot;Repository Name:%s,&quot;, name&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.containers.containerregistry.ContainerRegistryAsyncClient#listRepositoryNames()}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get manifest properties</h2>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryClient#getArtifact(java.lang.String, java.lang.String) getArtifact}
 * method can be used to get artifact client that allows to manage artifact tags and manifest properties.</p>
 *
 * <p>The sample below shows how to list manifest properties.</p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact -->
 * <pre>
 * RegistryArtifact registryArtifact = client.getArtifact&#40;repositoryName, tagOrDigest&#41;;
 * ArtifactManifestProperties properties = registryArtifact.getManifestProperties&#40;&#41;;
 * System.out.println&#40;properties.getDigest&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.containers.containerregistry.ContainerRegistryAsyncClient#getArtifact(java.lang.String, java.lang.String)}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete repository</h2>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryClient#deleteRepository(java.lang.String)  deleteRepository}
 * method can be used to delete a repository.</p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String -->
 * <pre>
 * client.deleteRepository&#40;repositoryName&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.containers.containerregistry.ContainerRegistryAsyncClient#deleteRepository(java.lang.String)}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Upload an artifact</h2>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryContentClient#setManifest(com.azure.containers.containerregistry.models.OciImageManifest, java.lang.String) setManifest}
 * method and its overloads can be used to upload artifact manifest.</p>
 *
 * <p>Note: artifacts configuration and all the layers must be uploaded before the manifest using
 * {@link com.azure.containers.containerregistry.ContainerRegistryContentClient#uploadBlob(com.azure.core.util.BinaryData) uploadBlob} method or on of it's overloads.</p>
 *
 * <p>The sample below shows how to upload artifact to the Azure Container Registry.</p>
 *
 * <!-- src_embed readme-sample-uploadImage -->
 * <pre>
 * BinaryData configContent = BinaryData
 *     .fromObject&#40;Collections.singletonMap&#40;&quot;hello&quot;, &quot;world&quot;&#41;&#41;;
 *
 * UploadRegistryBlobResult configUploadResult = contentClient.uploadBlob&#40;configContent&#41;;
 * System.out.printf&#40;&quot;Uploaded config: digest - %s, size - %s&#92;n&quot;, configUploadResult.getDigest&#40;&#41;,
 *     configContent.getLength&#40;&#41;&#41;;
 *
 * OciDescriptor configDescriptor = new OciDescriptor&#40;&#41;
 *     .setMediaType&#40;&quot;application&#47;vnd.unknown.config.v1+json&quot;&#41;
 *     .setDigest&#40;configUploadResult.getDigest&#40;&#41;&#41;
 *     .setSizeInBytes&#40;configContent.getLength&#40;&#41;&#41;;
 *
 * BinaryData layerContent = BinaryData.fromString&#40;&quot;Hello Azure Container Registry&quot;&#41;;
 * UploadRegistryBlobResult layerUploadResult = contentClient.uploadBlob&#40;layerContent&#41;;
 * System.out.printf&#40;&quot;Uploaded layer: digest - %s, size - %s&#92;n&quot;, layerUploadResult.getDigest&#40;&#41;,
 *     layerContent.getLength&#40;&#41;&#41;;
 *
 * OciImageManifest manifest = new OciImageManifest&#40;&#41;
 *     .setConfiguration&#40;configDescriptor&#41;
 *     .setSchemaVersion&#40;2&#41;
 *     .setLayers&#40;Collections.singletonList&#40;
 *         new OciDescriptor&#40;&#41;
 *             .setDigest&#40;layerUploadResult.getDigest&#40;&#41;&#41;
 *             .setSizeInBytes&#40;layerContent.getLength&#40;&#41;&#41;
 *             .setMediaType&#40;&quot;application&#47;octet-stream&quot;&#41;&#41;&#41;;
 *
 * SetManifestResult manifestResult = contentClient.setManifest&#40;manifest, &quot;latest&quot;&#41;;
 * System.out.printf&#40;&quot;Uploaded manifest: digest - %s&#92;n&quot;, manifestResult.getDigest&#40;&#41;&#41;;
 * </pre>
 * <!-- end readme-sample-uploadImage -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.containers.containerregistry.ContainerRegistryContentAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Download an artifact</h2>
 *
 * <p>The {@link com.azure.containers.containerregistry.ContainerRegistryContentClient#getManifest(java.lang.String)} getManifest}
 * method can be used to download manifest from Container Registry.</p>
 *
 * <p>To download artifact layers and configuration, you may use
 * {@link com.azure.containers.containerregistry.ContainerRegistryContentClient#downloadStream(java.lang.String, java.nio.channels.WritableByteChannel) downloadStream}
 * method or one of its overloads.</p>
 *
 * <p>The sample below shows how to download an artifact including configuration and all layers as files.</p>
 * <!-- src_embed readme-sample-downloadImage -->
 * <pre>
 * GetManifestResult manifestResult = contentClient.getManifest&#40;&quot;latest&quot;&#41;;
 *
 * OciImageManifest manifest = manifestResult.getManifest&#40;&#41;.toObject&#40;OciImageManifest.class&#41;;
 * System.out.printf&#40;&quot;Got manifest:&#92;n%s&#92;n&quot;, manifest.toJsonString&#40;&#41;&#41;;
 *
 * String configFileName = manifest.getConfiguration&#40;&#41;.getDigest&#40;&#41; + &quot;.json&quot;;
 * contentClient.downloadStream&#40;manifest.getConfiguration&#40;&#41;.getDigest&#40;&#41;, createFileChannel&#40;configFileName&#41;&#41;;
 * System.out.printf&#40;&quot;Got config: %s&#92;n&quot;, configFileName&#41;;
 *
 * for &#40;OciDescriptor layer : manifest.getLayers&#40;&#41;&#41; &#123;
 *     contentClient.downloadStream&#40;layer.getDigest&#40;&#41;, createFileChannel&#40;layer.getDigest&#40;&#41;&#41;&#41;;
 *     System.out.printf&#40;&quot;Got layer: %s&#92;n&quot;, layer.getDigest&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end readme-sample-downloadImage -->
 *
 * <p><strong>Note:</strong> For asynchronous examples, refer to the {@link com.azure.containers.containerregistry.ContainerRegistryContentAsyncClient}.</p>
 *
 * @see com.azure.containers.containerregistry.ContainerRegistryClient
 * @see com.azure.containers.containerregistry.ContainerRegistryAsyncClient
 * @see com.azure.containers.containerregistry.ContainerRegistryClientBuilder
 * @see com.azure.containers.containerregistry.ContainerRegistryContentClient
 * @see com.azure.containers.containerregistry.ContainerRegistryContentAsyncClient
 * @see com.azure.containers.containerregistry.ContainerRegistryContentClientBuilder
 */
package com.azure.containers.containerregistry;
