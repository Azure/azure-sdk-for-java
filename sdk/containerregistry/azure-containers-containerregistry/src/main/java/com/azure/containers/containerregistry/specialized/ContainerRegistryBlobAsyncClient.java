// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.specialized;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsGetChunkHeaders;
import com.azure.containers.containerregistry.models.DownloadBlobAsyncResult;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
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
import java.util.ArrayList;
import java.util.List;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.SUPPORTED_MANIFEST_TYPES;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getBlobSize;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.toDownloadManifestResponse;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.trimNextLink;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateResponseHeaderDigest;
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
    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;

    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryBlobAsyncClient.class);

    ContainerRegistryBlobAsyncClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version) {
        this.repositoryName = repositoryName;
        this.endpoint = endpoint;
        AzureContainerRegistryImpl registryImplClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version);
        this.blobsImpl = registryImplClient.getContainerRegistryBlobs();
        this.registriesImpl = registryImplClient.getContainerRegistries();
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
            return monoError(LOGGER, new NullPointerException("'manifest' can't be null."));
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
            return monoError(LOGGER, new NullPointerException("'options' can't be null."));
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
            return monoError(LOGGER, new NullPointerException("'options' can't be null."));
        }

        return withContext(context -> this.uploadManifestWithResponse(options, context));
    }

    private Mono<Response<UploadManifestResult>> uploadManifestWithResponse(UploadManifestOptions options, Context context) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' can't be null."));
        }

        ByteBuffer data = options.getManifest().toByteBuffer();
        String tagOrDigest = options.getTag() != null ? options.getTag() : computeDigest(data);
        return this.registriesImpl.createManifestWithResponseAsync(
            repositoryName,
            tagOrDigest,
            Flux.just(data),
            data.remaining(),
            ManifestMediaType.OCI_MANIFEST.toString(),
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
            return monoError(LOGGER, new NullPointerException("'data' can't be null."));
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

    private Mono<Response<UploadBlobResult>> uploadBlobWithResponse(ByteBuffer data, Context context) {
        if (data == null) {
            return monoError(LOGGER, new NullPointerException("'data' can't be null."));
        }

        String digest = computeDigest(data);
        return this.blobsImpl.startUploadWithResponseAsync(repositoryName, context)
            .flatMap(startUploadResponse -> this.blobsImpl.uploadChunkWithResponseAsync(trimNextLink(startUploadResponse.getDeserializedHeaders().getLocation()), Flux.just(data), data.remaining(), context))
            .flatMap(uploadChunkResponse -> this.blobsImpl.completeUploadWithResponseAsync(digest, trimNextLink(uploadChunkResponse.getDeserializedHeaders().getLocation()), (Flux<ByteBuffer>) null, 0L, context))
            .flatMap(completeUploadResponse -> {
                Response<UploadBlobResult> res = new ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, UploadBlobResult>(completeUploadResponse.getRequest(),
                    completeUploadResponse.getStatusCode(),
                    completeUploadResponse.getHeaders(),
                    new UploadBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest()),
                    completeUploadResponse.getDeserializedHeaders());

                return Mono.just(res);
            }).onErrorMap(UtilsImpl::mapException);
    }
    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @return The manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DownloadManifestResult> downloadManifest(String tagOrDigest) {
        return withContext(context -> this.downloadManifestWithResponse(tagOrDigest, SUPPORTED_MANIFEST_TYPES, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @param mediaType Manifest media type to request (or a comma separated list of media types).
     * @return The response for the manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DownloadManifestResult>> downloadManifestWithResponse(String tagOrDigest, ManifestMediaType mediaType) {
        return withContext(context -> this.downloadManifestWithResponse(tagOrDigest, mediaType, context));
    }

    private Mono<Response<DownloadManifestResult>> downloadManifestWithResponse(String tagOrDigest, ManifestMediaType mediaType, Context context) {
        if (tagOrDigest == null) {
            return monoError(LOGGER, new NullPointerException("'tagOrDigest' can't be null."));
        }

        return registriesImpl.getManifestWithResponseAsync(repositoryName, tagOrDigest, mediaType.toString(), context)
            .map(response -> toDownloadManifestResponse(tagOrDigest, response))
            .onErrorMap(UtilsImpl::mapException);
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
    public Mono<DownloadBlobAsyncResult> downloadStream(String digest) {
        return withContext(context -> downloadBlobInternal(digest, context));
    }

    private Mono<DownloadBlobAsyncResult> downloadBlobInternal(String digest, Context context) {
        if (digest == null) {
            return monoError(LOGGER, new NullPointerException("'digest' can't be null."));
        }

        Flux<ByteBuffer> content =
            blobsImpl.getChunkWithResponseAsync(repositoryName, digest, new HttpRange(0, (long) CHUNK_SIZE).toString(), context)
                .flatMapMany(firstResponse -> getAllChunks(firstResponse, digest, context))
                .flatMapSequential(chunk -> chunk.getValue().toFluxByteBuffer(), 1);
        return Mono.just(ConstructorAccessors.createBlobDownloadResult(digest, content));
    }

    private Flux<ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData>> getAllChunks(
        ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData> firstResponse, String digest, Context context) {
        validateResponseHeaderDigest(digest, firstResponse.getHeaders());

        long blobSize = getBlobSize(firstResponse.getHeaders().get(HttpHeaderName.CONTENT_RANGE));
        List<Mono<ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData>>> others = new ArrayList<>();
        others.add(Mono.just(firstResponse));
        for (long p = firstResponse.getValue().getLength(); p < blobSize; p += CHUNK_SIZE) {
            HttpRange range = new HttpRange(p, (long) CHUNK_SIZE);
            others.add(blobsImpl.getChunkWithResponseAsync(repositoryName, digest, range.toString(), context));
        }

        return Flux.concat(others);
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

    private Mono<Response<Void>> deleteBlobWithResponse(String digest, Context context) {
        if (digest == null) {
            return monoError(LOGGER, new NullPointerException("'digest' can't be null."));
        }

        return this.blobsImpl.deleteBlobWithResponseAsync(repositoryName, digest, context)
            .flatMap(response -> Mono.just(UtilsImpl.deleteResponseToSuccess(response)))
            .onErrorResume(
                ex -> ex instanceof HttpResponseException && ((HttpResponseException) ex).getResponse().getStatusCode() == 404,
                ex -> {
                    HttpResponse response = ((HttpResponseException) ex).getResponse();
                    // In case of 404, we still convert it to success i.e. no-op.
                    return Mono.just(new SimpleResponse<Void>(response.getRequest(), 202,
                        response.getHeaders(), null));
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

    private Mono<Response<Void>> deleteManifestWithResponse(String digest, Context context) {
        return this.registriesImpl.deleteManifestWithResponseAsync(repositoryName, digest, context)
            .flatMap(response -> Mono.just(UtilsImpl.deleteResponseToSuccess(response)))
            .onErrorMap(UtilsImpl::mapException);
    }
}
