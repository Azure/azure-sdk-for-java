// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.ProgressReporter;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobAsyncClientBase {

    private static final int BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_DOWNLOAD_BLOCK_SIZE = 100 * Constants.MB;

    private final ClientLogger logger = new ClientLogger(BlobAsyncClientBase.class);

    protected final AzureBlobStorageImpl azureBlobStorage;
    private final String snapshot;
    private final CpkInfo customerProvidedKey;
    protected final String accountName;

    /**
     * Package-private constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     * @param snapshot Optional. The snapshot identifier for the snapshot blob.
     * @param customerProvidedKey Optional. Customer provided key used during encryption of the blob's data on the
     * server.
     * @param accountName The account name for storage account.
     */
    protected BlobAsyncClientBase(AzureBlobStorageImpl azureBlobStorage, String snapshot,
        CpkInfo customerProvidedKey, String accountName) {
        this.azureBlobStorage = azureBlobStorage;
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;
        this.accountName = accountName;
    }

    /**
     * Creates a new {@link BlobAsyncClientBase} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobAsyncClientBase} used to interact with the specific snapshot.
     */
    public BlobAsyncClientBase getSnapshotClient(String snapshot) {
        return new BlobAsyncClientBase(new AzureBlobStorageBuilder()
            .url(getBlobUrl())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot, customerProvidedKey, accountName);
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        if (!this.isSnapshot()) {
            return azureBlobStorage.getUrl();
        } else {
            if (azureBlobStorage.getUrl().contains("?")) {
                return String.format("%s&snapshot=%s", azureBlobStorage.getUrl(), snapshot);
            } else {
                return String.format("%s?snapshot=%s", azureBlobStorage.getUrl(), snapshot);
            }
        }
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerName}
     *
     * @return The name of the container.
     */
    public final String getContainerName() {
        return BlobUrlParts.parse(this.azureBlobStorage.getUrl(), logger).getBlobContainerName();
    }

    /**
     * Get the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getBlobName}
     *
     * @return The name of the blob.
     */
    public final String getBlobName() {
        return BlobUrlParts.parse(this.azureBlobStorage.getUrl(), logger).getBlobName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureBlobStorage.getHttpPipeline();
    }

    /**
     * Gets the {@link CpkInfo} used to encrypt this blob's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the snapshotId for a blob resource
     *
     * @return A string that represents the snapshotId of the snapshot blob
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    /**
     * Determines if a blob is a snapshot
     *
     * @return A boolean that indicates if a blob is a snapshot
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }

    /**
     * Determines if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.exists}
     *
     * @return true if the blob exists, false if it doesn't
     */
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the blob this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.existsWithResponse}
     *
     * @return true if the blob exists, false if it doesn't
     */
    public Mono<Response<Boolean>> existsWithResponse() {
        return withContext(this::existsWithResponse);
    }

    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(null, context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURL#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> startCopyFromURL(String sourceUrl) {
        return startCopyFromURLWithResponse(sourceUrl, null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURLWithResponse#String-Map-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param priority {@link RehydratePriority} for rehydrating the blob.
     * @param sourceModifiedAccessConditions {@link ModifiedAccessConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destAccessConditions {@link BlobAccessConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> startCopyFromURLWithResponse(String sourceUrl, Map<String, String> metadata,
        AccessTier tier, RehydratePriority priority, ModifiedAccessConditions sourceModifiedAccessConditions,
        BlobAccessConditions destAccessConditions) {
        return withContext(context -> startCopyFromURLWithResponse(sourceUrl, metadata, tier, priority,
            sourceModifiedAccessConditions, destAccessConditions, context));
    }

    Mono<Response<String>> startCopyFromURLWithResponse(String sourceUrl, Map<String, String> metadata, AccessTier tier,
        RehydratePriority priority, ModifiedAccessConditions sourceModifiedAccessConditions,
        BlobAccessConditions destAccessConditions, Context context) {
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null
            ? new ModifiedAccessConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? new BlobAccessConditions() : destAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceModifiedAccessConditions.getIfModifiedSince())
            .setSourceIfUnmodifiedSince(sourceModifiedAccessConditions.getIfUnmodifiedSince())
            .setSourceIfMatch(sourceModifiedAccessConditions.getIfMatch())
            .setSourceIfNoneMatch(sourceModifiedAccessConditions.getIfNoneMatch());

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(ex));
        }

        return this.azureBlobStorage.blobs().startCopyFromURLWithRestResponseAsync(
            null, null, url, null, metadata, tier, priority, null, sourceConditions,
            destAccessConditions.getModifiedAccessConditions(), destAccessConditions.getLeaseAccessConditions(),
            context).map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getCopyId()));
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURL#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> abortCopyFromURL(String copyId) {
        return abortCopyFromURLWithResponse(copyId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURLWithResponse#String-LeaseAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> abortCopyFromURLWithResponse(String copyId,
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> abortCopyFromURLWithResponse(copyId, leaseAccessConditions, context));
    }

    Mono<Response<Void>> abortCopyFromURLWithResponse(String copyId, LeaseAccessConditions leaseAccessConditions,
        Context context) {
        return this.azureBlobStorage.blobs().abortCopyFromURLWithRestResponseAsync(
            null, null, copyId, null, null, leaseAccessConditions, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURL#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<String> copyFromURL(String copySource) {
        return copyFromURLWithResponse(copySource, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURLWithResponse#String-Map-AccessTier-ModifiedAccessConditions-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param sourceModifiedAccessConditions {@link ModifiedAccessConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destAccessConditions {@link BlobAccessConditions} against the destination.
     * @return A reactive response containing the copy ID for the long running operation.
     */
    public Mono<Response<String>> copyFromURLWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
        ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions) {
        return withContext(context -> copyFromURLWithResponse(copySource, metadata, tier,
            sourceModifiedAccessConditions, destAccessConditions, context));
    }

    Mono<Response<String>> copyFromURLWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
        ModifiedAccessConditions sourceModifiedAccessConditions, BlobAccessConditions destAccessConditions,
        Context context) {
        sourceModifiedAccessConditions = sourceModifiedAccessConditions == null
            ? new ModifiedAccessConditions() : sourceModifiedAccessConditions;
        destAccessConditions = destAccessConditions == null ? new BlobAccessConditions() : destAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceModifiedAccessConditions.getIfModifiedSince())
            .setSourceIfUnmodifiedSince(sourceModifiedAccessConditions.getIfUnmodifiedSince())
            .setSourceIfMatch(sourceModifiedAccessConditions.getIfMatch())
            .setSourceIfNoneMatch(sourceModifiedAccessConditions.getIfNoneMatch());

        URL url;
        try {
            url = new URL(copySource);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(ex));
        }

        return this.azureBlobStorage.blobs().copyFromURLWithRestResponseAsync(
            null, null, url, null, metadata, tier, null, sourceConditions,
            destAccessConditions.getModifiedAccessConditions(), destAccessConditions.getLeaseAccessConditions(),
            context).map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getCopyId()));
    }

    /**
     * Reads the entire blob. Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or
     * {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.download}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob data.
     */
    public Flux<ByteBuffer> download() {
        return downloadWithResponse(null, null, null, false)
            .flatMapMany(Response::getValue);
    }

    /**
     * Reads a range of bytes from a blob. Uploading data must be done from the {@link BlockBlobClient}, {@link
     * PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link BlobRange}
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return A reactive response containing the blob data.
     */
    public Mono<Response<Flux<ByteBuffer>>> downloadWithResponse(BlobRange range, ReliableDownloadOptions options,
        BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {
        return withContext(context -> downloadWithResponse(range, options, accessConditions, rangeGetContentMD5,
            context));
    }

    Mono<Response<Flux<ByteBuffer>>> downloadWithResponse(BlobRange range, ReliableDownloadOptions options,
        BlobAccessConditions accessConditions, boolean rangeGetContentMD5, Context context) {
        return download(range, accessConditions, rangeGetContentMD5, context)
            .map(response -> new SimpleResponse<>(
                response.getRawResponse(),
                response.body(options).switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])))));
    }

    /**
     * Reads a range of bytes from a blob. The response also includes the blob's properties and metadata. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a>.
     * <p>
     * Note that the response body has reliable download functionality built in, meaning that a failed download stream
     * will be automatically retried. This behavior may be configured with {@link ReliableDownloadOptions}.
     *
     * @param range {@link BlobRange}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return Emits the successful response.
     */
    Mono<DownloadAsyncResponse> download(BlobRange range, BlobAccessConditions accessConditions,
        boolean rangeGetContentMD5) {
        return withContext(context -> download(range, accessConditions, rangeGetContentMD5, context));
    }

    Mono<DownloadAsyncResponse> download(BlobRange range, BlobAccessConditions accessConditions,
        boolean rangeGetContentMD5, Context context) {
        range = range == null ? new BlobRange(0) : range;
        Boolean getMD5 = rangeGetContentMD5 ? rangeGetContentMD5 : null;
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;
        HttpGetterInfo info = new HttpGetterInfo()
            .setOffset(range.getOffset())
            .setCount(range.getCount())
            .setETag(accessConditions.getModifiedAccessConditions().getIfMatch());

        // TODO: range is BlobRange but expected as String
        // TODO: figure out correct response
        return this.azureBlobStorage.blobs().downloadWithRestResponseAsync(
            null, null, snapshot, null, range.toHeaderValue(), getMD5, null, null,
            accessConditions.getLeaseAccessConditions(), customerProvidedKey,
            accessConditions.getModifiedAccessConditions(), context)
            // Convert the autorest response to a DownloadAsyncResponse, which enable reliable download.
            .map(response -> {
                // If there wasn't an etag originally specified, lock on the one returned.
                info.setETag(response.getDeserializedHeaders().getETag());
                return new DownloadAsyncResponse(response, info,
                    // In the event of a stream failure, make a new request to pick up where we left off.
                    newInfo ->
                        this.download(new BlobRange(newInfo.getOffset(), newInfo.getCount()),
                            new BlobAccessConditions().setModifiedAccessConditions(
                                new ModifiedAccessConditions().setIfMatch(info.getETag())), false, context));
            });
    }


    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p>Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @return An empty response
     */
    public Mono<BlobProperties> downloadToFile(String filePath) {
        return downloadToFileWithResponse(filePath, null, null,
            null, null, false).flatMap(FluxUtil::toMono);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p>Uploading data must be done from the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link
     * AppendBlobClient}.</p>
     *
     * <p>This method makes an extra HTTP call to get the length of the blob in the beginning. To avoid this extra
     * call, provide the {@link BlobRange} parameter.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link BlobAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified blob range should be returned.
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, ReliableDownloadOptions options,
        BlobAccessConditions accessConditions, boolean rangeGetContentMD5) {
        return withContext(context -> downloadToFileWithResponse(filePath, range, parallelTransferOptions, options,
            accessConditions, rangeGetContentMD5, context));
    }

    // TODO (gapra) : Investigate if this is can be parallelized, and include the parallelTransfers parameter.
    Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, ReliableDownloadOptions options,
        BlobAccessConditions accessConditions, boolean rangeGetContentMD5, Context context) {
        final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
            ? new ParallelTransferOptions()
            : parallelTransferOptions;
        ProgressReceiver progressReceiver = finalParallelTransferOptions.getProgressReceiver();

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong(0);
        Lock progressLock = new ReentrantLock();

        return Mono.using(() -> downloadToFileResourceSupplier(filePath),
            channel -> getPropertiesWithResponse(accessConditions)
                .flatMap(response -> processInRange(channel, response,
                    range, finalParallelTransferOptions.getBlockSize(), options, accessConditions, rangeGetContentMD5,
                    context, totalProgress, progressLock, progressReceiver)), this::downloadToFileCleanup);

    }

    private Mono<Response<BlobProperties>> processInRange(AsynchronousFileChannel channel,
        Response<BlobProperties> blobPropertiesResponse, BlobRange range, Integer blockSize,
        ReliableDownloadOptions options, BlobAccessConditions accessConditions, boolean rangeGetContentMD5,
        Context context, AtomicLong totalProgress, Lock progressLock, ProgressReceiver progressReceiver) {
        return Mono.justOrEmpty(range).switchIfEmpty(Mono.just(new BlobRange(0,
            blobPropertiesResponse.getValue().getBlobSize()))).flatMapMany(rg ->
            Flux.fromIterable(sliceBlobRange(rg, blockSize)))
            .flatMap(chunk -> this.download(chunk, accessConditions, rangeGetContentMD5, context)
                .subscribeOn(Schedulers.elastic())
                .flatMap(dar -> {
                    Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                        dar.body(options), progressReceiver, progressLock, totalProgress);

                    return FluxUtil.writeFile(progressData, channel,
                        chunk.getOffset() - ((range == null) ? 0 : range.getOffset()));
                })).then(Mono.just(blobPropertiesResponse));
    }

    private AsynchronousFileChannel downloadToFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void downloadToFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private List<BlobRange> sliceBlobRange(BlobRange blobRange, Integer blockSize) {
        if (blockSize == null) {
            blockSize = BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE;
        }
        long offset = blobRange.getOffset();
        long length = blobRange.getCount();
        List<BlobRange> chunks = new ArrayList<>();
        for (long pos = offset; pos < offset + length; pos += blockSize) {
            long count = blockSize;
            if (pos + count > offset + length) {
                count = offset + length - pos;
            }
            chunks.add(new BlobRange(pos, count));
        }
        return chunks;
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        return deleteWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobAccessConditions accessConditions) {
        return withContext(context -> deleteWithResponse(deleteBlobSnapshotOptions, accessConditions, context));
    }

    Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blobs().deleteWithRestResponseAsync(
            null, null, snapshot, null, deleteBlobSnapshotOptions,
            null, accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(),
            context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<BlobProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobAccessConditions accessConditions) {
        return withContext(context -> getPropertiesWithResponse(accessConditions, context));
    }

    Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blobs().getPropertiesWithRestResponseAsync(
            null, null, snapshot, null, null, accessConditions.getLeaseAccessConditions(), customerProvidedKey,
            accessConditions.getModifiedAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, new BlobProperties(rb.getDeserializedHeaders())));
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeaders#BlobHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setHTTPHeaders(BlobHttpHeaders headers) {
        return setHTTPHeadersWithResponse(headers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeadersWithResponse#BlobHttpHeaders-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setHTTPHeadersWithResponse(BlobHttpHeaders headers,
        BlobAccessConditions accessConditions) {
        return withContext(context -> setHTTPHeadersWithResponse(headers, accessConditions, context));
    }

    Mono<Response<Void>> setHTTPHeadersWithResponse(BlobHttpHeaders headers, BlobAccessConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blobs().setHTTPHeadersWithRestResponseAsync(
            null, null, null, null, headers,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Map-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobAccessConditions accessConditions) {
        return withContext(context -> setMetadataWithResponse(metadata, accessConditions, context));
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, BlobAccessConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blobs().setMetadataWithRestResponseAsync(
            null, null, null, metadata, null, accessConditions.getLeaseAccessConditions(), customerProvidedKey,
            accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<BlobAsyncClientBase> createSnapshot() {
        return createSnapshotWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Map-BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob snapshot.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A response containing a {@link BlobAsyncClientBase} which is used to interact with the created snapshot,
     * use {@link #getSnapshotId()} to get the identifier for the snapshot.
     */
    public Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobAccessConditions accessConditions) {
        return withContext(context -> createSnapshotWithResponse(metadata, accessConditions, context));
    }

    Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata,
        BlobAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobAccessConditions() : accessConditions;

        return this.azureBlobStorage.blobs().createSnapshotWithRestResponseAsync(
            null, null, null, metadata, null, customerProvidedKey, accessConditions.getModifiedAccessConditions(),
            accessConditions.getLeaseAccessConditions(), context)
            .map(rb -> new SimpleResponse<>(rb, this.getSnapshotClient(rb.getDeserializedHeaders().getSnapshot())));
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTier#AccessTier}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    public Mono<Void> setAccessTier(AccessTier tier) {
        return setAccessTierWithResponse(tier, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param priority Optional priority to set for re-hydrating blobs.
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if {@code tier} is null.
     */
    public Mono<Response<Void>> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority,
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> setTierWithResponse(tier, priority, leaseAccessConditions, context));
    }

    Mono<Response<Void>> setTierWithResponse(AccessTier tier, RehydratePriority priority,
        LeaseAccessConditions leaseAccessConditions, Context context) {
        Utility.assertNotNull("tier", tier);

        return this.azureBlobStorage.blobs().setTierWithRestResponseAsync(
            null, null, tier, null, priority, null, leaseAccessConditions, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.undelete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> undelete() {
        return undeleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.undeleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> undeleteWithResponse() {
        return withContext(this::undeleteWithResponse);
    }

    Mono<Response<Void>> undeleteWithResponse(Context context) {
        return this.azureBlobStorage.blobs().undeleteWithRestResponseAsync(null,
            null, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfo}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        return getAccountInfoWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfoWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return a reactor response containing the sku name and account kind.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        return withContext(this::getAccountInfoWithResponse);
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return this.azureBlobStorage.blobs().getAccountInfoWithRestResponseAsync(null, null, context)
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.getDeserializedHeaders())));
    }
}
