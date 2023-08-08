// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

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
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.SetManifestOptions;
import com.azure.containers.containerregistry.models.SetManifestResult;
import com.azure.containers.containerregistry.models.UploadRegistryBlobResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ServiceResponseException;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOWNLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.SUPPORTED_MANIFEST_TYPES;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.UPLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.createSha256;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.deleteResponseToSuccess;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getBlobSize;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getLocation;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.toGetManifestResponse;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateDigest;
import static com.azure.core.util.CoreUtils.bytesToHexString;

/**
 * This class provides a client that exposes operations to push and pull images into container registry.
 * It exposes methods that upload, download and delete artifacts from the registry i.e. images and manifests.
 *
 * <p>View {@link ContainerRegistryContentClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryContentClientBuilder
 */
@ServiceClient(builder = ContainerRegistryContentClientBuilder.class)
public final class ContainerRegistryContentClient {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryContentClient.class);

    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;
    private final Tracer tracer;

    ContainerRegistryContentClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version, Tracer tracer) {
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
     * Upload the OCI manifest to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.setManifest -->
     * <pre>
     * contentClient.setManifest&#40;manifest, &quot;v1&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.setManifest -->
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param manifest The {@link OciImageManifest} that needs to be updated.
     * @param tag Tag to apply on uploaded manifest. If {@code null} is passed, no tags will be applied.
     * @return upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SetManifestResult setManifest(OciImageManifest manifest, String tag) {
        Objects.requireNonNull(manifest, "'manifest' cannot be null.");
        return setManifestWithResponse(BinaryData.fromObject(manifest), tag, ManifestMediaType.OCI_IMAGE_MANIFEST, Context.NONE).getValue();
    }

    /**
     * Uploads a manifest to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadCustomManifest -->
     * <pre>
     * SetManifestOptions options = new SetManifestOptions&#40;manifestList, DOCKER_MANIFEST_LIST_TYPE&#41;;
     *
     * Response&lt;SetManifestResult&gt; response = contentClient.setManifestWithResponse&#40;options, Context.NONE&#41;;
     * System.out.println&#40;&quot;Manifest uploaded, digest - &quot; + response.getValue&#40;&#41;.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadCustomManifest -->
     *
     * @param options The options for the upload manifest operation.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SetManifestResult> setManifestWithResponse(SetManifestOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return setManifestWithResponse(options.getManifest(), options.getTag(), options.getManifestMediaType(), context);
    }

    /**
     * Uploads a blob to the repository in chunks of 4MB.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadBlob -->
     * <pre>
     * BinaryData configContent = BinaryData.fromObject&#40;Collections.singletonMap&#40;&quot;hello&quot;, &quot;world&quot;&#41;&#41;;
     *
     * UploadRegistryBlobResult uploadResult = contentClient.uploadBlob&#40;configContent&#41;;
     * System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;, uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadBlob -->
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadBlobErrorHandling -->
     * <pre>
     * BinaryData configContent = BinaryData.fromObject&#40;Collections.singletonMap&#40;&quot;hello&quot;, &quot;world&quot;&#41;&#41;;
     *
     * try &#123;
     *     UploadRegistryBlobResult uploadResult = contentClient.uploadBlob&#40;configContent&#41;;
     *     System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;, uploadResult.getDigest&#40;&#41;,
     *         uploadResult.getSizeInBytes&#40;&#41;&#41;;
     * &#125; catch &#40;HttpResponseException ex&#41; &#123;
     *     if &#40;ex.getValue&#40;&#41; instanceof ResponseError&#41; &#123;
     *         ResponseError error = &#40;ResponseError&#41; ex.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Upload failed: code '%s'&#92;n&quot;, error.getCode&#40;&#41;&#41;;
     *         if &#40;&quot;BLOB_UPLOAD_INVALID&quot;.equals&#40;error.getCode&#40;&#41;&#41;&#41; &#123;
     *             System.out.println&#40;&quot;Transient upload issue, starting upload over&quot;&#41;;
     *             &#47;&#47; retry upload
     *         &#125;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadBlobErrorHandling -->
     *
     * @param content The blob content. The content may be loaded into memory depending on how {@link BinaryData} is created.
     * @return The upload response.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadRegistryBlobResult uploadBlob(BinaryData content) {
        return uploadBlob(content, Context.NONE);
    }

    /**
     * Uploads a blob to the repository in chunks of 4MB.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadFile -->
     * <pre>
     * BinaryData content = BinaryData.fromFile&#40;Paths.get&#40;&quot;artifact.tar.gz&quot;, CHUNK_SIZE&#41;&#41;;
     * UploadRegistryBlobResult uploadResult = contentClient.uploadBlob&#40;content, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;,
     *     uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadFile -->
     *
     * @param content The blob content.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The upload response.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code stream} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadRegistryBlobResult uploadBlob(BinaryData content, Context context) {
        Objects.requireNonNull(content, "'content' cannot be null.");

        InputStream stream = content.toStream();
        try {
            return runWithTracing(UPLOAD_BLOB_SPAN_NAME, (span) -> uploadBlobInternal(stream, span), context);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warning("Failed to close the stream", e);
            }
        }
    }

    /**
     * Download the manifest identified by the given tag or digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * Download manifest with tag:
     *
     * <!-- src_embed com.azure.containers.containerregistry.getManifestTag -->
     * <pre>
     * GetManifestResult latestResult = contentClient.getManifest&#40;&quot;latest&quot;&#41;;
     * if &#40;ManifestMediaType.DOCKER_MANIFEST.equals&#40;latestResult.getManifestMediaType&#40;&#41;&#41;
     *     || ManifestMediaType.OCI_IMAGE_MANIFEST.equals&#40;latestResult.getManifestMediaType&#40;&#41;&#41;&#41; &#123;
     *     OciImageManifest manifest = latestResult.getManifest&#40;&#41;.toObject&#40;OciImageManifest.class&#41;;
     * &#125; else &#123;
     *     throw new IllegalArgumentException&#40;&quot;Unexpected manifest type: &quot; + latestResult.getManifestMediaType&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.getManifestTag -->
     *
     * Download manifest with digest:
     *
     * <!-- src_embed com.azure.containers.containerregistry.getManifestDigest -->
     * <pre>
     * GetManifestResult getManifestResult = contentClient.getManifest&#40;
     *     &quot;sha256:6581596932dc735fd0df8cc240e6c28845a66829126da5ce25b983cf244e2311&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.getManifestDigest -->
     *
     * @param tagOrDigest Manifest tag or digest.
     * @return The manifest identified by the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public GetManifestResult getManifest(String tagOrDigest) {
        return getManifestWithResponse(tagOrDigest, Context.NONE).getValue();
    }

    /**
     * Download the manifest of custom type identified by the given tag or digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.getManifestWithResponse -->
     * <pre>
     * Response&lt;GetManifestResult&gt; downloadResponse = contentClient.getManifestWithResponse&#40;&quot;latest&quot;,
     *     Context.NONE&#41;;
     * System.out.printf&#40;&quot;Received manifest: digest - %s, response code: %s&#92;n&quot;, downloadResponse.getValue&#40;&#41;.getDigest&#40;&#41;,
     *     downloadResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.getManifestWithResponse -->
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response for the manifest identified by the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<GetManifestResult> getManifestWithResponse(String tagOrDigest, Context context) {
        Objects.requireNonNull(tagOrDigest, "'tagOrDigest' cannot be null.");

        try {
            Response<BinaryData> response =
                registriesImpl.getManifestWithResponse(repositoryName, tagOrDigest, SUPPORTED_MANIFEST_TYPES, context);
            return toGetManifestResponse(tagOrDigest, response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Download the blob identified by  the given digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.downloadStream -->
     * <pre>
     * Path file = Files.createTempFile&#40;digest, &quot;.tmp&quot;&#41;;
     * SeekableByteChannel channel = Files.newByteChannel&#40;file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE&#41;;
     * contentClient.downloadStream&#40;digest, channel&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.downloadStream -->
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
     * Download the blob identified by the given digest.
     *
     * @param digest The digest for the given image layer.
     * @param channel The channel to write content to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     * @throws ServiceResponseException thrown if content hash does not match requested digest.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadStream(String digest, WritableByteChannel channel, Context context) {
        runWithTracing(DOWNLOAD_BLOB_SPAN_NAME, (span) -> downloadBlobInternal(digest, channel, span), context);
    }

    /**
     * Delete the image identified by the given digest
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed readme-sample-deleteBlob -->
     * <pre>
     * GetManifestResult manifestResult = contentClient.getManifest&#40;&quot;latest&quot;&#41;;
     *
     * OciImageManifest manifest = manifestResult.getManifest&#40;&#41;.toObject&#40;OciImageManifest.class&#41;;
     * for &#40;OciDescriptor layer : manifest.getLayers&#40;&#41;&#41; &#123;
     *     contentClient.deleteBlob&#40;layer.getDigest&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end readme-sample-deleteBlob -->
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
     * Delete the image identified by the given digest
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

        try {
            Response<Void> response = blobsImpl.deleteBlobWithResponse(repositoryName, digest, context);
            return deleteResponseToSuccess(response);
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
     * Delete the manifest identified by the given digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed readme-sample-deleteManifest -->
     * <pre>
     * GetManifestResult manifestResult = contentClient.getManifest&#40;&quot;latest&quot;&#41;;
     * contentClient.deleteManifest&#40;manifestResult.getDigest&#40;&#41;&#41;;
     * </pre>
     * <!-- end readme-sample-deleteManifest -->
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
     * Delete the manifest identified by the given digest.
     *
     * @param digest The digest of the manifest.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteManifestWithResponse(String digest, Context context) {
        try {
            Response<Void> response = registriesImpl.deleteManifestWithResponse(repositoryName, digest, context);
            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private UploadRegistryBlobResult uploadBlobInternal(InputStream stream, Context context) {
        MessageDigest sha256 = createSha256();
        byte[] buffer = new byte[CHUNK_SIZE];

        try {
            ResponseBase<ContainerRegistryBlobsStartUploadHeaders, Void> startUploadResponse =
                blobsImpl.startUploadWithResponse(repositoryName, context);
            String location = getLocation(startUploadResponse);

            BinaryData chunk;
            long streamLength = 0L;
            while (true) {
                chunk = readChunk(stream, sha256, buffer);
                if (chunk == null) {
                    break;
                }

                streamLength += chunk.getLength();
                if (chunk.getLength() < CHUNK_SIZE) {
                    break;
                }

                ResponseBase<ContainerRegistryBlobsUploadChunkHeaders, Void> uploadChunkResponse =
                    blobsImpl.uploadChunkWithResponse(location, chunk, chunk.getLength(), context);
                location = getLocation(uploadChunkResponse);
            }

            String digest = "sha256:" + bytesToHexString(sha256.digest());

            ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, Void> completeUploadResponse =
                blobsImpl.completeUploadWithResponse(digest, location, chunk, chunk == null ? null : chunk.getLength(), context);

            return ConstructorAccessors.createUploadRegistryBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest(), streamLength);
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private BinaryData readChunk(InputStream stream, MessageDigest sha256, byte[] buffer) {
        int position = 0;
        while (position < CHUNK_SIZE) {
            try {
                int read = stream.read(buffer, position, CHUNK_SIZE - position);
                if (read < 0) {
                    break;
                }
                position += read;
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }
        }
        if (position == 0) {
            return null;
        }

        sha256.update(buffer, 0, position);

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.limit(position);
        return BinaryData.fromByteBuffer(byteBuffer);
    }

    private Response<SetManifestResult> setManifestWithResponse(BinaryData manifestData, String tagOrDigest, ManifestMediaType manifestMediaType, Context context) {
        BinaryData data = manifestData.toReplayableBinaryData();
        if (tagOrDigest == null) {
            tagOrDigest = computeDigest(data.toByteBuffer());
        }

        try {
            ResponseBase<ContainerRegistriesCreateManifestHeaders, Void> response = this.registriesImpl
                .createManifestWithResponse(repositoryName, tagOrDigest, data, data.getLength(),
                    manifestMediaType.toString(), context);

            return new ResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                ConstructorAccessors.createSetManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                response.getDeserializedHeaders());
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    private Context downloadBlobInternal(String digest, WritableByteChannel channel, Context context) {
        Objects.requireNonNull(digest, "'digest' cannot be null.");

        MessageDigest sha256 = createSha256();
        try {
            HttpRange range = new HttpRange(0, (long) CHUNK_SIZE);
            // TODO (limolkova) https://github.com/Azure/azure-sdk-for-java/issues/34400
            context = context.addData("azure-eagerly-read-response", true);
            Response<BinaryData> lastChunk = blobsImpl.getChunkWithResponse(repositoryName, digest, range.toString(), context);
            long blobSize = getBlobSize(lastChunk.getHeaders());
            long length = writeChunk(lastChunk, sha256, channel);

            for (long p = length; p < blobSize; p += CHUNK_SIZE) {
                range = new HttpRange(p, (long) CHUNK_SIZE);
                lastChunk = blobsImpl.getChunkWithResponse(repositoryName, digest, range.toString(), context);
                writeChunk(lastChunk, sha256, channel);
            }
        } catch (AcrErrorsException exception) {
            throw LOGGER.logExceptionAsError(mapAcrErrorsException(exception));
        }

        validateDigest(sha256, digest);

        return context;
    }

    private long writeChunk(Response<BinaryData> response, MessageDigest sha256, WritableByteChannel channel) {
        InputStream content = response.getValue().toStream();
        ByteBuffer buffer = ByteBuffer.wrap(getBytes(content));
        sha256.update(buffer.asReadOnlyBuffer());
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } finally {
            try {
                content.close();
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        }

        return buffer.limit();
    }

    private byte[] getBytes(InputStream stream) {
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[8192];
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArray();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
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
