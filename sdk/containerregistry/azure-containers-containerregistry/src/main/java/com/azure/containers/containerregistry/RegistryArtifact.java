// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.ArtifactManifestPropertiesHelper;
import com.azure.containers.containerregistry.implementation.ArtifactTagPropertiesHelper;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ArtifactManifestPropertiesInternal;
import com.azure.containers.containerregistry.implementation.models.ArtifactTagPropertiesInternal;
import com.azure.containers.containerregistry.implementation.models.ManifestWriteableProperties;
import com.azure.containers.containerregistry.implementation.models.TagAttributesBase;
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
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.formatFullyQualifiedReference;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.isDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;

/**
 * This class provides a helper type that contains all the operations for artifacts in a given repository.
 *
 * <p><strong>Instantiating Registry Artifact </strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.instantiation -->
 * <pre>
 * RegistryArtifact registryArtifact = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;.getArtifact&#40;repository, digest&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.RegistryArtifact.instantiation -->
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class RegistryArtifact {
    private static final ClientLogger LOGGER = new ClientLogger(RegistryArtifact.class);
    private final String fullyQualifiedReference;
    private final String endpoint;
    private final String repositoryName;
    private final String tagOrDigest;
    private final ContainerRegistriesImpl serviceClient;
    private String digest;

    /**
     * Creates a RegistryArtifact type that sends requests to the given repository in the container registry service at {@code endpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param repositoryName The name of the repository on which the service operations are performed.
     * @param tagOrDigest The tag or digest associated with the given artifact.
     * @param endpoint The URL string for the Azure Container Registry service.
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     */
    RegistryArtifact(String repositoryName, String tagOrDigest, HttpPipeline httpPipeline, String endpoint, String version) {
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
        this.tagOrDigest = tagOrDigest;
    }

    /**
     * Deletes the registry artifact with the digest and repository associated with the instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the registry artifact.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.deleteWithResponse#Context -->
     * <pre>
     * client.deleteWithResponse&#40;Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.deleteWithResponse#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the result of the service call.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Context context) {
        String res = getDigest();
        try {
            Response<Void> response = serviceClient.deleteManifestWithResponse(getRepositoryName(), res, context);

            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private String getDigest() {
        if (digest == null) {
            digest = isDigest(tagOrDigest)
                ? tagOrDigest
                : getTagProperties(tagOrDigest).getDigest();
        }
        return digest;
    }

    /**
     * Deletes the registry artifact with the digest and repository associated with the instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the registry artifact.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.delete -->
     * <pre>
     * client.delete&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.delete -->
     *
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(Context.NONE).getValue();
    }

    /**
     * Deletes the tag with the matching tag name for the given {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the tag for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.deleteTagWithResponse -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.deleteTagWithResponse&#40;tag, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.deleteTagWithResponse -->
     *
     * @param tag The name of the tag that needs to be deleted.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the result of the service call.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException thrown if {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTagWithResponse(String tag, Context context) {
        Objects.requireNonNull(tag, "'tag' cannot be null.");
        if (tag.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'tag' cannot be empty."));
        }
        try {
            Response<Void> response = serviceClient.deleteTagWithResponse(getRepositoryName(), tag, context);

            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Deletes the tag with the matching tag name for the given {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the tag for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.deleteTag -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * client.deleteTag&#40;tag&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.deleteTag -->
     *
     * @param tag The name of the tag that needs to be deleted.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException throws if {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTag(String tag) {
        deleteTagWithResponse(tag, Context.NONE).getValue();
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
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.getManifestPropertiesWithResponse -->
     * <pre>
     * Response&lt;ArtifactManifestProperties&gt; response = client.getManifestPropertiesWithResponse&#40;
     *     Context.NONE&#41;;
     * final ArtifactManifestProperties properties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.getManifestPropertiesWithResponse -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing {@link ArtifactManifestProperties properties} associated with the given {@code Digest}.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ArtifactManifestProperties> getManifestPropertiesWithResponse(Context context) {
        String res = getDigest();
        try {
            Response<ArtifactManifestPropertiesInternal> internalResponse = this.serviceClient
                .getManifestPropertiesWithResponse(getRepositoryName(), res, context);

            return new SimpleResponse<>(internalResponse,
                ArtifactManifestPropertiesHelper.create(internalResponse.getValue()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Gets the {@link ArtifactManifestProperties properties} associated with an artifact in given {@link #getRepositoryName() repository}.
     *
     * <p>This method can take in both a digest as well as a tag.<br>
     * In case a tag is provided it calls the service to get the digest associated with the given tag.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the registry artifact properties for a given tag or digest.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.getManifestProperties -->
     * <pre>
     * ArtifactManifestProperties properties = client.getManifestProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.getManifestProperties -->
     *
     * @return The {@link ArtifactManifestProperties properties} associated with the given {@code Digest}.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the given digest was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ArtifactManifestProperties getManifestProperties() {
        return getManifestPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Gets the tag properties associated with a given tag in the {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the properties associated with the given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.getTagPropertiesWithResponse -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * Response&lt;ArtifactTagProperties&gt; response = client.getTagPropertiesWithResponse&#40;tag, Context.NONE&#41;;
     * final ArtifactTagProperties properties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.getTagPropertiesWithResponse -->
     *
     * @param tag name of the tag.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the {@link ArtifactTagProperties properties} associated with the given tag.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException throws if {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ArtifactTagProperties> getTagPropertiesWithResponse(String tag, Context context) {
        Objects.requireNonNull(tag, "'tag' cannot be null.");
        if (tag.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'tag' cannot be empty."));
        }

        try {
            Response<ArtifactTagPropertiesInternal> internalResponse = this.serviceClient
                .getTagPropertiesWithResponse(getRepositoryName(), tag, context);

            return new SimpleResponse<>(internalResponse,
                ArtifactTagPropertiesHelper.create(internalResponse.getValue()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Gets the tag properties associated with a given tag in the {@link #getRepositoryName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the properties associated with the given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.getTagProperties -->
     * <pre>
     * String tag = getTag&#40;&#41;;
     * ArtifactTagProperties properties = client.getTagProperties&#40;tag&#41;;
     * System.out.printf&#40;&quot;Digest:%s,&quot;, properties.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.getTagProperties -->
     *
     * @param tag name of the tag.
     * @return The {@link ArtifactTagProperties properties} associated with the given tag.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the given tag was not found.
     * @throws NullPointerException thrown if {@code tag} is null.
     * @throws IllegalArgumentException throws if {@code tag} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ArtifactTagProperties getTagProperties(String tag) {
        return getTagPropertiesWithResponse(tag, Context.NONE).getValue();
    }

    /**
     * Fetches all the tags associated with the given {@link #getRepositoryName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listTagProperties(ArtifactTagOrder, Context)}  listTagProperties}
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all the tags associated with the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.listTagProperties -->
     * <pre>
     * client.listTagProperties&#40;&#41;.iterableByPage&#40;10&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *         tagProperties -&gt; System.out.println&#40;tagProperties.getDigest&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.listTagProperties -->
     *
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactTagProperties> listTagProperties() {
        return listTagProperties(ArtifactTagOrder.NONE, Context.NONE);
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
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.listTagPropertiesWithOptionsNoContext -->
     * <pre>
     * client.listTagProperties&#40;ArtifactTagOrder.LAST_UPDATED_ON_DESCENDING&#41;
     *     .iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;tagProperties -&gt; System.out.println&#40;tagProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.listTagPropertiesWithOptionsNoContext -->
     *
     * @param order The order in which the tags should be returned by the service.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactTagProperties> listTagProperties(ArtifactTagOrder order) {
        return listTagProperties(order, Context.NONE);
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
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.listTagPropertiesWithOptions -->
     * <pre>
     * client.listTagProperties&#40;ArtifactTagOrder.LAST_UPDATED_ON_DESCENDING, Context.NONE&#41;
     *     .iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;tagProperties -&gt; System.out.println&#40;tagProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.listTagPropertiesWithOptions -->
     *
     * @param order The order in which the tags should be returned by the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactTagProperties> listTagProperties(ArtifactTagOrder order, Context context) {
        return listTagPropertiesSync(order, context);
    }

    private PagedResponse<ArtifactTagProperties> listTagPropertiesNextSinglePageSync(String nextLink, Context context) {
        try {
            PagedResponse<TagAttributesBase> res = serviceClient.getTagsNextSinglePage(nextLink,
                context);

            return UtilsImpl.getPagedResponseWithContinuationToken(res,
                baseValues -> UtilsImpl.getTagProperties(baseValues, getRepositoryName()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Update the properties {@link ArtifactTagProperties} of the given tag in {@link #getRepositoryName() repository}.
     * These properties set whether the given tag can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.updateTagPropertiesWithResponse -->
     * <pre>
     * ArtifactTagProperties properties = getArtifactTagProperties&#40;&#41;;
     * String tag = getTag&#40;&#41;;
     * client.updateTagPropertiesWithResponse&#40;tag, properties, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.updateTagPropertiesWithResponse -->
     *
     * @param tag Name of the tag.
     * @param tagProperties {@link ArtifactTagProperties tagProperties} to be set.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given {@code tag} was not found.
     * @throws NullPointerException thrown if {@code tag} or {@code tagProperties} is null.
     * @throws IllegalArgumentException thrown if {@code tag} or {@code tagProperties} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ArtifactTagProperties> updateTagPropertiesWithResponse(String tag, ArtifactTagProperties tagProperties, Context context) {
        Objects.requireNonNull(tag, "'tag' cannot be null.");
        Objects.requireNonNull(tagProperties, "'tagProperties' cannot be null.");

        if (tag.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'tag' cannot be empty."));
        }

        TagWriteableProperties writeableProperties = new TagWriteableProperties()
            .setDeleteEnabled(tagProperties.isDeleteEnabled())
            .setListEnabled(tagProperties.isListEnabled())
            .setReadEnabled(tagProperties.isReadEnabled())
            .setWriteEnabled(tagProperties.isWriteEnabled());

        try {
            Response<ArtifactTagPropertiesInternal> internalResponse = this.serviceClient
                .updateTagAttributesWithResponse(getRepositoryName(), tag, writeableProperties, context);

            return new SimpleResponse<>(internalResponse,
                ArtifactTagPropertiesHelper.create(internalResponse.getValue()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Update the properties {@link ArtifactTagProperties} of the given {@code tag}.
     * These properties set whether the given tag can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given tag.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.updateTagProperties -->
     * <pre>
     * ArtifactTagProperties properties = getArtifactTagProperties&#40;&#41;;
     * String tag = getTag&#40;&#41;;
     * client.updateTagProperties&#40;tag, properties&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.updateTagProperties -->
     *
     * @param tag Name of the tag.
     * @param tagProperties {@link ArtifactTagProperties tagProperties} to be set.
     * @return The updated {@link ArtifactTagProperties properties }
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given {@code tag} was not found.
     * @throws NullPointerException thrown if {@code tag} or {@code tagProperties} is null.
     * @throws IllegalArgumentException thrown if {@code tag} or {@code tagProperties} is empty.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ArtifactTagProperties updateTagProperties(String tag, ArtifactTagProperties tagProperties) {
        return updateTagPropertiesWithResponse(tag, tagProperties, Context.NONE).getValue();
    }

    /**
     * Update the properties {@link ArtifactTagProperties} of the artifact with the given {@code digest}.
     * These properties set whether the given manifest can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given artifact.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.updateManifestPropertiesWithResponse -->
     * <pre>
     * ArtifactManifestProperties properties = getArtifactManifestProperties&#40;&#41;;
     * client.updateManifestPropertiesWithResponse&#40;properties, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.updateManifestPropertiesWithResponse -->
     *
     * @param manifestProperties {@link ArtifactManifestProperties tagProperties} to be set.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws NullPointerException thrown if the {@code manifestProperties} is null.
     * @throws ResourceNotFoundException thrown if the given {@code digest} was not found.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ArtifactManifestProperties> updateManifestPropertiesWithResponse(
        ArtifactManifestProperties manifestProperties, Context context) {
        Objects.requireNonNull(manifestProperties, "'manifestProperties' cannot be null.");

        ManifestWriteableProperties writeableProperties = new ManifestWriteableProperties()
            .setDeleteEnabled(manifestProperties.isDeleteEnabled())
            .setListEnabled(manifestProperties.isListEnabled())
            .setWriteEnabled(manifestProperties.isWriteEnabled())
            .setReadEnabled(manifestProperties.isReadEnabled());

        String res = getDigest();
        try {
            Response<ArtifactManifestPropertiesInternal> internalResponse = this.serviceClient
                .updateManifestPropertiesWithResponse(getRepositoryName(), res, writeableProperties,
                    context);

            return new SimpleResponse<>(internalResponse,
                ArtifactManifestPropertiesHelper.create(internalResponse.getValue()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Update the writeable properties {@link ArtifactTagProperties} of the artifact with the given {@code digest}.
     * These properties set whether the given manifest can be updated, deleted and retrieved.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties of a given manifest.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.RegistryArtifact.updateManifestProperties -->
     * <pre>
     * ArtifactManifestProperties properties = getArtifactManifestProperties&#40;&#41;;
     * client.updateManifestProperties&#40;properties&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.RegistryArtifact.updateManifestProperties -->
     *
     * @param manifestProperties {@link ArtifactManifestProperties manifestProperties} to be set.
     * @return The updated {@link ArtifactManifestProperties properties }
     * @throws ClientAuthenticationException thrown if the client does not have access to repository.
     * @throws ResourceNotFoundException thrown if the given {@code digest} was not found.
     * @throws NullPointerException thrown if the {@code manifestProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ArtifactManifestProperties updateManifestProperties(ArtifactManifestProperties manifestProperties) {
        return updateManifestPropertiesWithResponse(manifestProperties, Context.NONE).getValue();
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


    private PagedIterable<ArtifactTagProperties> listTagPropertiesSync(ArtifactTagOrder order, Context context) {
        return new PagedIterable<>(
            (pageSize) -> listTagPropertiesSinglePageSync(pageSize, order, context),
            (token, pageSize) -> listTagPropertiesNextSinglePageSync(token, context));
    }

    private PagedResponse<ArtifactTagProperties> listTagPropertiesSinglePageSync(Integer pageSize, ArtifactTagOrder order, Context context) {
        if (pageSize != null && pageSize < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        final String orderString = order.equals(ArtifactTagOrder.NONE) ? null : order.toString();

        String res = getDigest();
        try {
            PagedResponse<TagAttributesBase> response =
                serviceClient.getTagsSinglePage(getRepositoryName(), null, pageSize, orderString, res,
                    context);

            return UtilsImpl.getPagedResponseWithContinuationToken(response,
                baseValues -> UtilsImpl.getTagProperties(baseValues, getRepositoryName()));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }
}
