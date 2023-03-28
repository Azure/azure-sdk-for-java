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
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;

/**
 * This class provides a client that exposes operations to managing container images and artifacts.
 * synchronously. It exposes methods directly performed on the registry like listing the catalog.
 * as well as helper types like {@link #getArtifact(String, String) getArtifact} and {@link #getRepository(String) getRepository}
 * that can be used to perform operations on repository and artifacts.
 *
 * <p><strong>Instantiating a synchronous Container Registry client</strong></p>
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.instantiation -->
 * <pre>
 * ContainerRegistryClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Container Registry client with custom pipeline</strong></p>
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;&#47;* add policies *&#47;&#41;
 *     .build&#40;&#41;;
 *
 * ContainerRegistryClient registryAsyncClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .pipeline&#40;pipeline&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation -->
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class ContainerRegistryClient {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryClient.class);
    private final ContainerRegistriesImpl registriesImplClient;
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final String apiVersion;

    ContainerRegistryClient(HttpPipeline httpPipeline, String endpoint, String version) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.registriesImplClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version)
            .getContainerRegistries();
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
     * <p><strong>List the repository names in the registry.</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames -->
     * <pre>
     * client.listRepositoryNames&#40;&#41;.stream&#40;&#41;.forEach&#40;name -&gt; &#123;
     *     System.out.printf&#40;&quot;Repository Name:%s,&quot;, name&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames -->
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
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames#Context -->
     * <pre>
     * client.listRepositoryNames&#40;Context.NONE&#41;.stream&#40;&#41;.forEach&#40;name -&gt; &#123;
     *     System.out.printf&#40;&quot;Repository Name:%s,&quot;, name&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.listRepositoryNames#Context -->
     *
     * @param context The context to associate with this operation.
     * @return list of repositories.
     * @throws ClientAuthenticationException thrown if the client credentials do not have access to perform this operation.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRepositoryNames(Context context) {
        return new PagedIterable<>(
            (pageSize) -> listRepositoryNamesSinglePageSync(pageSize, context),
            (token, pageSize) -> listRepositoryNamesNextSinglePageSync(token, context));
    }

    private PagedResponse<String> listRepositoryNamesSinglePageSync(Integer pageSize, Context context) {
        if (pageSize != null && pageSize < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'pageSize' cannot be negative."));
        }

        try {
            return this.registriesImplClient.getRepositoriesSinglePage(null, pageSize, context);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private PagedResponse<String> listRepositoryNamesNextSinglePageSync(String nextLink, Context context) {
        try {
            return this.registriesImplClient.getRepositoriesNextSinglePage(nextLink, context);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }


    /**
     * Delete the repository identified by {@code repositoryName}.
     *
     * <p><strong>Delete a repository in the registry.</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String -->
     * <pre>
     * client.deleteRepository&#40;repositoryName&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepository#String -->
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
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepositoryWithResponse#String-Context -->
     * <pre>
     * client.deleteRepositoryWithResponse&#40;repositoryName, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.deleteRepositoryWithResponse#String-Context -->
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
        Objects.requireNonNull(repositoryName, "'repositoryName' cannot be null");

        if (repositoryName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'repositoryName' cannot be empty."));
        }

        try {
            return UtilsImpl.deleteResponseToSuccess(
                this.registriesImplClient.deleteRepositoryWithResponse(repositoryName, context));
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Creates a new instance of {@link ContainerRepository} object for the specified repository.
     *
     *  <p><strong>Create a ContainerRegistry helper instance.</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.getRepository -->
     * <pre>
     * ContainerRepository repository = client.getRepository&#40;repositoryName&#41;;
     * ContainerRepositoryProperties properties = repository.getProperties&#40;&#41;;
     * System.out.println&#40;properties.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.getRepository -->
     *
     * @param repositoryName Name of the repository to reference.
     * @return A new {@link ContainerRepository} for the desired repository.
     * @throws NullPointerException if {@code repositoryName} is null.
     * @throws IllegalArgumentException if {@code repositoryName} is empty.
     */
    public ContainerRepository getRepository(String repositoryName) {
        return new ContainerRepository(repositoryName, httpPipeline, endpoint, apiVersion);
    }

    /**
     * Creates a new instance of {@link RegistryArtifact} object for the specified artifact.
     *
     *  <p><strong>Create a RegistryArtifact helper instance.</strong></p>
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact -->
     * <pre>
     * RegistryArtifact registryArtifact = client.getArtifact&#40;repositoryName, tagOrDigest&#41;;
     * ArtifactManifestProperties properties = registryArtifact.getManifestProperties&#40;&#41;;
     * System.out.println&#40;properties.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRegistryClient.getArtifact -->
     *
     * @param repositoryName Name of the repository to reference.
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifact} object for the desired repository.
     * @throws NullPointerException if {@code repositoryName} or {@code tagOrDigest} is null.
     * @throws IllegalArgumentException if {@code repositoryName} or {@code tagOrDigest} is empty.
     */
    public RegistryArtifact getArtifact(String repositoryName, String tagOrDigest) {
        return new RegistryArtifact(repositoryName, tagOrDigest, httpPipeline, endpoint, apiVersion);
    }
}
