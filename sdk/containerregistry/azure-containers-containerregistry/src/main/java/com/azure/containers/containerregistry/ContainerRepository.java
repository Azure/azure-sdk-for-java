// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.models.ManifestOrderBy;
import com.azure.containers.containerregistry.models.RepositoryProperties;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * This class provides a client that contains all the operations for repositories in Azure Container Registry.
 * Operations allowed by the client are listing, retrieving, deleting, setting writeable properties.
 * These operations are supported on the repository and the respective tags and manifests in it.
 *
 * <p><strong>Instantiating Container Repository Client</strong></p>
 *
 * {@codesnippet com.azure.containers.containerregistry.repository.instantiation}
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
public final class ContainerRepository {
    private final ContainerRepositoryAsync asyncClient;

    /**
     * Creates a ContainerRepositoryAsyncClient that sends requests to the given repository in the container registry service at {@code endpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param asyncClient The async client for the given repository.
     */
    ContainerRepository(ContainerRepositoryAsync asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the Azure Container Registry service endpoint for the current instance.
     * @return The service endpoint for the current instance.
     */
    public String getName() {
        return this.asyncClient.getName();
    }

    /**
     * Gets the Azure Container Registry name for the current instance.
     * @return Return the registry name.
     */
    public String getRegistryEndpoint() {
        return this.asyncClient.getRegistryEndpoint();
    }

    /**
     * Gets the fully qualified name for the current instance.
     * @return Fully qualified name of the current instance.
     * */
    public String getFullyQualifiedName() {
        return this.asyncClient.getFullyQualifiedName();
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.deleteRepositoryWithResponse}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the result of the repository delete operation. It returns the count of the tags and
     * artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeleteRepositoryResult> deleteWithResponse(Context context) {
        return this.asyncClient.deleteWithResponse(context).block();
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.deleteRepository}
     *
     * @return It returns the count of the tags and artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeleteRepositoryResult delete() {
        return this.deleteWithResponse(Context.NONE).getValue();
    }

    /**
     * Gets the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.getPropertiesWithResponse}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RepositoryProperties> getPropertiesWithResponse(Context context) {
        return this.asyncClient.getPropertiesWithResponse(context).block();
    }

    /**
     * Gets the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.getProperties}
     *
     * @return The{@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RepositoryProperties getProperties() {
        return this.getPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get an instance of registry artifact class.
     *
     * @param tagOrDigest Tag or digest associated with the artifact.
     * @return repository client.
     */
    public RegistryArtifact getArtifact(String tagOrDigest) {
        return new RegistryArtifact(this.asyncClient.getArtifact(tagOrDigest));
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listManifests(ManifestOrderBy, Context)}   listManifests}
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.listManifests}.
     *
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifests() {
        return this.listManifests(ManifestOrderBy.NONE, Context.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository }.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order for the last updated time for the artifacts.
     * No assumptions on the order can be made if no options are provided by the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.listManifestsWithOptions}.
     *
     * @param orderBy the order in which the artifacts are returned by the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifests(ManifestOrderBy orderBy, Context context) {
        return new PagedIterable<>(this.asyncClient.listManifests(orderBy, context));
    }

    /**
     * Update the writeable properties {@link ContentProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.updatePropertiesWithResponse}.
     *
     * @param value {@link ContentProperties writeable properties} that need to be updated for the repository.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RepositoryProperties> updatePropertiesWithResponse(ContentProperties value, Context context) {
        return this.asyncClient.updatePropertiesWithResponse(value, context).block();
    }

    /**
     * Update the writeable properties {@link ContentProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.repository.updateProperties}.
     *
     * @param value {@link ContentProperties writeable properties} that need to be updated for the repository.
     * @return The updated {@link RepositoryProperties properties }
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RepositoryProperties updateProperties(ContentProperties value) {
        return this.updatePropertiesWithResponse(value, Context.NONE).getValue();
    }
}
