// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.RepositoryWriteableProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestOrder;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides helper methods for operations on a given repository in Azure Container Registry.
 * Operations included are listing, retrieving, deleting, setting writeable properties.
 *
 * <p><strong>Instantiating an asynchronous Container Repository Helper class</strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.instantiation -->
 * <pre>
 * ContainerRepositoryAsync repositoryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildAsyncClient&#40;&#41;
 *     .getRepository&#40;repository&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.instantiation -->
 *
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class, isAsync = true)
public final class ContainerRepositoryAsync {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRepositoryAsync.class);
    private final ContainerRegistriesImpl serviceClient;
    private final String repositoryName;
    private final String endpoint;
    private final String apiVersion;
    private final HttpPipeline httpPipeline;
    private final String registryLoginServer;

    /**
     * Creates a ContainerRepositoryAsyncClient that sends requests to the given repository in the container registry service at {@code endpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param repositoryName The name of the repository on which the service operations are performed.
     * @param endpoint The URL string for the Azure Container Registry service.
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     */
    ContainerRepositoryAsync(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version) {
        Objects.requireNonNull(repositoryName, "'repositoryName' cannot be null");
        if (repositoryName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'repositoryName' can't be empty."));
        }

        this.endpoint = endpoint;
        this.repositoryName = repositoryName;
        this.serviceClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version).getContainerRegistries();
        this.apiVersion = version;
        this.httpPipeline = httpPipeline;

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
     * @return The service endpoint for the current instance.
     */
    public String getName() {
        return this.repositoryName;
    }

    /**
     * Gets the Azure Container Registry name for the current instance.
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
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.deleteRepositoryWithResponse -->
     * <pre>
     * client.deleteWithResponse&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Successfully initiated delete of the repository.&quot;&#41;;
     * &#125;, error -&gt; &#123;
     *     System.out.println&#40;&quot;Failed to initiate a delete of the repository.&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.deleteRepositoryWithResponse -->
     *
     * @return A REST response containing the result of the repository delete operation. It returns the count of the tags and
     * artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(context -> deleteWithResponse(context));
    }

    private Mono<Response<Void>> deleteWithResponse(Context context) {
        return this.serviceClient.deleteRepositoryWithResponseAsync(repositoryName, context)
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.deleteRepository -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Successfully initiated delete of the repository.&quot;&#41;;
     * &#125;, error -&gt; &#123;
     *     System.out.println&#40;&quot;Failed to initiate a delete of the repository.&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.deleteRepository -->
     *
     * @return It returns the count of the tags and artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return this.deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new instance of {@link RegistryArtifactAsync} object for the specified artifact.
     *
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifactAsync} object for the desired repository.
     * @throws NullPointerException if {@code tagOrDigest} is null.
     * @throws IllegalArgumentException if {@code tagOrDigest} is empty.
     */
    public RegistryArtifactAsync getArtifact(String tagOrDigest) {
        return new RegistryArtifactAsync(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listManifestProperties(ArtifactManifestOrder)}  listManifestProperties}
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.listManifestProperties -->
     * <pre>
     * client.listManifestProperties&#40;&#41;.byPage&#40;10&#41;
     *     .subscribe&#40;ManifestPropertiesPagedResponse -&gt; &#123;
     *         ManifestPropertiesPagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.listManifestProperties -->
     *
     * @return {@link PagedFlux} of ManifestProperties for all the artifacts in the given repository.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactManifestProperties> listManifestProperties() {
        return listManifestProperties(ArtifactManifestOrder.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order based on the last updated time for the artifacts.
     * No assumptions on the order can be made if no options are provided to the service. </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.listManifestPropertiesWithOptions -->
     * <pre>
     * client.listManifestProperties&#40;ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING&#41;.byPage&#40;10&#41;
     *     .subscribe&#40;ManifestPropertiesPagedResponse -&gt; &#123;
     *         ManifestPropertiesPagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.listManifestPropertiesWithOptions -->
     *
     * @param order The order in which the artifacts are returned by the service.
     * @return {@link PagedFlux} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactManifestProperties> listManifestProperties(ArtifactManifestOrder order) {
        return new PagedFlux<>(
            (pageSize) -> withContext(context -> listManifestPropertiesSinglePageAsync(pageSize, order, context)),
            (token, pageSize) -> withContext(context -> listManifestPropertiesNextSinglePageAsync(token, context)));
    }

    private Mono<PagedResponse<ArtifactManifestProperties>> listManifestPropertiesSinglePageAsync(Integer pageSize, ArtifactManifestOrder order, Context context) {
        if (pageSize != null && pageSize < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        final String orderString = order == ArtifactManifestOrder.NONE ? null : order.toString();
        return this.serviceClient.getManifestsSinglePageAsync(repositoryName, null, pageSize, orderString, context)
            .map(res -> UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseArtifacts -> UtilsImpl.mapManifestsProperties(baseArtifacts, repositoryName,
                    registryLoginServer)))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private Mono<PagedResponse<ArtifactManifestProperties>> listManifestPropertiesNextSinglePageAsync(String nextLink, Context context) {
        return this.serviceClient.getManifestsNextSinglePageAsync(nextLink, context)
            .map(res -> UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseArtifacts -> UtilsImpl.mapManifestsProperties(baseArtifacts, repositoryName,
                    registryLoginServer)))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.getPropertiesWithResponse -->
     * <pre>
     * client.getPropertiesWithResponse&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     final ContainerRepositoryProperties properties = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Name:%s,&quot;, properties.getName&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.getPropertiesWithResponse -->
     *
     * @return A REST response with the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ContainerRepositoryProperties>> getPropertiesWithResponse() {
        return withContext(context -> this.getPropertiesWithResponse(context));
    }

    private Mono<Response<ContainerRepositoryProperties>> getPropertiesWithResponse(Context context) {
        return this.serviceClient.getPropertiesWithResponseAsync(repositoryName, context)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Name:%s,&quot;, response.getName&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.getProperties -->
     *
     * @return The {@link ContainerRepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ContainerRepositoryProperties> getProperties() {
        return this.getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Update the repository properties {@link ContainerRepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.updatePropertiesWithResponse -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updatePropertiesWithResponse&#40;properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.updatePropertiesWithResponse -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties repository properties} that need to be updated for the repository.
     * @return The updated {@link ContainerRepositoryProperties repository properties }.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ContainerRepositoryProperties>> updatePropertiesWithResponse(ContainerRepositoryProperties repositoryProperties) {
        return withContext(context -> this.updatePropertiesWithResponse(repositoryProperties, context));
    }

    private Mono<Response<ContainerRepositoryProperties>> updatePropertiesWithResponse(ContainerRepositoryProperties repositoryProperties, Context context) {
        if (repositoryProperties == null) {
            return monoError(LOGGER, new NullPointerException("'value' cannot be null."));
        }

        RepositoryWriteableProperties writableProperties = new RepositoryWriteableProperties()
            .setDeleteEnabled(repositoryProperties.isDeleteEnabled())
            .setListEnabled(repositoryProperties.isListEnabled())
            .setWriteEnabled(repositoryProperties.isWriteEnabled())
            .setReadEnabled(repositoryProperties.isReadEnabled());
//                .setTeleportEnabled(repositoryProperties.isTeleportEnabled());

        return this.serviceClient.updatePropertiesWithResponseAsync(repositoryName, writableProperties, context)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Update the repository properties {@link ContainerRepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepositoryAsync.updateProperties -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updateProperties&#40;properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepositoryAsync.updateProperties -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties writeable properties} that need to be updated for the repository.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ContainerRepositoryProperties> updateProperties(ContainerRepositoryProperties repositoryProperties) {
        return this.updatePropertiesWithResponse(repositoryProperties).flatMap(FluxUtil::toMono);
    }
}
