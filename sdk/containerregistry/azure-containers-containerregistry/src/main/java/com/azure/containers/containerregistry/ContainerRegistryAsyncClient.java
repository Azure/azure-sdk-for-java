// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryImplBuilder;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Instantiates Container Registry async client.
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class, isAsync = true)
public final class ContainerRegistryAsyncClient {
    private final ContainerRegistryImpl registryImplClient;
    private final ContainerRegistriesImpl registriesImplClient;
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final String apiVersion;
    private final String registryName;
    private final String loginServer;

    private final ClientLogger logger = new ClientLogger(ContainerRegistryAsyncClient.class);

    ContainerRegistryAsyncClient(HttpPipeline httpPipeline, String endpoint, String version) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.registryImplClient = new ContainerRegistryImplBuilder()
            .url(endpoint)
            .pipeline(httpPipeline)
            .buildClient();
        this.registriesImplClient = this.registryImplClient.getContainerRegistries();
        this.apiVersion = version;

        try {
            URL endpointUrl = new URL(endpoint);
            this.loginServer = endpointUrl.getHost();
            this.registryName = this.loginServer.split("\\.")[0];
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
    }

    /**
     * This method returns the login server associated with the given client.
     * @return The full login server name including the namespace.
     */
    public String getLoginServer() {
        return this.loginServer;
    }

    /**
     * This method returns the name of the registry.
     * @return The registry name without the namespace.
     */
    public String getName() {
        return this.registryName;
    }

    /**
     * This method returns the complete registry endpoint.
     * @return The registry endpoint including the authority.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * List all the repository names in this registry.
     *
     * @return list of repository names.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRepositoryNames() {
        return new PagedFlux<>(
            (pageSize) -> withContext(context -> listRepositoryNamesSinglePageAsync(pageSize, context)),
            (token, pageSize) -> withContext(context -> listRepositoryNamesNextSinglePageAsync(token, context)));
    }

    PagedFlux<String> listRepositoryNames(Context context) {
        return new PagedFlux<>(
            (pageSize) -> listRepositoryNamesSinglePageAsync(pageSize, context),
            (token, pageSize) -> listRepositoryNamesNextSinglePageAsync(token, context));
    }

    Mono<PagedResponse<String>> listRepositoryNamesSinglePageAsync(Integer pageSize, Context context) {
        try {
            if (pageSize != null && pageSize < 0) {
                return monoError(logger, new IllegalArgumentException("'pageSize' cannot be negative."));
            }

            Mono<PagedResponse<String>> pagedResponseMono = this.registriesImplClient.getRepositoriesSinglePageAsync(null, pageSize, context)
                .map(res -> Utils.getPagedResponseWithContinuationToken(res))
                .onErrorMap(Utils::mapException);
            return pagedResponseMono;

        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<PagedResponse<String>> listRepositoryNamesNextSinglePageAsync(String nextLink, Context context) {
        try {
            Mono<PagedResponse<String>> pagedResponseMono = this.registriesImplClient.getRepositoriesNextSinglePageAsync(nextLink, context);
            return pagedResponseMono.map(res -> Utils.getPagedResponseWithContinuationToken(res));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Delete the repository identified by 'repositoryName'.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @return deleted repository properties.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the given name was not found.
     * @throws NullPointerException thrown if the name is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeleteRepositoryResult>> deleteRepositoryWithResponse(String repositoryName) {
        return withContext(context -> deleteRepositoryWithResponse(repositoryName, context));
    }

    Mono<Response<DeleteRepositoryResult>> deleteRepositoryWithResponse(String repositoryName, Context context) {
        try {
            if (repositoryName == null) {
                return monoError(logger, new NullPointerException("'repositoryName' cannot be null."));
            }

            return this.registriesImplClient.deleteRepositoryWithResponseAsync(repositoryName, context)
                .onErrorMap(Utils::mapException);

        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Delete the repository identified by 'repositoryName'.
     *
     * @param repositoryName Name of the image (including the namespace).
     * @return deleted repository properties.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the given name was not found.
     * @throws NullPointerException thrown if the name is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeleteRepositoryResult> deleteRepository(String repositoryName) {
        return withContext(context -> this.deleteRepository(repositoryName, context));
    }

    Mono<DeleteRepositoryResult> deleteRepository(String repositoryName, Context context) {
        return this.deleteRepositoryWithResponse(repositoryName, context).map(Response::getValue);
    }

    /**
     * Get an instance of container repository class.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @return repository client.
     */
    public ContainerRepositoryAsync getRepository(String repositoryName) {
        return new ContainerRepositoryAsync(repositoryName, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Get an instance of registry artifact class.
     *
     * @param repositoryName Name of the repository (including the namespace).
     * @param tagOrDigest Tag or digest associated with the artifact.
     * @return repository client.
     */
    public RegistryArtifactAsync getArtifact(String repositoryName, String tagOrDigest) {
        return new RegistryArtifactAsync(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }
}
