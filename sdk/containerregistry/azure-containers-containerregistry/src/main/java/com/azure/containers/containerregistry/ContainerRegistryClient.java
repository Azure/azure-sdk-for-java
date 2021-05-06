// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous ContainerRegistry type. */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class ContainerRegistryClient {
    private final ContainerRegistryAsyncClient asyncClient;

    ContainerRegistryClient(ContainerRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * This method returns the login server associated with the given client.
     * @return The full login server name including the namespace.
     */
    public String getLoginServer() {
        return asyncClient.getLoginServer();
    }

    /**
     * This method returns the name of the registry.
     * @return The registry name without the namespace.
     */
    public String getName() {
        return asyncClient.getName();
    }

    /**
     * This method returns the complete registry endpoint.
     * @return The registry endpoint including the authority.
     */
    public String getEndpoint() {
        return asyncClient.getEndpoint();
    }

    /**
     * List all the repository names in this registry.
     *
     * @return list of repositories.
     * @throws ClientAuthenticationException thrown if the client credentials do not have access to perform this operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRepositoryNames() {
        return listRepositoryNames(Context.NONE);
    }

    /**
     * List all the repository names in this registry.
     *
     * @param context The context to associate with this operation.
     * @return list of repositories.
     * @throws ClientAuthenticationException thrown if the client credentials do not have access to perform this operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRepositoryNames(Context context) {
        return new PagedIterable<String>(asyncClient.listRepositoryNames(context));
    }

    /**
     * Delete the repository identified by 'repositoryName'.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @return deleted repository properties.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository to be deleted does not exist.
     * @throws NullPointerException thrown if the name is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeleteRepositoryResult deleteRepository(String repositoryName) {
        return this.deleteRepositoryWithResponse(repositoryName, Context.NONE).getValue();
    }

    /**
     * Delete the repository identified by 'repositoryName'.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @param context The context to associate with this operation.
     * @return deleted repository properties.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository to be deleted does not exist.
     * @throws NullPointerException thrown if the name is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeleteRepositoryResult> deleteRepositoryWithResponse(String repositoryName, Context context) {
        return this.asyncClient.deleteRepositoryWithResponse(repositoryName, context).block();
    }

    /**
     * Get an instance of repository client from the registry client.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @return repository client.
     */
    public ContainerRepository getRepository(String repositoryName) {
        return new ContainerRepository(this.asyncClient.getRepository(repositoryName));
    }

    /**
     * Get an instance of registry artifact class.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @param tagOrDigest Tag or digest associated with the artifact.
     * @return repository client.
     */
    public RegistryArtifact getArtifact(String repositoryName, String tagOrDigest) {
        return new RegistryArtifact(this.asyncClient.getArtifact(repositoryName, tagOrDigest));
    }
}
