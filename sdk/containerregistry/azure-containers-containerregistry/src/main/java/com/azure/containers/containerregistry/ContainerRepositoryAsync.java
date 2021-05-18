// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.ArtifactManifestPropertiesHelper;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryImplBuilder;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesBase;
import com.azure.containers.containerregistry.implementation.models.RepositoryWriteableProperties;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.models.ManifestOrderBy;
import com.azure.containers.containerregistry.models.RepositoryProperties;
import com.azure.core.annotation.ReturnType;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides helper methods for operations on a given repository in Azure Container Registry.
 * Operations included are listing, retrieving, deleting, setting writeable properties.
 *
 * <p><strong>Instantiating an asynchronous Container Repository Helper class</strong></p>
 *
 * {@codesnippet com.azure.containers.containerregistry.async.repository.instantiation}
 *
 */
public final class ContainerRepositoryAsync {
    private final ContainerRegistriesImpl serviceClient;
    private final String repositoryName;
    private final String endpoint;
    private final String apiVersion;
    private final HttpPipeline httpPipeline;
    private final String registryLoginServer;

    private final ClientLogger logger = new ClientLogger(ContainerRepositoryAsync.class);

    /**
     * Creates a ContainerRepositoryAsyncClient that sends requests to the given repository in the container registry service at {@code endpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param repositoryName The name of the repository on which the service operations are performed.
     * @param endpoint The URL string for the Azure Container Registry service.
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     */
    ContainerRepositoryAsync(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version) {
        if (repositoryName == null) {
            throw logger.logExceptionAsError(new NullPointerException("'repositoryName' can't be null"));
        }

        ContainerRegistryImpl registryImpl = new ContainerRegistryImplBuilder()
            .pipeline(httpPipeline)
            .url(endpoint).buildClient();

        this.endpoint = endpoint;
        this.repositoryName = repositoryName;
        this.serviceClient = registryImpl.getContainerRegistries();
        this.apiVersion = version;
        this.httpPipeline = httpPipeline;

        try {
            URL endpointUrl = new URL(endpoint);
            this.registryLoginServer = endpointUrl.getHost();
        } catch (MalformedURLException ex) {
            // This will not happen.
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
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
     * {@codesnippet com.azure.containers.containerregistry.async.repository.deleteRepositoryWithResponse}
     *
     * @return A REST response containing the result of the repository delete operation. It returns the count of the tags and
     * artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeleteRepositoryResult>> deleteWithResponse() {
        return withContext(context -> deleteWithResponse(context));
    }

    Mono<Response<DeleteRepositoryResult>> deleteWithResponse(Context context) {
        try {
            return this.serviceClient.deleteRepositoryWithResponseAsync(repositoryName, context)
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.deleteRepository}
     *
     * @return It returns the count of the tags and artifacts that are deleted as part of the repository delete.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeleteRepositoryResult> delete() {
        return this.deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get an instance of registry artifact class.
     *
     * @param tagOrDigest Tag or digest associated with the artifact.
     * @return repository client.
     */
    public RegistryArtifactAsync getArtifact(String tagOrDigest) {
        return new RegistryArtifactAsync(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listManifests(ManifestOrderBy)}  listManifests}
     * No assumptions on the order can be made if no options are provided to the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.listManifests}.
     *
     * @return {@link PagedFlux} of ManifestProperties for all the artifacts in the given repository.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactManifestProperties> listManifests() {
        return listManifests(ManifestOrderBy.NONE);
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
     * {@codesnippet com.azure.containers.containerregistry.async.repository.listManifestsWithOptions}.
     *
     * @param orderBy The order in which the artifacts are returned by the service.
     * @return {@link PagedFlux} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ArtifactManifestProperties> listManifests(ManifestOrderBy orderBy) {
        return new PagedFlux<>(
            (pageSize) -> withContext(context -> listManifestsSinglePageAsync(pageSize, orderBy, context)),
            (token, pageSize) -> withContext(context -> listManifestsNextSinglePageAsync(token, context)));
    }

    PagedFlux<ArtifactManifestProperties> listManifests(ManifestOrderBy orderBy, Context context) {
        return new PagedFlux<>(
            (pageSize) -> listManifestsSinglePageAsync(pageSize, orderBy, context),
            (token, pageSize) -> listManifestsNextSinglePageAsync(token, context));
    }

    Mono<PagedResponse<ArtifactManifestProperties>> listManifestsSinglePageAsync(Integer pageSize, ManifestOrderBy orderBy, Context context) {
        try {
            if (pageSize != null && pageSize < 0) {
                return monoError(logger, new IllegalArgumentException("'pageSize' cannot be negative."));
            }

            final String orderByString = orderBy == ManifestOrderBy.NONE ? null : orderBy.toString();
            return this.serviceClient.getManifestsSinglePageAsync(repositoryName, null, pageSize, orderByString, context)
                .map(res -> Utils.getPagedResponseWithContinuationToken(res, this::mapManifestsProperties))
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<PagedResponse<ArtifactManifestProperties>> listManifestsNextSinglePageAsync(String nextLink, Context context) {
        try {
            return this.serviceClient.getManifestsNextSinglePageAsync(nextLink, context)
                .map(res -> Utils.getPagedResponseWithContinuationToken(res, this::mapManifestsProperties))
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private List<ArtifactManifestProperties> mapManifestsProperties(List<ManifestAttributesBase> baseArtifacts) {
        if (baseArtifacts == null) {
            return null;
        }

        return baseArtifacts.stream().map(value -> {
            ArtifactManifestProperties manifestProperties = new ArtifactManifestProperties()
                .setDeleteEnabled(value.isDeleteEnabled())
                .setListEnabled(value.isListEnabled())
                .setWriteEnabled(value.isWriteEnabled())
                .setReadEnabled(value.isReadEnabled());

            ArtifactManifestPropertiesHelper.setRepositoryName(manifestProperties, repositoryName);
            ArtifactManifestPropertiesHelper.setRegistryLoginServer(manifestProperties, registryLoginServer);
            ArtifactManifestPropertiesHelper.setDigest(manifestProperties, value.getDigest());
            ArtifactManifestPropertiesHelper.setManifestReferences(manifestProperties, value.getManifestReferences());
            ArtifactManifestPropertiesHelper.setCpuArchitecture(manifestProperties, value.getArchitecture());
            ArtifactManifestPropertiesHelper.setOperatingSystem(manifestProperties, value.getOperatingSystem());
            ArtifactManifestPropertiesHelper.setCreatedOn(manifestProperties, value.getCreatedOn());
            ArtifactManifestPropertiesHelper.setlastUpdatedOn(manifestProperties, value.getLastUpdatedOn());
            ArtifactManifestPropertiesHelper.setSize(manifestProperties, value.getSize());
            ArtifactManifestPropertiesHelper.setTags(manifestProperties, value.getTags());
            return manifestProperties;
        }).collect(Collectors.toList());
    }

    /**
     * Gets the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.getPropertiesWithResponse}
     *
     * @return A REST response with the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RepositoryProperties>> getPropertiesWithResponse() {
        return withContext(context -> this.getPropertiesWithResponse(context));
    }

    Mono<Response<RepositoryProperties>> getPropertiesWithResponse(Context context) {
        try {
            if (context == null) {
                return monoError(logger, new NullPointerException("'context' cannot be null."));
            }
            return this.serviceClient.getPropertiesWithResponseAsync(repositoryName, context)
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.getProperties}
     *
     * @return The {@link RepositoryProperties properties} associated with the given {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RepositoryProperties> getProperties() {
        return this.getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Update the repository properties {@link RepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.setPropertiesWithResponse}.
     *
     * @param value {@link RepositoryProperties repository properties} that need to be updated for the repository.
     * @return The updated {@link RepositoryProperties repository properties }.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RepositoryProperties>> setPropertiesWithResponse(RepositoryProperties value) {
        return withContext(context -> this.setPropertiesWithResponse(value, context));
    }

    Mono<Response<RepositoryProperties>> setPropertiesWithResponse(RepositoryProperties value, Context context) {
        try {
            if (value == null) {
                return monoError(logger, new NullPointerException("'value' cannot be null."));
            }

            RepositoryWriteableProperties writableProperties = new RepositoryWriteableProperties()
                .setDeleteEnabled(value.isDeleteEnabled())
                .setListEnabled(value.isListEnabled())
                .setWriteEnabled(value.isWriteEnabled())
                .setReadEnabled(value.isReadEnabled())
                .setTeleportEnabled(value.isTeleportEnabled());

            return this.serviceClient.setPropertiesWithResponseAsync(repositoryName, writableProperties, context)
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Update the repository properties {@link RepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * {@codesnippet com.azure.containers.containerregistry.async.repository.setProperties}.
     *
     * @param value {@link RepositoryProperties writeable properties} that need to be updated for the repository.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RepositoryProperties> setProperties(RepositoryProperties value) {
        return this.setPropertiesWithResponse(value).flatMap(FluxUtil::toMono);
    }
}
