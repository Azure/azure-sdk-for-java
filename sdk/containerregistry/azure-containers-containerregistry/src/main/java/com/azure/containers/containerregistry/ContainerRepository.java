// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactManifestOrder;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * This class provides a helper type that contains all the operations for repositories in Azure Container Registry.
 * Operations allowed by this type are listing, retrieving, deleting, setting writeable properties. These operations are
 * supported on the repository and the respective tags and manifests in it.
 *
 * <p><strong>Instantiating Container Repository helper type.</strong></p>
 *
 * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.instantiation -->
 * <pre>
 * ContainerRepository repositoryClient = new ContainerRegistryClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;
 *     .audience&#40;ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD&#41;
 *     .buildClient&#40;&#41;.getRepository&#40;repository&#41;;
 * </pre>
 * <!-- end com.azure.containers.containerregistry.ContainerRepository.instantiation -->
 *
 * <p>View {@link ContainerRegistryClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryClientBuilder
 */
@ServiceClient(builder = ContainerRegistryClientBuilder.class)
public final class ContainerRepository {
    private final ContainerRepositoryAsync asyncClient;


    /**
     * Creates a {@link ContainerRepositoryAsync} that sends requests to the given repository in the container registry
     * service at {@code endpoint}. Each service call goes through the {@code pipeline}.
     *
     * @param asyncClient The async client for the given repository.
     */
    ContainerRepository(ContainerRepositoryAsync asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the Azure Container Registry service endpoint for the current instance.
     *
     * @return The service endpoint for the current instance.
     */
    public String getName() {
        return this.asyncClient.getName();
    }

    /**
     * Gets the Azure Container Registry name for the current instance.
     *
     * @return Return the registry name.
     */
    public String getRegistryEndpoint() {
        return this.asyncClient.getRegistryEndpoint();
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.deleteRepositoryWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = client.deleteWithResponse&#40;Context.NONE&#41;;
     * System.out.printf&#40;&quot;Successfully initiated delete.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.deleteRepositoryWithResponse -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call. artifacts
     * that are deleted as part of the repository delete.
     * @return A void response for completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Context context) {
        return this.asyncClient.deleteWithResponse(context).block();
    }

    /**
     * Delete the repository in the Azure Container Registry for the given {@link #getName() repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.deleteRepository -->
     * <pre>
     * client.delete&#40;&#41;;
     * System.out.printf&#40;&quot;Successfully initiated delete.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.deleteRepository -->
     *
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        this.deleteWithResponse(Context.NONE);
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.getPropertiesWithResponse -->
     * <pre>
     * Response&lt;ContainerRepositoryProperties&gt; response = client.getPropertiesWithResponse&#40;Context.NONE&#41;;
     * final ContainerRepositoryProperties properties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Name:%s,&quot;, properties.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.getPropertiesWithResponse -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the {@link ContainerRepositoryProperties properties} associated with the given
     * {@link #getName() repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ContainerRepositoryProperties> getPropertiesWithResponse(Context context) {
        return this.asyncClient.getPropertiesWithResponse(context).block();
    }

    /**
     * Gets the {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.getProperties -->
     * <pre>
     * ContainerRepositoryProperties properties = client.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Name:%s,&quot;, properties.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.getProperties -->
     *
     * @return The {@link ContainerRepositoryProperties properties} associated with the given {@link #getName()
     * repository}.
     * @throws ClientAuthenticationException thrown if the client does not have access to modify the namespace.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ContainerRepositoryProperties getProperties() {
        return this.getPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Creates a new instance of {@link RegistryArtifact} object for the specified artifact.
     *
     * @param tagOrDigest Either a tag or digest that uniquely identifies the artifact.
     * @return A new {@link RegistryArtifact} object for the desired repository.
     * @throws NullPointerException if {@code tagOrDigest} is null.
     * @throws IllegalArgumentException if {@code tagOrDigest} is empty.
     */
    public RegistryArtifact getArtifact(String tagOrDigest) {
        return new RegistryArtifact(this.asyncClient.getArtifact(tagOrDigest));
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository}.
     *
     * <p> If you would like to specify the order in which the tags are returned please
     * use the overload that takes in the options parameter {@link #listManifestProperties(ArtifactManifestOrder,
     * Context)}   listManifestProperties} No assumptions on the order can be made if no options are provided to the
     * service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestProperties -->
     * <pre>
     * client.listManifestProperties&#40;&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestProperties -->
     *
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties() {
        return this.listManifestProperties(ArtifactManifestOrder.NONE, Context.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository }.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order for the last updated time for the artifacts. No
     * assumptions on the order can be made if no options are provided by the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptionsNoContext -->
     * <pre>
     * client.listManifestProperties&#40;ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptionsNoContext -->
     *
     * @param order the order in which the artifacts are returned by the service.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties(ArtifactManifestOrder order) {
        return this.listManifestProperties(order, Context.NONE);
    }

    /**
     * Fetches all the artifacts associated with the given {@link #getName() repository }.
     *
     * <p> The method supports options to select the order in which the artifacts are returned by the service.
     * Currently the service supports an ascending or descending order for the last updated time for the artifacts. No
     * assumptions on the order can be made if no options are provided by the service.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all artifacts associated with the given repository from the most recently updated to the last.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptions -->
     * <pre>
     * client.listManifestProperties&#40;ArtifactManifestOrder.LAST_UPDATED_ON_DESCENDING, Context.NONE&#41;.iterableByPage&#40;10&#41;
     *     .forEach&#40;pagedResponse -&gt; &#123;
     *         pagedResponse.getValue&#40;&#41;.stream&#40;&#41;.forEach&#40;
     *             ManifestProperties -&gt; System.out.println&#40;ManifestProperties.getDigest&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.listManifestPropertiesWithOptions -->
     *
     * @param order the order in which the artifacts are returned by the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of the artifacts for the given repository in the order specified by the options.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the
     * namespace.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ArtifactManifestProperties> listManifestProperties(ArtifactManifestOrder order, Context context) {
        return new PagedIterable<>(this.asyncClient.listManifestProperties(order, context));
    }

    /**
     * Update the settable properties {@link ContainerRepositoryProperties} of the given {@link #getName() repository}.
     * These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.updatePropertiesWithResponse -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updatePropertiesWithResponse&#40;properties, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.updatePropertiesWithResponse -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties repository properties} that need to be updated
     * for the repository.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response with the completion.
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ContainerRepositoryProperties> updatePropertiesWithResponse(ContainerRepositoryProperties repositoryProperties, Context context) {
        return this.asyncClient.updatePropertiesWithResponse(repositoryProperties, context).block();
    }

    /**
     * Update the repository properties {@link ContainerRepositoryProperties} of the given {@link #getName()
     * repository}. These properties set the update, delete and retrieve options of the repository.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the writeable properties for the given repository.</p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.ContainerRepository.updateProperties -->
     * <pre>
     * ContainerRepositoryProperties properties = getRepositoryProperties&#40;&#41;;
     * client.updateProperties&#40;properties&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.ContainerRepository.updateProperties -->
     *
     * @param repositoryProperties {@link ContainerRepositoryProperties repository properties} that need to be updated
     * for the repository.
     * @return The updated {@link ContainerRepositoryProperties properties }
     * @throws ClientAuthenticationException thrown if the client does not have access to the repository.
     * @throws ResourceNotFoundException thrown if the repository with the given name was not found.
     * @throws HttpResponseException thrown if any other unexpected exception is returned by the service.
     * @throws NullPointerException thrown if the {@code repositoryProperties} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ContainerRepositoryProperties updateProperties(ContainerRepositoryProperties repositoryProperties) {
        return this.updatePropertiesWithResponse(repositoryProperties, Context.NONE).getValue();
    }
}
