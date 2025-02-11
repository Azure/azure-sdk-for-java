// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * <p>This class provides a client that works with repositories in Azure Container Registry.
 * It allows to list and delete repositories within the registry or obtain an instance of {@link ContainerRepositoryAsync}
 * or {@link RegistryArtifactAsync} that can be used to perform operations on the repository or artifact.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Container Registry service you'll need to create an instance of
 * ContainerRegistryAsyncClient.</p>
 *
 * <p>To create the client and communicate with the service, you'll need to use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a></p>.
 *
 * <p><strong>Sample: Construct Container Registry Async Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a Container Registry Async Client.</p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation -->
 * <pre>
 * ContainerRegistryAsyncClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to
 * {@link com.azure.containers.containerregistry.ContainerRegistryClient}.</p>
 *
 * <p>View {@link ContainerRegistryClientBuilder} for additional ways to construct the client.</p>
 *
 * <p>The Container Registry Async Client allows to list and delete repositories and obtain instances of repository and
 * artifact client. See methods below to explore all capabilities this client provides.</p>

 * <p>View {@link ContainerRegistryClientBuilder} for additional ways to construct the client.</p>
 *
 * @see com.azure.containers.containerregistry
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class, isAsync = true)
public final class ContainerRegistryAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryAsyncClient.class);
    private final ContainerRegistriesImpl registriesImplClient;
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final String apiVersion;

    ContainerRegistryAsyncClient(HttpPipeline httpPipeline, String endpoint, String version) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.registriesImplClient
            = new AzureContainerRegistryImpl(httpPipeline, endpoint, version).getContainerRegistries();
        this.apiVersion = version;
    }

    /**
     *  Gets the service endpoint.
     *
     * @return The service endpoint for the Azure Container Registry instance.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * List all the repository names in this registry.
     *
     * <p><strong>List repository names in the registry</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.listRepositoryNames -->
     * <pre>
     * client.listRepositoryNames&#40;&#41;.subscribe&#40;name -&gt; &#123;
     *     System.out.printf&#40;&quot;Repository Name:%s,&quot;, name&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.listRepositoryNames -->
     *
     * @return list of repository names.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to perform this operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRepositoryNames() {
        return new PagedFlux<>(
            (pageSize) -> withContext(context -> listRepositoryNamesSinglePageAsync(pageSize, context)),
            (token, pageSize) -> withContext(context -> listRepositoryNamesNextSinglePageAsync(token, context)));
    }

    private Mono<PagedResponse<String>> listRepositoryNamesSinglePageAsync(Integer pageSize, Context context) {
        if (pageSize != null && pageSize < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        return this.registriesImplClient.getRepositoriesSinglePageAsync(null, pageSize, context)
            .map(UtilsImpl::getPagedResponseWithContinuationToken)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private Mono<PagedResponse<String>> listRepositoryNamesNextSinglePageAsync(String nextLink, Context context) {
        return this.registriesImplClient.getRepositoriesNextSinglePageAsync(nextLink, context)
            .map(UtilsImpl::getPagedResponseWithContinuationToken)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Delete the repository with provided {@code repositoryName}.
     *
     * <p><strong>Delete a repository in the registry</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepositoryWithResponse#String -->
     * <pre>
     * client.deleteRepositoryWithResponse&#40;repositoryName&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Successfully initiated delete of the repository.&quot;&#41;;
     * &#125;, error -&gt; &#123;
     *     System.out.println&#40;&quot;Failed to initiate a delete of the repository.&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepositoryWithResponse#String -->
     *
     * @param repositoryName Name of the repository.
     * @return the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to delete the repository.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if the {@code repositoryName} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRepositoryWithResponse(String repositoryName) {
        return withContext(context -> deleteRepositoryWithResponse(repositoryName, context));
    }

    private Mono<Response<Void>> deleteRepositoryWithResponse(String repositoryName, Context context) {
        if (repositoryName == null) {
            return monoError(LOGGER, new NullPointerException("'repositoryName' cannot be null."));
        }

        if (repositoryName.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'repositoryName' cannot be empty."));
        }

        return this.registriesImplClient.deleteRepositoryWithResponseAsync(repositoryName, context)
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Delete the repository with provided {@code repositoryName}.
     *
     * <p><strong>Delete a repository in the registry</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepository#String -->
     * <pre>
     * client.deleteRepository&#40;repositoryName&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Successfully initiated delete of the repository.&quot;&#41;;
     * &#125;, error -&gt; &#123;
     *     System.out.println&#40;&quot;Failed to initiate a delete of the repository.&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.deleteRepository#String -->
     *
     * @param repositoryName Name of the repository.
     * @return the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to delete the repository.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if {@code repositoryName} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRepository(String repositoryName) {
        return withContext(context -> this.deleteRepository(repositoryName, context));
    }

    private Mono<Void> deleteRepository(String repositoryName, Context context) {
        return this.deleteRepositoryWithResponse(repositoryName, context).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new instance of {@link ContainerRepositoryAsync} object for the specified repository.
     *
     * <p><strong>Get an instance of {@link ContainerRepositoryAsync}</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.containeregistryasyncclient.getRepository -->
     * <pre>
     * ContainerRepositoryAsync repositoryAsync = client.getRepository&#40;repositoryName&#41;;
     * repositoryAsync.getProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.println&#40;properties.getName&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.containeregistryasyncclient.getRepository -->
     *
     * @param repositoryName Name of the repository to reference.
     * @return A new {@link ContainerRepositoryAsync} for the requested repository.
     * @throws NullPointerException if {@code repositoryName} is null.
     * @throws IllegalArgumentException if {@code repositoryName} is empty.
     */
    public ContainerRepositoryAsync getRepository(String repositoryName) {
        return new ContainerRepositoryAsync(repositoryName, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Creates a new instance of {@link RegistryArtifactAsync} object for the specified artifact.
     *
     * <p><strong>Get an instance of {@link RegistryArtifactAsync}</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact -->
     * <pre>
     * RegistryArtifactAsync registryArtifactAsync = client.getArtifact&#40;repositoryName, tagOrDigest&#41;;
     * registryArtifactAsync.getManifestProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.println&#40;properties.getDigest&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact -->
     *
     * @param repositoryName Name of the repository containing the artifact.
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifactAsync RegistryArtifactAsync} for the requested artifact.
     * @throws NullPointerException if {@code repositoryName} or {@code tagOrDigest} are null.
     * @throws IllegalArgumentException if {@code repositoryName} or {@code tagOrDigest} are empty.
     */
    public RegistryArtifactAsync getArtifact(String repositoryName, String tagOrDigest) {
        return new RegistryArtifactAsync(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }
}
