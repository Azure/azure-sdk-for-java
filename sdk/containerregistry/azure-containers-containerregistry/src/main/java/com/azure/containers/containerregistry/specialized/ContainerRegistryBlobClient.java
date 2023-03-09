// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.specialized;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsStartUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsUploadChunkHeaders;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOWNLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.UPLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.createSha256;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.deleteResponseToSuccess;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.enableSync;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getBlobSize;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getContentTypeString;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getLocation;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.toDownloadManifestResponse;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateResponseHeaderDigest;
import static com.azure.core.util.CoreUtils.bytesToHexString;

/**
 * This class provides a client that exposes operations to push and pull images into container registry.
 * It exposes methods that upload, download and delete artifacts from the registry i.e. images and manifests.
 *
 * <p>View {@link ContainerRegistryBlobClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryBlobClientBuilder
 */
@ServiceClient(builder = ContainerRegistryBlobClientBuilder.class)
public final class ContainerRegistryBlobClient {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryBlobClient.class);

    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;
    private final Tracer tracer;

    ContainerRegistryBlobClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version, Tracer tracer) {
        this.repositoryName = repositoryName;
        this.endpoint = endpoint;
        AzureContainerRegistryImpl registryImplClient = new AzureContainerRegistryImpl(httpPipeline, endpoint, version);
        this.blobsImpl = registryImplClient.getContainerRegistryBlobs();
        this.registriesImpl = registryImplClient.getContainerRegistries();
        this.tracer = tracer;
    }

    /**
     * This method returns the registry's repository on which operations are being performed.
     *
     * @return The name of the repository
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * This method returns the complete registry endpoint.
     *
     * @return The registry endpoint including the authority.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Upload the Oci manifest to the repository.
     * The upload is done as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param manifest The OciManifest that needs to be updated.
     * @return operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadManifestResult uploadManifest(OciImageManifest manifest) {
        Objects.requireNonNull(manifest, "'manifest' cannot be null.");
        return uploadManifestWithResponse(BinaryData.fromObject(manifest), null, ManifestMediaType.OCI_MANIFEST, Context.NONE).getValue();
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the upload manifest operation.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadManifestResult uploadManifest(UploadManifestOptions options) {
        return uploadManifestWithResponse(options.getManifest(), options.getTag(), options.getMediaType(), Context.NONE).getValue();
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the upload manifest operation.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UploadManifestResult> uploadManifestWithResponse(UploadManifestOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return uploadManifestWithResponse(options.getManifest(), options.getTag(), options.getMediaType(), context);
    }

    private Response<UploadManifestResult> uploadManifestWithResponse(BinaryData manifestData, String tagOrDigest, ManifestMediaType manifestMediaType, Context context) {
        BinaryData data = manifestData.toReplayableBinaryData();
        if (tagOrDigest == null) {
            tagOrDigest = computeDigest(data.toByteBuffer());
        }

        try {
            ResponseBase<ContainerRegistriesCreateManifestHeaders, Void> response = this.registriesImpl
                .createManifestWithResponse(repositoryName, tagOrDigest, data, data.getLength(),
                    manifestMediaType.toString(), enableSync(context));

            return new ResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                ConstructorAccessors.createUploadManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                response.getDeserializedHeaders());
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * Also, the blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     *
     * @param data The blob\image content that needs to be uploaded.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadBlobResult uploadBlob(BinaryData data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        InputStream stream = data.toStream();
        try {
            return uploadBlob(Channels.newChannel(stream), Context.NONE);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warning("Failed to close the stream", e);
            }
        }
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * Also, the blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     * The service does support this via range header.
     *
     * @param stream The blob\image content that needs to be uploaded.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadBlobResult uploadBlob(ReadableByteChannel stream, Context context) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");
        return runWithTracing(UPLOAD_BLOB_SPAN_NAME, (span) -> uploadBlobInternal(stream, span), enableSync(context));
    }

    private UploadBlobResult uploadBlobInternal(ReadableByteChannel stream, Context context) {
        MessageDigest sha256 = createSha256();
        byte[] buffer = new byte[CHUNK_SIZE];

        try {
            ResponseBase<ContainerRegistryBlobsStartUploadHeaders, Void> startUploadResponse =
                blobsImpl.startUploadWithResponse(repositoryName, context);
            String location = getLocation(startUploadResponse);

            BinaryData chunk;
            while (true) {
                chunk = readChunk(stream, sha256, buffer);
                if (chunk == null || chunk.getLength() < CHUNK_SIZE) {
                    break;
                }

                ResponseBase<ContainerRegistryBlobsUploadChunkHeaders, Void> uploadChunkResponse =
                    blobsImpl.uploadChunkWithResponse(location, chunk, chunk.getLength(), context);
                location = getLocation(uploadChunkResponse);
            }

            String digest = "sha256:" + bytesToHexString(sha256.digest());

            ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, Void> completeUploadResponse =
                blobsImpl.completeUploadWithResponse(digest, location, chunk, chunk == null ? null : chunk.getLength(), context);

            return ConstructorAccessors.createUploadBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest());
        } catch (AcrErrorsException ex) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(ex));
        }
    }
    private BinaryData readChunk(ReadableByteChannel stream, MessageDigest sha256, byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        while (byteBuffer.position() < CHUNK_SIZE) {
            try {
                if (stream.read(byteBuffer) < 0) {
                    break;
                }
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }
        }
        if (byteBuffer.position() == 0) {
            return null;
        }

        byteBuffer.flip();
        sha256.update(byteBuffer.asReadOnlyBuffer());
        return BinaryData.fromByteBuffer(byteBuffer);
    }
    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param tagOrDigest Manifest tag or digest.
     * @return The manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DownloadManifestResult downloadManifest(String tagOrDigest) {
        return downloadManifestWithResponse(tagOrDigest, null, Context.NONE).getValue();
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @param mediaTypes List of {@link  ManifestMediaType} to request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response for the manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DownloadManifestResult> downloadManifestWithResponse(String tagOrDigest, Collection<ManifestMediaType> mediaTypes, Context context) {
        Objects.requireNonNull(tagOrDigest, "'tagOrDigest' cannot be null.");

        String requestMediaTypes = getContentTypeString(mediaTypes);

        try {
            Response<BinaryData> response =
                registriesImpl.getManifestWithResponse(repositoryName, tagOrDigest,
                    requestMediaTypes, enableSync(context));
            return toDownloadManifestResponse(tagOrDigest, response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Download the blob associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @param channel The channel to write content to.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     * @throws ServiceResponseException thrown if content hash does not match requested digest.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadStream(String digest, WritableByteChannel channel) {
        downloadStream(digest, channel, Context.NONE);
    }

    /**
     * Download the blob\layer associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @param channel The channel to write content to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     * @throws ServiceResponseException thrown if content hash does not match requested digest.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadStream(String digest, WritableByteChannel channel, Context context) {
        runWithTracing(DOWNLOAD_BLOB_SPAN_NAME, (span) -> {
            downloadBlobInternal(digest, channel, span);
            return null;
        }, context);
    }

    private void downloadBlobInternal(String digest, WritableByteChannel channel, Context context) {
        Objects.requireNonNull(digest, "'digest' cannot be null.");

        context = enableSync(context);
        MessageDigest sha256 = createSha256();
        try {
            Response<BinaryData> firstChunk = readRange(digest, new HttpRange(0, (long) CHUNK_SIZE), channel, sha256, context);
            validateResponseHeaderDigest(digest, firstChunk.getHeaders());

            long blobSize = getBlobSize(firstChunk.getHeaders().get(HttpHeaderName.CONTENT_RANGE));
            for (long p = firstChunk.getValue().getLength(); p < blobSize; p += CHUNK_SIZE) {
                readRange(digest, new HttpRange(p, (long) CHUNK_SIZE), channel, sha256, context);
            }
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }

        validateDigest(sha256, digest);
    }

    private Response<BinaryData> readRange(String digest, HttpRange range, WritableByteChannel channel, MessageDigest sha256, Context context) {
        Response<BinaryData> response = blobsImpl.getChunkWithResponse(repositoryName, digest, range.toString(), context);

        ByteBuffer buffer = response.getValue().toByteBuffer();
        sha256.update(buffer.asReadOnlyBuffer());
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }

        return response;
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteBlob(String digest) {
        deleteBlobWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteBlobWithResponse(String digest, Context context) {
        Objects.requireNonNull(digest, "'digest' cannot be null.");

        context = enableSync(context);
        try {
            Response<BinaryData> streamResponse =
                blobsImpl.deleteBlobWithResponse(repositoryName, digest, enableSync(context));
            return deleteResponseToSuccess(streamResponse);
        } catch (HttpResponseException ex) {
            if (ex.getResponse().getStatusCode() == 404) {
                HttpResponse response = ex.getResponse();
                // In case of 404, we still convert it to success i.e. no-op.
                return new SimpleResponse<>(response.getRequest(), 202,
                    response.getHeaders(), null);
            } else {
                throw LOGGER.logExceptionAsError(ex);
            }
        }
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteManifest(String digest) {
        deleteManifestWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteManifestWithResponse(String digest, Context context) {
        context = enableSync(context);
        try {
            Response<Void> response = registriesImpl.deleteManifestWithResponse(repositoryName, digest,
                enableSync(context));

            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private <T> T runWithTracing(String spanName, Function<Context, T> operation, Context context) {
        Context span = tracer.start(spanName, context);
        Exception exception = null;
        try {
            return operation.apply(span);
        } catch (RuntimeException ex) {
            exception = ex;
            throw ex;
        } finally {
            tracer.end(null, exception, span);
        }
    }
}
