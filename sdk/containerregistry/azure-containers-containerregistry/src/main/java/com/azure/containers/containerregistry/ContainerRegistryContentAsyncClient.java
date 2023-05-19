// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsGetChunkHeaders;
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.containers.containerregistry.models.OciImageManifest;
import com.azure.containers.containerregistry.models.UploadRegistryBlobResult;
import com.azure.containers.containerregistry.models.SetManifestOptions;
import com.azure.containers.containerregistry.models.SetManifestResult;
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
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOCKER_DIGEST_HEADER_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOWNLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.SUPPORTED_MANIFEST_TYPES;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.UPLOAD_BLOB_SPAN_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.computeDigest;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.createSha256;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getBlobSize;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getContentLength;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getLocation;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.toGetManifestResponse;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateDigest;
import static com.azure.core.util.CoreUtils.bytesToHexString;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that exposes operations to push and pull images into container registry.
 * It exposes methods that upload, download and delete artifacts from the registry i.e. images and manifests.
 *
 * <p>View {@link ContainerRegistryContentClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryContentClientBuilder
 */
@ServiceClient(builder = ContainerRegistryContentClientBuilder.class, isAsync = true)
public final class ContainerRegistryContentAsyncClient {
    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;
    private final Tracer tracer;
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryContentAsyncClient.class);

    ContainerRegistryContentAsyncClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version, Tracer tracer) {
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
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.setManifestAsync -->
     * <pre>
     * OciImageManifest manifest = new OciImageManifest&#40;&#41;
     *         .setConfiguration&#40;configDescriptor&#41;
     *         .setSchemaVersion&#40;2&#41;
     *         .setLayers&#40;Collections.singletonList&#40;layerDescriptor&#41;&#41;;
     * Mono&lt;SetManifestResult&gt; result = contentClient.setManifest&#40;manifest, &quot;latest&quot;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.setManifestAsync -->
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     * @param manifest The {@link OciImageManifest} that needs to be uploaded.
     * @param tag Tag to apply on uploaded manifest. If {@code null} is passed, no tags will be applied.
     * @return upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SetManifestResult> setManifest(OciImageManifest manifest, String tag) {
        if (manifest == null) {
            return monoError(LOGGER, new NullPointerException("'manifest' can't be null."));
        }

        return withContext(context -> setManifestWithResponse(BinaryData.fromObject(manifest), tag, ManifestMediaType.OCI_IMAGE_MANIFEST, context))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Uploads a manifest to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadCustomManifestAsync -->
     * <pre>
     * SetManifestOptions options = new SetManifestOptions&#40;manifestList, DOCKER_MANIFEST_LIST_TYPE&#41;
     *     .setTag&#40;&quot;v2&quot;&#41;;
     *
     * contentClient.setManifestWithResponse&#40;options&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.println&#40;&quot;Manifest uploaded, digest - &quot; + response.getValue&#40;&#41;.getDigest&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadCustomManifestAsync -->
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     * @param options The options for the upload manifest operation.
     * @return The rest response containing the upload result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SetManifestResult>> setManifestWithResponse(SetManifestOptions options) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' can't be null."));
        }

        return withContext(context -> this.setManifestWithResponse(options.getManifest(), options.getTag(), options.getManifestMediaType(), context));
    }

    /**
     * Uploads a blob to the repository.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadBlobAsync -->
     * <pre>
     * BinaryData configContent = BinaryData.fromObject&#40;Collections.singletonMap&#40;&quot;hello&quot;, &quot;world&quot;&#41;&#41;;
     *
     * contentClient
     *     .uploadBlob&#40;configContent&#41;
     *     .subscribe&#40;uploadResult -&gt; System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;,
     *             uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadBlobAsync -->
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadFileAsync -->
     * <pre>
     * contentClient.uploadBlob&#40;BinaryData.fromFile&#40;Paths.get&#40;&quot;artifact.tar.gz&quot;&#41;, CHUNK_SIZE&#41;&#41;
     *     .subscribe&#40;uploadResult -&gt;
     *         System.out.printf&#40;&quot;Uploaded blob: digest - '%s', size - %s&#92;n&quot;,
     *             uploadResult.getDigest&#40;&#41;, uploadResult.getSizeInBytes&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadFileAsync -->
     *
     * <!-- src_embed com.azure.containers.containerregistry.uploadBlobAsyncErrorHandling -->
     * <pre>
     * layerContent
     *     .flatMap&#40;content -&gt; contentClient.uploadBlob&#40;content&#41;&#41;
     *     .doOnError&#40;HttpResponseException.class, &#40;ex&#41; -&gt; &#123;
     *         if &#40;ex.getCause&#40;&#41; instanceof AcrErrorsException&#41; &#123;
     *             AcrErrorsException acrErrors = &#40;AcrErrorsException&#41; ex.getCause&#40;&#41;;
     *             for &#40;AcrErrorInfo info : acrErrors.getValue&#40;&#41;.getErrors&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Uploaded blob failed: code '%s'&#92;n&quot;, info.getCode&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.uploadBlobAsyncErrorHandling -->
     *
     * <p>
     * Note:
     * </p>
     * Content may be uploaded in chunks of up to 4MB size. Chunk size depends on the passed {@link BinaryData} content.
     * When {@link BinaryData} is created using {@link BinaryData#fromFlux(Flux, Long, boolean)}, it may be uploaded in
     * chunks matching individual {@link ByteBuffer} in the {@link Flux} and up to 4MB size.
     * Buffers that are bigger than 4MB can be broken down into smaller chunks, but small buffers are not aggregated.
     * To decrease number of chunks for big content, use buffers of 4MB size.
     *
     * @param content The blob content that needs to be uploaded.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadRegistryBlobResult> uploadBlob(BinaryData content) {
        if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' can't be null."));
        }

        return withContext(context -> runWithTracing(UPLOAD_BLOB_SPAN_NAME, span -> uploadBlob(content.toFluxByteBuffer(), span), context));
    }

    /**
     * Download the manifest identified by the given tag or digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.getManifestAsync -->
     * <pre>
     * contentClient.getManifest&#40;&quot;latest&quot;&#41;
     *     .doOnNext&#40;downloadResult -&gt; &#123;
     *         if &#40;ManifestMediaType.OCI_IMAGE_MANIFEST.equals&#40;downloadResult.getManifestMediaType&#40;&#41;&#41;
     *             || ManifestMediaType.DOCKER_MANIFEST.equals&#40;downloadResult.getManifestMediaType&#40;&#41;&#41;&#41; &#123;
     *             OciImageManifest manifest = downloadResult.getManifest&#40;&#41;.toObject&#40;OciImageManifest.class&#41;;
     *             System.out.println&#40;&quot;Got OCI manifest&quot;&#41;;
     *         &#125; else &#123;
     *             throw new IllegalArgumentException&#40;&quot;Unexpected manifest type: &quot; + downloadResult.getManifestMediaType&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.getManifestAsync -->
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @return The manifest identified by the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetManifestResult> getManifest(String tagOrDigest) {
        return withContext(context -> this.getManifestWithResponse(tagOrDigest, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Download the manifest identified by the given tag or digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed com.azure.containers.containerregistry.getManifestWithResponseAsync -->
     * <pre>
     * contentClient.getManifestWithResponse&#40;&quot;latest&quot;&#41;
     *     .doOnNext&#40;response -&gt; &#123;
     *         GetManifestResult manifestResult = response.getValue&#40;&#41;;
     *         if &#40;ManifestMediaType.OCI_IMAGE_MANIFEST.equals&#40;manifestResult.getManifestMediaType&#40;&#41;&#41;
     *             || ManifestMediaType.DOCKER_MANIFEST.equals&#40;manifestResult.getManifestMediaType&#40;&#41;&#41;&#41; &#123;
     *             OciImageManifest manifest = manifestResult.getManifest&#40;&#41;.toObject&#40;OciImageManifest.class&#41;;
     *             System.out.println&#40;&quot;Got OCI manifest&quot;&#41;;
     *         &#125; else &#123;
     *             throw new IllegalArgumentException&#40;&quot;Unexpected manifest type: &quot; + manifestResult.getManifestMediaType&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.getManifestWithResponseAsync -->
     *
     * @param tagOrDigest Manifest reference which can be tag or digest.
     * @return The response for the manifest identified by the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GetManifestResult>> getManifestWithResponse(String tagOrDigest) {
        return withContext(context -> this.getManifestWithResponse(tagOrDigest, context));
    }

    /**
     * Download the blob identified by the given digest.
     * Content is downloaded in chunks of 4MB size each.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * Write content to synchronous channel, for example {@link java.nio.channels.FileChannel}:
     *
     * <!-- src_embed com.azure.containers.containerregistry.downloadStreamAsyncFile -->
     * <pre>
     * contentClient
     *     .downloadStream&#40;digest&#41;
     *     .flatMap&#40;downloadResult -&gt;
     *         Mono.using&#40;&#40;&#41; -&gt; new FileOutputStream&#40;trimSha&#40;digest&#41;&#41;,
     *             fileStream -&gt; FluxUtil.writeToWritableByteChannel&#40;
     *                 downloadResult.toFluxByteBuffer&#40;&#41;, fileStream.getChannel&#40;&#41;&#41;,
     *             fileStream -&gt; closeStream&#40;fileStream&#41;&#41;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.downloadStreamAsyncFile -->
     *
     * Write content to asynchronous byte channel, for example {@link java.nio.channels.AsynchronousSocketChannel}:
     *
     * <!-- src_embed com.azure.containers.containerregistry.downloadStreamAsyncSocket -->
     * <pre>
     * contentClient
     *     .downloadStream&#40;digest&#41;
     *     .flatMap&#40;downloadResult -&gt;
     *         Mono.using&#40;
     *             &#40;&#41; -&gt; openSocket&#40;&#41;,
     *             socket -&gt; FluxUtil.writeToAsynchronousByteChannel&#40;downloadResult.toFluxByteBuffer&#40;&#41;, socket&#41;,
     *             socket -&gt; closeStream&#40;socket&#41;&#41;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.containers.containerregistry.downloadStreamAsyncSocket -->
     *
     * @param digest The digest for the given image layer.
     * @return The image identified by the given digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> downloadStream(String digest) {
        return withContext(context ->
            runWithTracing(DOWNLOAD_BLOB_SPAN_NAME, span -> downloadBlobInternal(digest, span), context));
    }

    /**
     * Delete the image identified by the given digest
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed readme-sample-deleteBlobAsync -->
     * <pre>
     * contentClient.getManifest&#40;&quot;latest&quot;&#41;
     *     .flatMap&#40;manifest -&gt; contentClient.deleteBlob&#40;manifest.getDigest&#40;&#41;&#41;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end readme-sample-deleteBlobAsync -->
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
     * Delete the image identified by the given digest
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

    /**
     * Delete the manifest identified by the given digest.
     *
     * <p><strong>Code Samples:</strong></p>
     *
     * <!-- src_embed readme-sample-deleteManifestAsync -->
     * <pre>
     * contentClient.getManifest&#40;&quot;latest&quot;&#41;
     *     .flatMap&#40;manifest -&gt; contentClient.deleteManifest&#40;manifest.getDigest&#40;&#41;&#41;&#41;
     *     .block&#40;&#41;;
     * </pre>
     * <!-- end readme-sample-deleteManifestAsync -->
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
     * Delete the manifest identified by the given digest.
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

    private Mono<Response<SetManifestResult>> setManifestWithResponse(BinaryData manifestData, String tagOrDigest, ManifestMediaType manifestMediaType, Context context) {
        ByteBuffer data = manifestData.toByteBuffer();
        if (tagOrDigest == null) {
            tagOrDigest = computeDigest(data);
        }

        return registriesImpl
            .createManifestWithResponseAsync(
                repositoryName,
                tagOrDigest,
                BinaryData.fromByteBuffer(data),
                data.remaining(),
                manifestMediaType.toString(),
                context)
            .map(response -> (Response<SetManifestResult>)
                new ResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    ConstructorAccessors.createSetManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                    response.getDeserializedHeaders()))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }


    private Mono<Response<GetManifestResult>> getManifestWithResponse(String tagOrDigest, Context context) {
        if (tagOrDigest == null) {
            return monoError(LOGGER, new NullPointerException("'tagOrDigest' can't be null."));
        }

        return registriesImpl.getManifestWithResponseAsync(repositoryName, tagOrDigest, SUPPORTED_MANIFEST_TYPES, context)
            .map(response -> toGetManifestResponse(tagOrDigest, response))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private Mono<Response<Void>> deleteManifestWithResponse(String digest, Context context) {
        return this.registriesImpl.deleteManifestWithResponseAsync(repositoryName, digest, context)
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    /**
     * Break the source Flux into chunks that are <= chunk size. This makes filling the pooled buffers much easier
     * as we can guarantee we only need at most two buffers for any call to write (two in the case of one pool buffer
     * filling up with more data to write). We use flatMapSequential because we need to guarantee we preserve the
     * ordering of the buffers, but we don't really care if one is split before another.
     * @param data Data to chunk
     * @param length stream length
     * @return Chunked data
     */
    private static Flux<ByteBuffer> chunkSource(Flux<ByteBuffer> data, MessageDigest sha256, AtomicLong length) {
        // TODO (limolkova) unify with storage, taken from it.
        return data
            .flatMapSequential(buffer -> {
                length.addAndGet(buffer.remaining());
                if (buffer.remaining() <= CHUNK_SIZE) {
                    sha256.update(buffer.asReadOnlyBuffer());
                    return Flux.just(buffer);
                }
                int numSplits = (int) Math.ceil(buffer.remaining() / (double) CHUNK_SIZE);
                return Flux.range(0, numSplits)
                    .map(i -> {
                        ByteBuffer duplicate = buffer.duplicate().asReadOnlyBuffer();
                        duplicate.position(i * CHUNK_SIZE);
                        duplicate.limit(Math.min(duplicate.limit(), (i + 1) * CHUNK_SIZE));
                        sha256.update(duplicate.asReadOnlyBuffer());
                        return duplicate;
                    });
            }, 1, 1);
    }

    private Mono<BinaryData> downloadBlobInternal(String digest, Context context) {
        if (digest == null) {
            return monoError(LOGGER, new NullPointerException("'digest' can't be null."));
        }

        Flux<ByteBuffer> content =
            blobsImpl.getChunkWithResponseAsync(repositoryName, digest, new HttpRange(0, (long) CHUNK_SIZE).toString(), context)
                .flatMapMany(firstResponse -> getAllChunks(firstResponse, digest, context))
                .flatMapSequential(chunk -> chunk.getValue().toFluxByteBuffer(), 1);

        MessageDigest sha256 = createSha256();
        content = content
            .doOnNext(buffer -> sha256.update(buffer.asReadOnlyBuffer()))
            .doOnComplete(() -> validateDigest(sha256, digest))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);

        return BinaryData.fromFlux(content, null, false);
    }

    private Flux<ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData>> getAllChunks(
        ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData> firstResponse, String digest, Context context) {
        long blobSize = getBlobSize(firstResponse.getHeaders());
        List<Mono<ResponseBase<ContainerRegistryBlobsGetChunkHeaders, BinaryData>>> others = new ArrayList<>();
        others.add(Mono.just(firstResponse));

        long contentLength = getContentLength(firstResponse.getHeaders().get(HttpHeaderName.CONTENT_LENGTH));
        for (long p = contentLength; p < blobSize; p += CHUNK_SIZE) {
            HttpRange range = new HttpRange(p, (long) CHUNK_SIZE);
            others.add(blobsImpl.getChunkWithResponseAsync(repositoryName, digest, range.toString(), context));
        }

        return Flux.concat(others);
    }

    private Mono<Response<Void>> deleteBlobWithResponse(String digest, Context context) {
        if (digest == null) {
            return monoError(LOGGER, new NullPointerException("'digest' can't be null."));
        }

        return this.blobsImpl.deleteBlobWithResponseAsync(repositoryName, digest, context)
            .map(UtilsImpl::deleteResponseToSuccess)
            .onErrorResume(
                ex -> ex instanceof HttpResponseException && ((HttpResponseException) ex).getResponse().getStatusCode() == 404,
                ex -> {
                    HttpResponse response = ((HttpResponseException) ex).getResponse();
                    // In case of 404, we still convert it to success i.e. no-op.
                    return Mono.just(new SimpleResponse<>(response.getRequest(), 202,
                        response.getHeaders(), null));
                })
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private Mono<String> upload(Flux<ByteBuffer> data, String location, Context context) {
        AtomicReference<String> locationRef = new AtomicReference<>(location);

        return data
            .flatMapSequential(chunk -> {
                BinaryData chunkData = BinaryData.fromByteBuffer(chunk);
                return blobsImpl.uploadChunkWithResponseAsync(locationRef.get(), chunkData, chunkData.getLength(), context)
                    .map(response -> getLocation(response));
            }, 1, 1)
            .doOnNext(locationRef::set)
            .last();
    }

    private Mono<UploadRegistryBlobResult> uploadBlob(Flux<ByteBuffer> content, Context context) {
        if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' can't be null."));
        }

        AtomicLong streamLength = new AtomicLong(0);
        MessageDigest sha256 = createSha256();
        Flux<ByteBuffer> chunks = chunkSource(content, sha256, streamLength);

        return blobsImpl
            .startUploadWithResponseAsync(repositoryName, context)
            .flatMap(response -> upload(chunks, getLocation(response), context))
            // TODO (limolkova) if we knew when's the last chunk, we could upload it in complete call instead.
            .flatMap(location -> blobsImpl.completeUploadWithResponseAsync("sha256:" + bytesToHexString(sha256.digest()), location, (BinaryData) null, 0L, context))
            .map(response -> ConstructorAccessors.createUploadRegistryBlobResult(response.getHeaders().getValue(DOCKER_DIGEST_HEADER_NAME), streamLength.get()))
            .onErrorMap(AcrErrorsException.class, UtilsImpl::mapAcrErrorsException);
    }

    private <T> Mono<T> runWithTracing(String spanName, Function<Context, Mono<T>> operation, Context context) {
        Context span = tracer.start(spanName, context);
        return operation.apply(span)
            .doOnEach(signal -> {
                if (signal.isOnComplete() || signal.isOnError()) {
                    tracer.end(null, signal.getThrowable(), span);
                }
            });
    }
}
