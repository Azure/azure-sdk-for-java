// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.specialized;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImplBuilder;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ManifestWrapper;
import com.azure.containers.containerregistry.models.DownloadBlobResult;
import com.azure.containers.containerregistry.models.DownloadManifestOptions;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.deleteResponseToSuccess;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that exposes operations to push and pull images into container registry.
 * It exposes methods that upload, download and delete artifacts from the registry i.e. images and manifests.
 *
 * <p>View {@link ContainerRegistryBlobClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryBlobClientBuilder
 */
@ServiceClient(builder = ContainerRegistryBlobClientBuilder.class, isAsync = true)
public class ContainerRegistryBlobAsyncClient {

    private final AzureContainerRegistryImpl registryImplClient;
    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;

    private final ClientLogger logger = new ClientLogger(ContainerRegistryBlobAsyncClient.class);

    ContainerRegistryBlobAsyncClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version) {
        this.repositoryName = repositoryName;
        this.endpoint = endpoint;
        this.registryImplClient = new AzureContainerRegistryImplBuilder()
            .url(endpoint)
            .pipeline(httpPipeline)
            .apiVersion(version)
            .buildClient();
        this.blobsImpl = this.registryImplClient.getContainerRegistryBlobs();
        this.registriesImpl = this.registryImplClient.getContainerRegistries();
    }

    /**
     * This method returns the registry's repository on which operations are being performed.
     *
     * @return The name of the repository
     */
    public String getRepositoryName() {
        return this.repositoryName;
    }

    /**
     * This method returns the complete registry endpoint.
     *
     * @return The registry endpoint including the authority.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Upload the Oci manifest to the repository.
     * The upload is done as a single operation.
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     * @param manifest The OciManifest that needs to be uploaded.
     * @return operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadManifestResult> uploadManifest(OciManifest manifest) {
        if (manifest == null) {
            return monoError(logger, new NullPointerException("'manifest' can't be null."));
        }

        return withContext(context -> this.uploadManifestWithResponse(new UploadManifestOptions(manifest), context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     * @param options The options for the upload manifest operation.
     * @return operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadManifestResult> uploadManifest(UploadManifestOptions options) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' can't be null."));
        }

        return withContext(context -> this.uploadManifestWithResponse(options, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the upload manifest operation.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UploadManifestResult>> uploadManifestWithResponse(UploadManifestOptions options) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' can't be null."));
        }

        return withContext(context -> this.uploadManifestWithResponse(options, context));
    }

    Mono<Response<UploadManifestResult>> uploadManifestWithResponse(UploadManifestOptions options, Context context) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' can't be null."));
        }

        ByteBuffer data = options.getManifest().toByteBuffer();
        String tagOrDigest = options.getTag() != null ? options.getTag() : UtilsImpl.computeDigest(data);
        return this.registriesImpl.createManifestWithResponseAsync(
            repositoryName,
            tagOrDigest,
            Flux.just(data),
            data.remaining(),
            UtilsImpl.OCI_MANIFEST_MEDIA_TYPE,
            context).map(response -> {
                Response<UploadManifestResult> res = new ResponseBase<ContainerRegistriesCreateManifestHeaders, UploadManifestResult>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    new UploadManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                    response.getDeserializedHeaders());
                return res;
            }).onErrorMap(UtilsImpl::mapException);
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * The blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     *
     * @param data The blob\image content that needs to be uploaded.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadBlobResult> uploadBlob(BinaryData data) {
        if (data == null) {
            return monoError(logger, new NullPointerException("'data' can't be null."));
        }

        return withContext(context -> this.uploadBlobWithResponse(data.toByteBuffer(), context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * The blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     *
     * @param data The blob\image content that needs to be uploaded.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UploadBlobResult>> uploadBlobWithResponse(BinaryData data) {
        return withContext(context -> this.uploadBlobWithResponse(data.toByteBuffer(), context));
    }

    Mono<Response<UploadBlobResult>> uploadBlobWithResponse(ByteBuffer data, Context context) {
        if (data == null) {
            return monoError(logger, new NullPointerException("'data' can't be null."));
        }

        String digest = UtilsImpl.computeDigest(data);
        return this.blobsImpl.startUploadWithResponseAsync(repositoryName, context)
            .flatMap(startUploadResponse -> this.blobsImpl.uploadChunkWithResponseAsync(trimNextLink(startUploadResponse.getDeserializedHeaders().getLocation()), Flux.just(data), data.remaining(), context))
            .flatMap(uploadChunkResponse -> this.blobsImpl.completeUploadWithResponseAsync(digest, trimNextLink(uploadChunkResponse.getDeserializedHeaders().getLocation()), null, 0L, context))
            .flatMap(completeUploadResponse -> {
                Response<UploadBlobResult> res = new ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, UploadBlobResult>(completeUploadResponse.getRequest(),
                    completeUploadResponse.getStatusCode(),
                    completeUploadResponse.getHeaders(),
                    new UploadBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest()),
                    completeUploadResponse.getDeserializedHeaders());

                return Mono.just(res);
            }).onErrorMap(UtilsImpl::mapException);
    }

    private String trimNextLink(String locationHeader) {
        // The location header returned in the nextLink for upload chunk operations starts with a '/'
        // which the service expects us to remove before calling it.
        if (locationHeader.startsWith("/")) {
            return locationHeader.substring(1);
        }

        return locationHeader;
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options Options for the operation.
     * @return The manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DownloadManifestResult> downloadManifest(DownloadManifestOptions options) {
        return this.downloadManifestWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the operation.
     * @return The response for the manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DownloadManifestResult>> downloadManifestWithResponse(DownloadManifestOptions options) {
        return withContext(context -> this.downloadManifestWithResponse(options, context));
    }

    Mono<Response<DownloadManifestResult>> downloadManifestWithResponse(DownloadManifestOptions options, Context context) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' can't be null."));
        }

        String tagOrDigest = options.getTag() != null ? options.getTag() : options.getDigest();

        return this.registriesImpl.getManifestWithResponseAsync(repositoryName, tagOrDigest, UtilsImpl.OCI_MANIFEST_MEDIA_TYPE, context)
            .flatMap(response -> {
                String digest = UtilsImpl.getDigestFromHeader(response.getHeaders());
                ManifestWrapper wrapper = response.getValue();

                // The service wants us to validate the digest here since a lot of customers forget to do it before consuming
                // the contents returned by the service.
                if (Objects.equals(digest, tagOrDigest) || Objects.equals(response.getValue().getTag(), tagOrDigest)) {
                    OciManifest ociManifest = new OciManifest()
                        .setAnnotations(wrapper.getAnnotations())
                        .setConfig(wrapper.getConfig())
                        .setLayers(wrapper.getLayers())
                        .setSchemaVersion(wrapper.getSchemaVersion());

                    Response<DownloadManifestResult> res = new SimpleResponse<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        new DownloadManifestResult(digest, ociManifest, BinaryData.fromObject(ociManifest)));

                    return Mono.just(res);
                } else {
                    return monoError(logger, new ServiceResponseException("The digest in the response does not match the expected digest."));
                }
            }).onErrorMap(UtilsImpl::mapException);
    }

    /**
     * Download the blob associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @return The image associated with the given digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DownloadBlobResult> downloadBlob(String digest) {
        return this.downloadBlobWithResponse(digest).flatMap(FluxUtil::toMono);
    }

    /**
     * Download the blob\layer associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @return The image associated with the given digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DownloadBlobResult>> downloadBlobWithResponse(String digest) {
        return withContext(context -> this.downloadBlobWithResponse(digest, context));
    }

    Mono<Response<DownloadBlobResult>> downloadBlobWithResponse(String digest, Context context) {
        if (digest == null) {
            return monoError(logger, new NullPointerException("'digest' can't be null."));
        }

        return this.blobsImpl.getBlobWithResponseAsync(repositoryName, digest, context).flatMap(streamResponse -> {
            String resDigest = UtilsImpl.getDigestFromHeader(streamResponse.getHeaders());

            return BinaryData.fromFlux(streamResponse.getValue())
                .flatMap(binaryData -> {
                    // The service wants us to validate the digest here since a lot of customers forget to do it before consuming
                    // the contents returned by the service.
                    if (Objects.equals(resDigest, digest)) {
                        Response<DownloadBlobResult> response = new SimpleResponse<>(
                            streamResponse.getRequest(),
                            streamResponse.getStatusCode(),
                            streamResponse.getHeaders(),
                            new DownloadBlobResult(resDigest, binaryData));

                        return Mono.just(response);
                    } else {
                        return monoError(logger, new ServiceResponseException("The digest in the response does not match the expected digest."));
                    }
                }).doFinally(ignored -> {
                    try {
                        streamResponse.close();
                    } catch (Exception e) {
                        logger.logThrowableAsError(e);
                    }
                });
        }).onErrorMap(UtilsImpl::mapException);
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @return The completion signal.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteBlob(String digest) {
        return this.deleteBlobWithResponse(digest).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @return The REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteBlobWithResponse(String digest) {
        return withContext(context -> deleteBlobWithResponse(digest, context));
    }

    Mono<Response<Void>> deleteBlobWithResponse(String digest, Context context) {
        if (digest == null) {
            return monoError(logger, new NullPointerException("'digest' can't be null."));
        }

        return this.blobsImpl.deleteBlobWithResponseAsync(repositoryName, digest, context)
            .flatMap(streamResponse -> {
                Mono<Response<Void>> res = deleteResponseToSuccess(streamResponse);
                // Since we are not passing the streamResponse back to the user, we need to close this.
                streamResponse.close();
                return res;
            })
            .onErrorMap(UtilsImpl::mapException);
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @return The completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteManifest(String digest) {
        return this.deleteManifestWithResponse(digest).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @return The REST response for completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteManifestWithResponse(String digest) {
        return withContext(context -> deleteManifestWithResponse(digest, context));
    }

    Mono<Response<Void>> deleteManifestWithResponse(String digest, Context context) {
        return this.registriesImpl.deleteManifestWithResponseAsync(repositoryName, digest, context)
            .flatMap(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(UtilsImpl::mapException);
    }
}
