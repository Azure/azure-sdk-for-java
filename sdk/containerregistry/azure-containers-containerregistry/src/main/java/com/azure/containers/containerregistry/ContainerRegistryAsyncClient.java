// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImplBuilder;
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

import static com.azure.containers.containerregistry.Utils.CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides a client that exposes operations to managing container images and artifacts.
 * It exposes methods directly performed on the registry like listing the catalog.
 * as well as helper types like {@link #getArtifact(String, String) getArtifact} and {@link #getRepository(String) getRepository}
 * that can be used to perform operations on repository and artifacts directly.
 *
 * <p><strong>Instantiating an asynchronous Container Registry client</strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation -->
 * <pre>
 * ContainerRegistryAsyncClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .audience&#40;ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation -->
 *
 * <p><strong>Instantiating an asynchronous Container Registry client using a custom pipeline</strong></p>
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryAsyncClient.pipeline.instantiation -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;&#47;* add policies *&#47;&#41;
 *     .build&#40;&#41;;
 *
 * ContainerRegistryAsyncClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .pipeline&#40;pipeline&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .audience&#40;ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD&#41;
 *     .credential&#40;credential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryAsyncClient.pipeline.instantiation -->
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class, isAsync = true)
public final class ContainerRegistryAsyncClient {
    private final AzureContainerRegistryImpl registryImplClient;
    private final ContainerRegistriesImpl registriesImplClient;
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final String apiVersion;

    private final ClientLogger logger = new ClientLogger(ContainerRegistryAsyncClient.class);

    ContainerRegistryAsyncClient(HttpPipeline httpPipeline, String endpoint, String version) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.registryImplClient = new AzureContainerRegistryImplBuilder()
            .url(endpoint)
            .pipeline(httpPipeline)
            .apiVersion(version)
            .buildClient();
        this.registriesImplClient = this.registryImplClient.getContainerRegistries();
        this.apiVersion = version;
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
     * <p><strong>List repository names in the registry.</strong></p>
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

            Mono<PagedResponse<String>> pagedResponseMono = this.registriesImplClient.getRepositoriesSinglePageAsync(null, pageSize, context.addData(AZ_TRACING_NAMESPACE_KEY, CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE))
                .map(res -> Utils.getPagedResponseWithContinuationToken(res))
                .onErrorMap(Utils::mapException);
            return pagedResponseMono;

        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<PagedResponse<String>> listRepositoryNamesNextSinglePageAsync(String nextLink, Context context) {
        try {
            Mono<PagedResponse<String>> pagedResponseMono = this.registriesImplClient.getRepositoriesNextSinglePageAsync(nextLink, context.addData(AZ_TRACING_NAMESPACE_KEY, CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE));
            return pagedResponseMono.map(res -> Utils.getPagedResponseWithContinuationToken(res));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Delete the repository identified by 'repositoryName'.
     *
     * <p><strong>Delete a repository in the registry.</strong></p>
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
     * @param repositoryName Name of the repository (including the namespace).
     * @return the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if the {@code repositoryName} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRepositoryWithResponse(String repositoryName) {
        return withContext(context -> deleteRepositoryWithResponse(repositoryName, context));
    }

    Mono<Response<Void>> deleteRepositoryWithResponse(String repositoryName, Context context) {
        try {
            if (repositoryName == null) {
                return monoError(logger, new NullPointerException("'repositoryName' cannot be null."));
            }

            if (repositoryName.isEmpty()) {
                return monoError(logger, new IllegalArgumentException("'repositoryName' cannot be empty."));
            }

            return this.registriesImplClient.deleteRepositoryWithResponseAsync(repositoryName, context.addData(AZ_TRACING_NAMESPACE_KEY, CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE))
                .flatMap(Utils::deleteResponseToSuccess)
                .onErrorMap(Utils::mapException);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Delete the repository identified by {@code repositoryName}.
     *
     * <p><strong>Delete a repository in the registry.</strong></p>
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
     * @param repositoryName Name of the image (including the namespace).
     * @return the completion stream.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code repositoryName} is null.
     * @throws IllegalArgumentException thrown if {@code repositoryName} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRepository(String repositoryName) {
        return withContext(context -> this.deleteRepository(repositoryName, context));
    }

    Mono<Void> deleteRepository(String repositoryName, Context context) {
        return this.deleteRepositoryWithResponse(repositoryName, context).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new instance of {@link ContainerRepositoryAsync} object for the specified repository.
     *
     * <p><strong>Create an instance of ContainerRepositoryAsync helper type</strong></p>
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
     * @return A new {@link ContainerRepositoryAsync} for the desired repository.
     * @throws NullPointerException if {@code repositoryName} is null.
     * @throws IllegalArgumentException if {@code repositoryName} is empty.
     */
    public ContainerRepositoryAsync getRepository(String repositoryName) {
        return new ContainerRepositoryAsync(repositoryName, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Creates a new instance of {@link RegistryArtifactAsync} object for the specified artifact.
     *
     * <p><strong>Create an instance of RegistryArtifactAsync helper type</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact -->
     * <pre>
     * RegistryArtifactAsync registryArtifactAsync = client.getArtifact&#40;repositoryName, tagOrDigest&#41;;
     * registryArtifactAsync.getManifestProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.println&#40;properties.getDigest&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.containeregistryasyncclient.getArtifact -->
     *
     * @param repositoryName Name of the repository to reference.
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifactAsync RegistryArtifactAsync} for the desired repository.
     * @throws NullPointerException if {@code repositoryName} or {@code tagOrDigest} is null.
     * @throws IllegalArgumentException if {@code repositoryName} or {@code tagOrDigest} is empty.
     */
    public RegistryArtifactAsync getArtifact(String repositoryName, String tagOrDigest) {
        return new RegistryArtifactAsync(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }
}
