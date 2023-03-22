// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsStartUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsUploadChunkHeaders;
import com.azure.containers.containerregistry.implementation.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesBase;
import com.azure.containers.containerregistry.implementation.models.RepositoryWriteableProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestOrder;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOWNLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.UPLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.createSha256;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.deleteResponseToSuccess;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.enableSync;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getBlobSize;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getLocation;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateResponseHeaderDigest;
import static com.azure.core.util.CoreUtils.bytesToHexString;

/**
 * This class provides a helper type that contains all the operations for repositories in Azure Container Registry.
 * Operations allowed by this type are listing, retrieving, deleting, setting writeable properties. These operations are
 * supported on the repository and the respective tags and manifests in it.
 *
 * <p><strong>Instantiating Container Repository helper type.</strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.instantiation -->
 * <pre>
 * ContainerRepository repositoryClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;.getRepository&#40;repository&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRepository.instantiation -->
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class ContainerRepository {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRepository.class);
    private final ContainerRegistriesImpl serviceClient;
    private final String repositoryName;
    private final String endpoint;
    private final String apiVersion;
    private final HttpPipeline httpPipeline;
    private final String registryLoginServer;
    private final ContainerRegistryBlobsImpl blobClient;
    private final Tracer tracer;

    /**
     * Creates a {@link ContainerRepository} that sends requests to the given repository in the container registry
     * service at {@code endpoint}. Each service call goes through the {@code pipeline}.
     *
     * @param repositoryName The name of the repository on which the service operations are performed.
     * @param endpoint The URL string for the Azure Container Registry service.
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     */
    ContainerRepository(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version, Tracer tracer) {
        Objects.requireNonNull(repositoryName, "'repositoryName' cannot be null");
        if (repositoryName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'repositoryName' can't be empty."));
        }

        this.endpoint = endpoint;
        this.repositoryName = repositoryName;
        AzureContainerRegistryImpl registryImplClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version);
        this.serviceClient = registryImplClient.getContainerRegistries();
        this.apiVersion = version;
        this.httpPipeline = httpPipeline;
        this.tracer = tracer;
        this.blobClient = registryImplClient.getContainerRegistryBlobs();

        try {
            URL endpointUrl = new URL(endpoint);
            this.registryLoginServer = endpointUrl.getHost();
        } catch (MalformedURLException ex) {
            // This will not happen.
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
    }


    /**
     * Gets the Azure Container Registry service endpoint for the current instance.
     *
     * @return The service endpoint for the current instance.
     */
    public String getName() {
        return this.repositoryName;
    }

    /**
     * Gets the Azure Container Registry name for the current instance.
     *
     * @return Return the registry name.
     */
    public String getRegistryEndpoint() {
        return this.endpoint;
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.deleteRepositoryWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = client.deleteWithResponse&#40;Context.NONE&#41;;
     * System.out.printf&#40;&quot;Successfully initiated delete.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.deleteRepositoryWithResponse -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call. artifacts
     * that are deleted as part of the repository delete.
     * @return A void response for completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Context context) {
        try {
            Response<DeleteRepositoryResult> response =
                this.serviceClient.deleteRepositoryWithResponse(repositoryName, enableSync(context));
            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.deleteRepository -->
     * <pre>
     * client.delete&#40;&#41;;
     * System.out.printf&#40;&quot;Successfully initiated delete.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.deleteRepository -->
     *
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        this.deleteWithResponse(Context.NONE);
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.getPropertiesWithResponse -->
     * <pre>
     * Response&lt;ContainerRepositoryProperties&gt; response = client.getPropertiesWithResponse&#40;Context.NONE&#41;;
     * final ContainerRepositoryProperties properties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Name:%s,&quot;, properties.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.getPropertiesWithResponse -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the {@link ContainerRepositoryProperties properties} associated with the given
     * {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ContainerRepositoryProperties> getPropertiesWithResponse(Context context) {
        try {
            return this.serviceClient.getPropertiesWithResponse(repositoryName, enableSync(context));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.getProperties -->
     * <pre>
     * ContainerRepositoryProperties properties = client.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Name:%s,&quot;, properties.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.getProperties -->
     *
     * @return The {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ContainerRepositoryProperties getProperties() {
        return this.getPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Creates a new instance of {@link RegistryArtifact} object for the specified artifact.
     *
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifact} object for the desired repository.
     * @throws NullPointerException if {@code tagOrDigest} is null.
     * @throws IllegalArgumentException if {@code tagOrDigest} is empty.
     */
    public RegistryArtifact getArtifact(String tagOrDigest) {
        return new RegistryArtifact(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listManifestProperties(ArtifactManifestOrder,
     * Context)}   listManifestProperties} No assumptions on the order can be made if no options are provided to the
     * service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestProperties -->
     * <pre>
     * client.listManifestProperties&#40;&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestProperties -->
     *
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties() {
        return this.listManifestProperties(ArtifactManifestOrder.NONE, Context.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository }.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order for the last updated time for the artifacts. No
     * assumptions on the order can be made if no options are provided by the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptionsNoContext -->
     * <pre>
     * client.listManifestProperties&#40;ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptionsNoContext -->
     *
     * @param order the order in which the artifacts are returned by the service.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties(ArtifactManifestOrder order) {
        return this.listManifestProperties(order, Context.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository }.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order for the last updated time for the artifacts. No
     * assumptions on the order can be made if no options are provided by the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptions -->
     * <pre>
     * client.listManifestProperties&#40;ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING, Context.NONE&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptions -->
     *
     * @param order the order in which the artifacts are returned by the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties(ArtifactManifestOrder order, Context context) {
        return this.listManifestPropertiesSync(order, context);
    }

    private PagedIterable<ArtifactManifestProperties> listManifestPropertiesSync(ArtifactManifestOrder order, Context context) {
        return new PagedIterable<>(
            (pageSize) -> listManifestPropertiesSinglePageSync(pageSize, order, context),
            (token, pageSize) -> listManifestPropertiesNextSinglePageSync(token, context));
    }

    private PagedResponse<ArtifactManifestProperties> listManifestPropertiesSinglePageSync(Integer pageSize, ArtifactManifestOrder order, Context context) {
        if (pageSize != null && pageSize < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        final String orderString = order == ArtifactManifestOrder.NONE ? null : order.toString();
        try {
            PagedResponse<ManifestAttributesBase> res =
                this.serviceClient.getManifestsSinglePage(repositoryName, null, pageSize, orderString,
                    enableSync(context));

            return UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseArtifacts -> UtilsImpl.mapManifestsProperties(baseArtifacts, repositoryName, registryLoginServer));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private PagedResponse<ArtifactManifestProperties> listManifestPropertiesNextSinglePageSync(String nextLink, Context context) {
        try {
            PagedResponse<ManifestAttributesBase> res = this.serviceClient.getManifestsNextSinglePage(nextLink,
                enableSync(context));
            return UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseArtifacts -> UtilsImpl.mapManifestsProperties(baseArtifacts, repositoryName, registryLoginServer));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Update the settable properties {@link ContainerRepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.updatePropertiesWithResponse -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updatePropertiesWithResponse&#40;properties, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.updatePropertiesWithResponse -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties repository properties} that need to be updated
     * for the repository.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ContainerRepositoryProperties> updatePropertiesWithResponse(ContainerRepositoryProperties repositoryProperties, Context context) {
        return this.updatePropertiesWithResponseSync(repositoryProperties, context);
    }

    /**
     * Update the repository properties {@link ContainerRepositoryProperties} of the given {@link #getName()
     * repository}. These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.updateProperties -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updateProperties&#40;properties&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.updateProperties -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties repository properties} that need to be updated
     * for the repository.
     * @return The updated {@link ContainerRepositoryProperties properties }
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ContainerRepositoryProperties updateProperties(ContainerRepositoryProperties repositoryProperties) {
        return this.updatePropertiesWithResponse(repositoryProperties, Context.NONE).getValue();
    }

    private Response<ContainerRepositoryProperties> updatePropertiesWithResponseSync(ContainerRepositoryProperties repositoryProperties, Context context) {
        Objects.requireNonNull(repositoryProperties, "'repositoryProperties' cannot be null");

        RepositoryWriteableProperties writableProperties = new RepositoryWriteableProperties()
            .setDeleteEnabled(repositoryProperties.isDeleteEnabled())
            .setListEnabled(repositoryProperties.isListEnabled())
            .setWriteEnabled(repositoryProperties.isWriteEnabled())
            .setReadEnabled(repositoryProperties.isReadEnabled());
//          .setTeleportEnabled(repositoryProperties.isTeleportEnabled());

        try {
            return this.serviceClient.updatePropertiesWithResponse(repositoryName, writableProperties,
                enableSync(context));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }


    /**
     * Upload the OCI manifest to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadManifest -->
     * <pre>
     * blobClient.uploadManifest&#40;manifest, &quot;v1&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadManifest -->
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param manifest The {@link OciImageManifest} that needs to be updated.
     * @param tag Tag to apply on uploaded manifest. If {@code null} is passed, no tags will be applied.
     * @return upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadManifestResult setManifest(OciImageManifest manifest, String tag) {
        Objects.requireNonNull(manifest, "'manifest' cannot be null.");
        return uploadManifestWithResponse(BinaryData.fromObject(manifest), tag, ManifestMediaType.OCI_MANIFEST, Context.NONE).getValue();
    }

    /**
     * Uploads a manifest to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadCustomManifest -->
     * <pre>
     * UploadManifestOptions options = new UploadManifestOptions&#40;manifestList, DOCKER_MANIFEST_LIST_TYPE&#41;;
     *
     * Response&lt;UploadManifestResult&gt; response = blobClient.uploadManifestWithResponse&#40;options, Context.NONE&#41;;
     * System.out.println&#40;&quot;Manifest uploaded, digest - &quot; + response.getValue&#40;&#41;.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadCustomManifest -->
     *
     * @param options The options for the upload manifest operation.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UploadManifestResult> setManifestWithResponse(UploadManifestOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return uploadManifestWithResponse(options.getManifest(), options.getTag(), options.getManifestMediaType(), context);
    }



    private Response<UploadManifestResult> uploadManifestWithResponse(BinaryData manifestData, String tagOrDigest, ManifestMediaType manifestMediaType, Context context) {
        BinaryData data = manifestData.toReplayableBinaryData();
        if (tagOrDigest == null) {
            tagOrDigest = computeDigest(data.toByteBuffer());
        }

        try {
            ResponseBase<ContainerRegistriesCreateManifestHeaders, Void> response = this.serviceClient
                .createManifestWithResponse(repositoryName, tagOrDigest, data, data.getLength(),
                    manifestMediaType.toString(), enableSync(context));

            return new ResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                ConstructorAccessors.createUploadManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                response.getDeserializedHeaders());
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }


    /**
     * Uploads a blob to the repository in chunks of 4MB.
     * Use this method to upload relatively small content that fits into memory. For large content use
     * {@link ContainerRepository#uploadBlob(ReadableByteChannel, Context)} overload.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadBlob -->
     * <pre>
     * BinaryData configContent = BinaryData.fromObject&#40;Collections.singletonMap&#40;&quot;hello&quot;, &quot;world&quot;&#41;&#41;;
     *
     * UploadBlobResult uploadResult = blobClient.uploadBlob&#40;configContent&#41;;
     * System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;, uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadBlob -->
     *
     * @param data The blob content. The content may be loaded into memory depending on how {@link BinaryData} is created.
     * @return The upload response.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadBlobResult uploadBlob(BinaryData data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        InputStream stream = data.toStream();
        try {
            return uploadBlob(Channels.newChannel(stream), Context.NONE);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warning("Failed to close the stream", e);
            }
        }
    }

    /**
     * Uploads a blob to the repository in chunks of 4MB.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadStream -->
     * <pre>
     * try &#40;FileInputStream content = new FileInputStream&#40;&quot;artifact.tar.gz&quot;&#41;&#41; &#123;
     *     UploadBlobResult uploadResult = blobClient.uploadBlob&#40;content.getChannel&#40;&#41;, Context.NONE&#41;;
     *     System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;,
     *         uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadStream -->
     *
     * @param stream The blob content.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The upload response.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code stream} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadBlobResult uploadBlob(ReadableByteChannel stream, Context context) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");
        return runWithTracing(UPLOAD_BLOB_SPAN_NAME, (span) -> uploadBlobInternal(stream, span), enableSync(context));
    }


    private UploadBlobResult uploadBlobInternal(ReadableByteChannel stream, Context context) {
        MessageDigest sha256 = createSha256();
        byte[] buffer = new byte[CHUNK_SIZE];

        try {
            ResponseBase<ContainerRegistryBlobsStartUploadHeaders, Void> startUploadResponse =
                blobClient.startUploadWithResponse(repositoryName, context);
            String location = getLocation(startUploadResponse);

            BinaryData chunk;
            long streamLength = 0L;
            while (true) {
                chunk = readChunk(stream, sha256, buffer);
                if (chunk == null) {
                    break;
                }

                streamLength += chunk.getLength();
                if (chunk.getLength() < CHUNK_SIZE) {
                    break;
                }

                ResponseBase<ContainerRegistryBlobsUploadChunkHeaders, Void> uploadChunkResponse =
                    blobClient.uploadChunkWithResponse(location, chunk, chunk.getLength(), context);
                location = getLocation(uploadChunkResponse);
            }

            String digest = "sha256:" + bytesToHexString(sha256.digest());

            ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, Void> completeUploadResponse =
                blobClient.completeUploadWithResponse(digest, location, chunk, chunk == null ? null : chunk.getLength(), context);

            return ConstructorAccessors.createUploadBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest(), streamLength);
        } catch (AcrErrorsException ex) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(ex));
        }
    }


    private BinaryData readChunk(ReadableByteChannel stream, MessageDigest sha256, byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        while (byteBuffer.position() < CHUNK_SIZE) {
            try {
                if (stream.read(byteBuffer) < 0) {
                    break;
                }
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }
        }
        if (byteBuffer.position() == 0) {
            return null;
        }

        byteBuffer.flip();
        sha256.update(byteBuffer.asReadOnlyBuffer());
        return BinaryData.fromByteBuffer(byteBuffer);
    }

    private <T> T runWithTracing(String spanName, Function<Context, T> operation, Context context) {
        Context span = tracer.start(spanName, context);
        Exception exception = null;
        try {
            return operation.apply(span);
        } catch (RuntimeException ex) {
            exception = ex;
            throw ex;
        } finally {
            tracer.end(null, exception, span);
        }
    }


    /**
     * Download the blob identified by  the given digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.downloadStream -->
     * <pre>
     * Path file = Files.createTempFile&#40;digest, &quot;.tmp&quot;&#41;;
     * SeekableByteChannel channel = Files.newByteChannel&#40;file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE&#41;;
     * blobClient.downloadStream&#40;digest, channel&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.downloadStream -->
     *
     * @param digest The digest for the given image layer.
     * @param channel The channel to write content to.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     * @throws ServiceResponseException thrown if content hash does not match requested digest.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadStream(String digest, WritableByteChannel channel) {
        downloadStreamWithResponse(digest, channel, Context.NONE);
    }

    /**
     * Download the blob identified by the given digest.
     *
     * @param digest The digest for the given image layer.
     * @param channel The channel to write content to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return Response to last request received while downloading the context. Note that downloading content could be done
     *         over several requests due to downloading in chunks or resuming failed downloads. Use distributed tracing or
     *         logging to get insights into all requests done within this operation.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     * @throws ServiceResponseException thrown if content hash does not match requested digest.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadStreamWithResponse(String digest, WritableByteChannel channel, Context context) {
        return runWithTracing(DOWNLOAD_BLOB_SPAN_NAME, (span) -> downloadBlobInternal(digest, channel, span), context);
    }

    /**
     * Delete the image identified by the given digest
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed readme-sample-deleteBlob -->
     * <pre>
     * DownloadManifestResult manifestResult = blobClient.downloadManifest&#40;&quot;latest&quot;&#41;;
     *
     * OciImageManifest manifest = manifestResult.asOciImageManifest&#40;&#41;;
     * for &#40;OciDescriptor layer : manifest.getLayers&#40;&#41;&#41; &#123;
     *     blobClient.deleteBlob&#40;layer.getDigest&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end readme-sample-deleteBlob -->
     *
     * @param digest The digest for the given image layer.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteBlob(String digest) {
        deleteBlobWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Delete the image identified by the given digest
     *
     * @param digest The digest for the given image layer.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteBlobWithResponse(String digest, Context context) {
        Objects.requireNonNull(digest, "'digest' cannot be null.");

        context = enableSync(context);
        try {
            Response<BinaryData> streamResponse =
                blobClient.deleteBlobWithResponse(repositoryName, digest, enableSync(context));
            return deleteResponseToSuccess(streamResponse);
        } catch (HttpResponseException ex) {
            if (ex.getResponse().getStatusCode() == 404) {
                HttpResponse response = ex.getResponse();
                // In case of 404, we still convert it to success i.e. no-op.
                return new SimpleResponse<>(response.getRequest(), 202,
                    response.getHeaders(), null);
            } else {
                throw LOGGER.logExceptionAsError(ex);
            }
        }
    }

    private Response<Void> downloadBlobInternal(String digest, WritableByteChannel channel, Context context) {
        Objects.requireNonNull(digest, "'digest' cannot be null.");

        context = enableSync(context);
        MessageDigest sha256 = createSha256();
        Response<BinaryData> lastChunk;
        try {
            lastChunk = readRange(digest, new HttpRange(0, (long) CHUNK_SIZE), channel, sha256, context);
            validateResponseHeaderDigest(digest, lastChunk.getHeaders());

            long blobSize = getBlobSize(lastChunk.getHeaders().get(HttpHeaderName.CONTENT_RANGE));
            for (long p = lastChunk.getValue().getLength(); p < blobSize; p += CHUNK_SIZE) {
                lastChunk = readRange(digest, new HttpRange(p, (long) CHUNK_SIZE), channel, sha256, context);
            }
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }

        validateDigest(sha256, digest);

        return new SimpleResponse<>(lastChunk.getRequest(), lastChunk.getStatusCode(), lastChunk.getHeaders(), null);
    }

    private Response<BinaryData> readRange(String digest, HttpRange range, WritableByteChannel channel, MessageDigest sha256, Context context) {
        Response<BinaryData> response = blobClient.getChunkWithResponse(repositoryName, digest, range.toString(), context);

        ByteBuffer buffer = response.getValue().toByteBuffer();
        sha256.update(buffer.asReadOnlyBuffer());
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }

        return response;
    }
}
