// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * This class provides a client that exposes operations to managing container images and artifacts.
 * synchronously. It exposes methods directly performed on the registry like listing the catalog.
 * as well as helper types like {@link #getArtifact(String, String) getArtifact} and {@link #getRepository(String) getRepository}
 * that can be used to perform operations on repository and artifacts.
 *
 * <p><strong>Instantiating a synchronous Container Registry client</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Container Registry client with custom pipeline</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation}
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class ContainerRegistryClient {
    private final ContainerRegistryAsyncClient asyncClient;

    ContainerRegistryClient(ContainerRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
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
     * <p><strong>List the repository names in the registry.</strong></p>
     *
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames}
     *
     * @return list of repository names.
     * @throws ClientAuthenticationException thrown if the client credentials do not have access to perform this operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRepositoryNames() {
        return listRepositoryNames(Context.NONE);
    }

    /**
     * List all the repository names in this registry.
     *
     * <p><strong>List the repository names in the registry.</strong></p>
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames#Context}
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
     * Delete the repository identified by {@code repositoryName}.
     *
     * <p><strong>Delete a repository in the registry.</strong></p>
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String}
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if the {@code repositoryName} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRepository(String repositoryName) {
        this.deleteRepositoryWithResponse(repositoryName, Context.NONE);
    }

    /**
     * Delete the repository identified by {@code repositoryName}.
     *
     *  <p><strong>Delete a repository in the registry.</strong></p>
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepositoryWithResponse#String-Context}
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @param context The context to associate with this operation.
     * @return Completion response.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if the {@code repositoryName} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRepositoryWithResponse(String repositoryName, Context context) {
        return this.asyncClient.deleteRepositoryWithResponse(repositoryName, context).block();
    }

    /**
     * Creates a new instance of {@link ContainerRepository} object for the specified repository.
     *
     *  <p><strong>Create a ContainerRegistry helper instance.</strong></p>
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.getRepository}
     *
     * @param repositoryName Name of the repository to reference.
     * @return A new {@link ContainerRepository} for the desired repository.
     * @throws NullPointerException if {@code repositoryName} is null.
     * @throws IllegalArgumentException if {@code repositoryName} is empty.
     */
    public ContainerRepository getRepository(String repositoryName) {
        return new ContainerRepository(this.asyncClient.getRepository(repositoryName));
    }

    /**
     * Creates a new instance of {@link RegistryArtifact} object for the specified artifact.
     *
     *  <p><strong>Create a RegistryArtifact helper instance.</strong></p>
     * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact}
     *
     *
     * @param repositoryName Name of the repository to reference.
     * @param digest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifact} object for the desired repository.
     * @throws NullPointerException if {@code repositoryName} or {@code digest} is null.
     * @throws IllegalArgumentException if {@code repositoryName} or {@code digest} is empty.
     */
    public RegistryArtifact getArtifact(String repositoryName, String digest) {
        return new RegistryArtifact(this.asyncClient.getArtifact(repositoryName, digest));
    }
}
