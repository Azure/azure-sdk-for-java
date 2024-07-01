// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.ArtifactManifestPropertiesHelper;
import com.azure.containers.containerregistry.implementation.ArtifactTagPropertiesHelper;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ManifestWriteableProperties;
import com.azure.containers.containerregistry.implementation.models.TagWriteableProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagOrder;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
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
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.formatFullyQualifiedReference;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.isDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a helper type that contains all the operations for artifacts in a given repository.
 *
 * <p><strong>Instantiating an asynchronous RegistryArtifact helper.</strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.instantiation -->
 * <pre>
 * RegistryArtifactAsync registryArtifactAsync = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildAsyncClient&#40;&#41;.getArtifact&#40;repository, digest&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.instantiation -->
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class, isAsync = true)
public final class RegistryArtifactAsync {
    private static final ClientLogger LOGGER = new ClientLogger(RegistryArtifactAsync.class);
    private final String fullyQualifiedReference;
    private final String endpoint;
    private final String repositoryName;
    private final ContainerRegistriesImpl serviceClient;
    private final Mono<String> digestMono;

    /**
     * Creates a RegistryArtifactAsync type that sends requests to the given repository in the container registry service at {@code endpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param repositoryName The name of the repository on which the service operations are performed.
     * @param tagOrDigest The tag or digest associated with the given artifact.
     * @param endpoint The URL string for the Azure Container Registry service.
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     */
    RegistryArtifactAsync(String repositoryName, String tagOrDigest, HttpPipeline httpPipeline, String endpoint, String version) {
        Objects.requireNonNull(repositoryName, "'repositoryName' cannot be null.");
        if (repositoryName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'repositoryName' can't be empty"));
        }

        Objects.requireNonNull(tagOrDigest, "'tagOrDigest' cannot be null.");
        if (tagOrDigest.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'digest' can't be empty"));
        }

        this.serviceClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version).getContainerRegistries();
        this.fullyQualifiedReference = formatFullyQualifiedReference(endpoint, repositoryName, tagOrDigest);
        this.endpoint = endpoint;
        this.repositoryName = repositoryName;
        this.digestMono = isDigest(tagOrDigest) ? Mono.just(tagOrDigest)
            : Mono.defer(() -> getTagProperties(tagOrDigest)
                .map(a -> a.getDigest())
                .cache());
    }

    /**
     * Deletes the registry artifact with the matching digest in the given {@link #getRepositoryName() respository.}
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the registry artifact.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.deleteWithResponse -->
     * <pre>
     * client.deleteWithResponse&#40;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.deleteWithResponse -->
     *
     * @return A REST response with completion signal.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(context -> deleteWithResponse(context));
    }

    private Mono<Response<Void>> deleteWithResponse(Context context) {
        return digestMono
            .flatMap(res -> serviceClient.deleteManifestWithResponseAsync(getRepositoryName(), res, context))
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Deletes the registry artifact with the matching digest in the given {@link #getRepositoryName() respository.}
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the registry artifact.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.delete -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.delete -->
     *
     * @return the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the tag with the matching tag name for the given {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the tag for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTagWithResponse -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.deleteTagWithResponse&#40;tag&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTagWithResponse -->
     *
     * @param tag The name of the 'tag' that uniquely identifies the 'tag' that needs to be deleted.
     * @return A REST response with completion signal.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException thrown if {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTagWithResponse(String tag) {
        return withContext(context -> deleteTagWithResponse(tag, context));
    }

    private Mono<Response<Void>> deleteTagWithResponse(String tag, Context context) {
        if (tag == null) {
            return monoError(LOGGER, new NullPointerException("'tag' cannot be null"));
        }
        if (tag.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'tag' cannot be empty."));
        }

        return serviceClient.deleteTagWithResponseAsync(getRepositoryName(), tag, context)
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Deletes the tag with the matching tag name for the given {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the tag for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTag -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.deleteTag&#40;tag&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.deleteTag -->
     *
     * @param tag The name of the tag that uniquely identifies the tag that needs to be deleted.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException thrown if the {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTag(String tag) {
        return deleteTagWithResponse(tag).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the {@link ArtifactManifestProperties properties} associated with an artifact in given {@link #getRepositoryName() repository}.
     *
     * <p>This method can take in both a digest as well as a tag.<br>
     * In case a tag is provided it calls the service to get the digest associated with the given tag.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestPropertiesWithResponse -->
     * <pre>
     * client.getManifestPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ArtifactManifestProperties properties = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestPropertiesWithResponse -->
     *
     * @return A REST response containing {@link ArtifactManifestProperties properties} associated with the given {@code Digest}.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ArtifactManifestProperties>> getManifestPropertiesWithResponse() {
        return withContext(context -> getManifestPropertiesWithResponse(context));
    }

    private  Mono<Response<ArtifactManifestProperties>> getManifestPropertiesWithResponse(Context context) {
        return digestMono
            .flatMap(res -> serviceClient.getManifestPropertiesWithResponseAsync(getRepositoryName(), res, context))
            .<Response<ArtifactManifestProperties>>map(internalResponse -> new SimpleResponse<>(internalResponse,
                ArtifactManifestPropertiesHelper.create(internalResponse.getValue())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Gets the {@link ArtifactManifestProperties properties} associated with an artifact in given {@link #getRepositoryName() repository}.
     *
     * <p>This method can take in both a digest as well as a tag.<br>
     * In case a tag is provided it calls the service to get the digest associated with the given tag.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestProperties -->
     * <pre>
     * client.getManifestProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.getManifestProperties -->
     *
     * @return The {@link ArtifactManifestProperties properties} associated with the given {@code Digest}.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ArtifactManifestProperties> getManifestProperties() {
        return getManifestPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the tag properties associated with a given tag in the {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the properties associated with the given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.getTagPropertiesWithResponse -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.getTagPropertiesWithResponse&#40;tag&#41;.subscribe&#40;response -&gt; &#123;
     *     final ArtifactTagProperties properties = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.getTagPropertiesWithResponse -->
     *
     * @param tag name of the tag that uniquely identifies a given tag.
     * @return A REST response with the {@link ArtifactTagProperties properties} associated with the given tag.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code tag} is null.
     * @throws IllegalArgumentException thrown if the {@code tag} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ArtifactTagProperties>> getTagPropertiesWithResponse(String tag) {
        return withContext(context -> getTagPropertiesWithResponse(tag, context));
    }

    private Mono<Response<ArtifactTagProperties>> getTagPropertiesWithResponse(String tag, Context context) {
        if (tag == null) {
            return monoError(LOGGER, new NullPointerException("'tag' cannot be null."));
        }
        if (tag.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'tag' cannot be empty."));
        }

        return serviceClient.getTagPropertiesWithResponseAsync(getRepositoryName(), tag, context)
            .<Response<ArtifactTagProperties>>map(internalResponse -> new SimpleResponse<>(internalResponse,
                ArtifactTagPropertiesHelper.create(internalResponse.getValue())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Gets the tag properties associated with a given tag in the {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the properties associated with the given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.getTagProperties -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.getTagProperties&#40;tag&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.getTagProperties -->
     *
     * @param tag name of the tag that uniquely identifies a given tag.
     * @return The {@link ArtifactTagProperties properties} associated with the given tag.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code tag} is null.
     * @throws IllegalArgumentException thrown if the {@code tag} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ArtifactTagProperties> getTagProperties(String tag) {
        return getTagPropertiesWithResponse(tag).flatMap(FluxUtil::toMono);
    }

    /**
     * Fetches all the tags associated with the given {@link #getRepositoryName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listTagProperties(ArtifactTagOrder)}  listTagProperties}
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all the tags associated with the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.listTagProperties -->
     * <pre>
     * client.listTagProperties&#40;&#41;.byPage&#40;10&#41;
     *     .subscribe&#40;tagPropertiesPagedResponse -&gt; &#123;
     *         tagPropertiesPagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             tagProperties -&gt; System.out.println&#40;tagProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.listTagProperties -->
     *
     * @return {@link PagedFlux} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactTagProperties> listTagProperties() {
        return listTagProperties(ArtifactTagOrder.NONE);
    }

    /**
     * Fetches all the tags associated with the given {@link #getRepositoryName() repository}.
     *
     * <p> The method supports options to select the order in which the tags are returned by the service.
     * Currently the service supports an ascending or descending order based on the last updated time of the tag.
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all the tags associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.listTagPropertiesWithOptions -->
     * <pre>
     * client.listTagProperties&#40;ArtifactTagOrder.LAST_UPDATED_ON_DESCENDING&#41;
     *     .byPage&#40;10&#41;
     *     .subscribe&#40;tagPropertiesPagedResponse -&gt; &#123;
     *         tagPropertiesPagedResponse.getValue&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;tagProperties -&gt; System.out.println&#40;tagProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.listTagPropertiesWithOptions -->
     *
     * @param order The order in which the tags should be returned by the service.
     * @return {@link PagedFlux} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactTagProperties> listTagProperties(ArtifactTagOrder order) {
        return new PagedFlux<>(
            (pageSize) -> withContext(context -> listTagPropertiesSinglePageAsync(pageSize, order, context)),
            (token, pageSize) -> withContext(context -> listTagPropertiesNextSinglePageAsync(token, context)));
    }

    private  Mono<PagedResponse<ArtifactTagProperties>> listTagPropertiesSinglePageAsync(Integer pageSize, ArtifactTagOrder order, Context context) {
        if (pageSize != null && pageSize < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        final String orderString = order.equals(ArtifactTagOrder.NONE) ? null : order.toString();

        return digestMono
            .flatMap(digest -> serviceClient.getTagsSinglePageAsync(getRepositoryName(), null, pageSize, orderString, digest, context))
            .map(digest -> UtilsImpl.getPagedResponseWithContinuationToken(digest,
                baseValues -> UtilsImpl.getTagProperties(baseValues, getRepositoryName())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private Mono<PagedResponse<ArtifactTagProperties>> listTagPropertiesNextSinglePageAsync(String nextLink, Context context) {
        return serviceClient.getTagsNextSinglePageAsync(nextLink, context)
            .map(res -> UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseValues -> UtilsImpl.getTagProperties(baseValues, getRepositoryName())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Update the properties {@link ArtifactTagProperties} of the tag with the given name {@code tag}..
     * These properties set whether the given tag can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse -->
     * <pre>
     * ArtifactTagProperties properties = getTagProperties&#40;&#41;;
     * String tag = getTag&#40;&#41;;
     * client.updateTagPropertiesWithResponse&#40;tag, properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse -->
     *
     * @param tag Name of the tag that uniquely identifies it.
     * @param tagProperties {@link ArtifactTagProperties value} to be set.
     * @return A REST response for completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code tag} is null.
     * @throws IllegalArgumentException thrown if the {@code tag} is empty.
     * @throws NullPointerException thrown if {@code tagProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ArtifactTagProperties>> updateTagPropertiesWithResponse(
            String tag, ArtifactTagProperties tagProperties) {
        return withContext(context -> updateTagPropertiesWithResponse(tag, tagProperties, context));
    }

    private Mono<Response<ArtifactTagProperties>> updateTagPropertiesWithResponse(
        String tag, ArtifactTagProperties tagProperties, Context context) {
        if (tag == null) {
            return monoError(LOGGER, new NullPointerException("'tag' cannot be null."));
        }

        if (tag.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'tag' cannot be empty."));
        }

        if (tagProperties == null) {
            return monoError(LOGGER, new NullPointerException("'tagProperties' cannot be null."));
        }

        TagWriteableProperties writeableProperties = new TagWriteableProperties()
            .setDeleteEnabled(tagProperties.isDeleteEnabled())
            .setListEnabled(tagProperties.isListEnabled())
            .setReadEnabled(tagProperties.isReadEnabled())
            .setWriteEnabled(tagProperties.isWriteEnabled());

        return serviceClient.updateTagAttributesWithResponseAsync(getRepositoryName(), tag, writeableProperties, context)
            .<Response<ArtifactTagProperties>>map(internalResponse -> new SimpleResponse<>(internalResponse,
                ArtifactTagPropertiesHelper.create(internalResponse.getValue())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Update the properties {@link ArtifactTagProperties} of the tag with the given name {@code tag}.
     * These properties set whether the given tag can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse -->
     * <pre>
     * ArtifactTagProperties properties = getTagProperties&#40;&#41;;
     * String tag = getTag&#40;&#41;;
     * client.updateTagPropertiesWithResponse&#40;tag, properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.updateTagPropertiesWithResponse -->
     *
     * @param tag Name of the tag that uniquely identifies it.
     * @param tagProperties {@link ArtifactTagProperties tagProperties} to be set.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code tag} is null.
     * @throws IllegalArgumentException thrown if the {@code tag} is empty.
     * @throws NullPointerException thrown if {@code tagProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ArtifactTagProperties> updateTagProperties(String tag, ArtifactTagProperties tagProperties) {
        return updateTagPropertiesWithResponse(tag, tagProperties).flatMap(FluxUtil::toMono);
    }

    /**
     * Update the properties {@link ArtifactManifestProperties} of the artifact with the given {@code digest}.
     * These properties set whether the given manifest can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given manifest.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestPropertiesWithResponse -->
     * <pre>
     * ArtifactManifestProperties properties = getArtifactManifestProperties&#40;&#41;;
     * client.updateManifestPropertiesWithResponse&#40;properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestPropertiesWithResponse -->
     *
     * @param manifestProperties {@link ArtifactManifestProperties manifestProperties} to be set.
     * @return A REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code manifestProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ArtifactManifestProperties>> updateManifestPropertiesWithResponse(ArtifactManifestProperties manifestProperties) {
        return withContext(context -> updateManifestPropertiesWithResponse(manifestProperties, context));
    }

    private Mono<Response<ArtifactManifestProperties>> updateManifestPropertiesWithResponse(
        ArtifactManifestProperties manifestProperties, Context context) {
        if (manifestProperties == null) {
            return monoError(LOGGER, new NullPointerException("'value' cannot be null."));
        }

        ManifestWriteableProperties writeableProperties = new ManifestWriteableProperties()
            .setDeleteEnabled(manifestProperties.isDeleteEnabled())
            .setListEnabled(manifestProperties.isListEnabled())
            .setWriteEnabled(manifestProperties.isWriteEnabled())
            .setReadEnabled(manifestProperties.isReadEnabled());

        return digestMono
            .flatMap(digest -> serviceClient.updateManifestPropertiesWithResponseAsync(getRepositoryName(), digest,
                writeableProperties, context))
            .<Response<ArtifactManifestProperties>>map(internalResponse -> new SimpleResponse<>(internalResponse,
                ArtifactManifestPropertiesHelper.create(internalResponse.getValue())))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Update the properties {@link ArtifactManifestProperties} of the artifact with the given {@code digest}.
     * These properties set whether the given manifest can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given manifest.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestProperties -->
     * <pre>
     * ArtifactManifestProperties properties = getArtifactManifestProperties&#40;&#41;;
     * client.updateManifestProperties&#40;properties&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifactAsync.updateManifestProperties -->
     *
     * @param manifestProperties {@link ArtifactManifestProperties manifestProperties} to be set.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code manifestProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ArtifactManifestProperties> updateManifestProperties(ArtifactManifestProperties manifestProperties) {
        return updateManifestPropertiesWithResponse(manifestProperties).flatMap(FluxUtil::toMono);
    }


    /**
     * Gets the Azure Container Registry service endpoint for the current instance.
     * @return The service endpoint for the current instance.
     */
    public String getRegistryEndpoint() {
        return endpoint;
    }

    /**
     * Gets the fully qualified reference for the current instance.
     * The fully qualifiedName is of the form 'registryName/repositoryName@digest'
     * or 'registryName/repositoryName:tag' based on the docker naming convention and whether
     * tag or digest was supplied to the constructor.
     * @return Fully qualified reference of the current instance.
     * */
    public String getFullyQualifiedReference() {
        return fullyQualifiedReference;
    }

    /**
     * Gets the repository name for the current instance.
     * Gets the repository name for the current instance.
     * @return Name of the repository for the current instance.
     * */
    public String getRepositoryName() {
        return repositoryName;
    }
}
