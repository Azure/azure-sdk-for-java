// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client that contains all operations that apply to any blob type.
 *
 * <p>
 * This client offers the ability to download blobs. Note that uploading data is specific to each type of blob. Please
 * refer to the {@link BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient} for upload options.
 */
public class BlobClientBase {
    private final ClientLogger logger = new ClientLogger(BlobClientBase.class);

    private final BlobAsyncClientBase client;

    /**
     * Constructor used by {@link SpecializedBlobClientBuilder}.
     *
     * @param client the async blob client
     */
    protected BlobClientBase(BlobAsyncClientBase client) {
        this.client = client;
    }

    /**
     * Creates a new {@link BlobClientBase} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobClientBase} used to interact with the specific snapshot.
     */
    public BlobClientBase getSnapshotClient(String snapshot) {
        return new BlobClientBase(client.getSnapshotClient(snapshot));
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        return client.getBlobUrl();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return client.getAccountName();
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getContainerName}
     *
     * @return The name of the container.
     */
    public final String getContainerName() {
        return client.getContainerName();
    }

    /**
     * Decodes and gets the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getBlobName}
     *
     * @return The decoded name of the blob.
     */
    public final String getBlobName() {
        return client.getBlobName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return client.getHttpPipeline();
    }

    /**
     * Gets the {@link CpkInfo} used to encrypt this blob's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return client.getCustomerProvidedKey();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return client.getServiceVersion();
    }

    /**
     * Gets the snapshotId for a blob resource
     *
     * @return A string that represents the snapshotId of the snapshot blob
     */
    public String getSnapshotId() {
        return client.getSnapshotId();
    }

    /**
     * Determines if a blob is a snapshot
     *
     * @return A boolean that indicates if a blob is a snapshot
     */
    public boolean isSnapshot() {
        return client.isSnapshot();
    }

    /**
     * Opens a blob input stream to download the blob.
     * <p>
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public final BlobInputStream openInputStream() {
        return openInputStream(new BlobRange(0), null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     * <p>
     *
     * @param range {@link BlobRange}
     * @param requestConditions An {@link BlobRequestConditions} object that represents the access conditions for the
     * blob.
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public final BlobInputStream openInputStream(BlobRange range, BlobRequestConditions requestConditions) {
        return new BlobInputStream(client, range.getOffset(), range.getCount(), requestConditions);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.exists}
     *
     * @return true if the container exists, false if it doesn't
     */
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.existsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        Mono<Response<Boolean>> response = client.existsWithResponse(context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link SyncPoller} to poll the progress of blob copy operation.
     */
    public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval) {
        return beginCopy(sourceUrl,
                null,
                null,
                null,
                null,
                null, pollInterval);
    }

    /**
     * Copies the data at the source URL to a blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param priority {@link RehydratePriority} for rehydrating the blob.
     * @param sourceModifiedRequestConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destRequestConditions {@link BlobRequestConditions} against the destination.
     * @param pollInterval Duration between each poll for the copy status. If none is specified, a default of one second
     * is used.
     * @return A {@link SyncPoller} to poll the progress of blob copy operation.
     */
    public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier,
            RehydratePriority priority, RequestConditions sourceModifiedRequestConditions,
            BlobRequestConditions destRequestConditions, Duration pollInterval) {

        return client.beginCopy(sourceUrl, metadata, tier, priority, sourceModifiedRequestConditions,
                destRequestConditions, pollInterval).getSyncPoller();
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrl#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     */
    public void abortCopyFromUrl(String copyId) {
        abortCopyFromUrlWithResponse(copyId, null, null, Context.NONE);
    }

    /**
     * Stops a pending copy that was previously started and leaves a destination blob with 0 length and metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrlWithResponse#String-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/abort-copy-blob">Azure Docs</a></p>
     *
     * @param copyId The id of the copy operation to abort.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> abortCopyFromUrlWithResponse(String copyId, String leaseId, Duration timeout,
            Context context) {
        return blockWithOptionalTimeout(client.abortCopyFromUrlWithResponse(copyId, leaseId, context), timeout);
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.copyFromUrl#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from.
     * @return The copy ID for the long running operation.
     * @throws IllegalArgumentException If {@code copySource} is a malformed {@link URL}.
     */
    public String copyFromUrl(String copySource) {
        return copyFromUrlWithResponse(copySource, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Copies the data at the source URL to a blob and waits for the copy to complete before returning a response.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/copy-blob">Azure Docs</a></p>
     *
     * @param copySource The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     * @param metadata Metadata to associate with the destination blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param sourceModifiedRequestConditions {@link RequestConditions} against the source. Standard HTTP Access
     * conditions related to the modification of data. ETag and LastModifiedTime are used to construct conditions
     * related to when the blob was changed relative to the given request. The request will fail if the specified
     * condition is not satisfied.
     * @param destRequestConditions {@link BlobRequestConditions} against the destination.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The copy ID for the long running operation.
     * @throws IllegalArgumentException If {@code copySource} is a malformed {@link URL}.
     */
    public Response<String> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier,
            RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions,
            Duration timeout, Context context) {
        Mono<Response<String>> response = client
            .copyFromUrlWithResponse(copySource, metadata, tier, sourceModifiedRequestConditions, destRequestConditions,
                context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Downloads the entire blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.download#OutputStream}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public void download(OutputStream stream) {
        downloadWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public BlobDownloadResponse downloadWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("stream", stream);
        Mono<BlobDownloadResponse> download = client
            .downloadWithResponse(range, options, requestConditions, getRangeContentMd5, context)
            .flatMap(response -> response.getValue().reduce(stream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new BlobDownloadResponse(response)));

        return blockWithOptionalTimeout(download, timeout);
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @return The blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    public BlobProperties downloadToFile(String filePath) {
        return downloadToFileWithResponse(filePath, null, null, null, null, false, null, Context.NONE).getValue();
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
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     *        transfers parameter is ignored.
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Duration timeout, Context context) {
        Mono<Response<BlobProperties>> download = client.downloadToFileWithResponse(filePath, range,
            parallelTransferOptions, downloadRetryOptions, requestConditions, rangeGetContentMd5, context);
        return blockWithOptionalTimeout(download, timeout);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     */
    public void delete() {
        deleteWithResponse(null, null, null, Context.NONE);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param deleteBlobSnapshotOptions Specifies the behavior for deleting the snapshots on this blob. {@code Include}
     * will delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<Void>> response = client
            .deleteWithResponse(deleteBlobSnapshotOptions, requestConditions, context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The blob properties and metadata.
     */
    public BlobProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The blob properties and metadata.
     */
    public Response<BlobProperties> getPropertiesWithResponse(BlobRequestConditions requestConditions, Duration timeout,
        Context context) {
        Mono<Response<BlobProperties>> response = client.getPropertiesWithResponse(requestConditions, context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setHttpHeaders#BlobHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     */
    public void setHttpHeaders(BlobHttpHeaders headers) {
        setHttpHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link BlobHttpHeaders}
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = client
            .setHttpHeadersWithResponse(headers, requestConditions, context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = client.setMetadataWithResponse(metadata, requestConditions, context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @return A response containing a {@link BlobClient} which is used to interact with the created snapshot, use
     * {@link BlobClient#getSnapshotId()} to get the identifier for the snapshot.
     */
    public BlobClientBase createSnapshot() {
        return createSnapshotWithResponse(null, null, null, Context.NONE).getValue();
    }


    /**
     * Creates a read-only snapshot of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-blob">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the blob snapshot.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link BlobClient} which is used to interact with the created snapshot, use
     * {@link BlobClient#getSnapshotId()} to get the identifier for the snapshot.
     */
    public Response<BlobClientBase> createSnapshotWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<BlobClientBase>> response = client
            .createSnapshotWithResponse(metadata, requestConditions, context)
            .map(rb -> new SimpleResponse<>(rb, new BlobClientBase(rb.getValue())));

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     */
    public void setAccessTier(AccessTier tier) {
        setAccessTierWithResponse(tier, null, null, null, Context.NONE);
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param tier The new tier for the blob.
     * @param priority Optional priority to set for re-hydrating blobs.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId,
        Duration timeout, Context context) {
        return blockWithOptionalTimeout(client.setTierWithResponse(tier, priority, leaseId, context), timeout);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.undelete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     */
    public void undelete() {
        undeleteWithResponse(null, Context.NONE);
    }

    /**
     * Undelete restores the content and metadata of a soft-deleted blob and/or any associated soft-deleted snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.undeleteWithResponse#Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/undelete-blob">Azure Docs</a></p>
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> undeleteWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = client.undeleteWithResponse(context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getAccountInfo}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @return The sku name and account kind.
     */
    public StorageAccountInfo getAccountInfo() {
        return getAccountInfoWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.getAccountInfoWithResponse#Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a></p>
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The sku name and account kind.
     */
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageAccountInfo>> response = client.getAccountInfoWithResponse(context);

        return blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Generates a user delegation SAS for the blob using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @see BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime) for more information on how to get a
     * user delegation key.
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return this.client.generateUserDelegationSas(blobServiceSasSignatureValues, userDelegationKey);
    }

    /**
     * Generates a service SAS for the blob using the specified {@link BlobServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues}
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues) {
        return this.client.generateSas(blobServiceSasSignatureValues);
    }
}
