// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.util.ChunkedDownloadUtils;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadContentAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.*;
import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client side encryption client that contains generic blob operations for Azure Storage Blobs.
 * Operations allowed by the client are uploading, downloading and copying a blob, retrieving and setting metadata,
 * retrieving and setting HTTP headers, and deleting and un-deleting a blob. The upload and download operation allow for
 * encryption and decryption of the data client side. Note: setting metadata in particular is unsafe and should only be
 * done so with caution.
 * <p> Please refer to the
 * <a href=https://docs.microsoft.com/azure/storage/common/storage-client-side-encryption-java>Azure
 * Docs For Client-Side Encryption</a> for more information.
 *
 * <p>
 * This client is instantiated through {@link EncryptedBlobClientBuilder}
 *
 * <p>
 * For operations on a specific blob type (i.e. append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient()
 * getBlockBlobClient}, or {@link #getPageBlobClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations. Note, these types do not support client-side encryption, though decryption is
 * possible in case the associated block/page/append blob contains encrypted data.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = EncryptedBlobClientBuilder.class)
public class EncryptedBlobClient extends BlobClient {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptedBlobClient.class);
    private final EncryptedBlobAsyncClient encryptedBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     */
    EncryptedBlobClient(EncryptedBlobAsyncClient encryptedBlobAsyncClient) {
        super(encryptedBlobAsyncClient);
        this.encryptedBlobAsyncClient = encryptedBlobAsyncClient;
    }

    /**
     * Creates a new {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public EncryptedBlobClient getEncryptionScopeClient(String encryptionScope) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getEncryptionScopeAsyncClient(encryptionScope));
    }

    /**
     * Creates a new {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public EncryptedBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey));
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(false);
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(boolean overwrite) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            if (exists()) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return getBlobOutputStream(null, null, null, null, requestConditions);
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        return this.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
            .setTier(tier).setRequestConditions(requestConditions));
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param options {@link BlockBlobOutputStreamOptions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options) {

        return BlobOutputStream.blockBlobOutputStream(encryptedBlobAsyncClient, options, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String -->
     * <pre>
     * try &#123;
     *     client.uploadFromFile&#40;filePath&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String -->
     *
     * @param filePath Path of the file to upload
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean -->
     * <pre>
     * try &#123;
     *     boolean overwrite = false; &#47;&#47; Default value
     *     client.uploadFromFile&#40;filePath, overwrite&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether to overwrite should data already exist on the blob
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, boolean overwrite) {
        if (!overwrite && exists()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
        }
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * long blockSize = 100 * 1024 * 1024; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFile&#40;filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT,
     *         requestConditions, timeout&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout) throws UncheckedIOException {
        this.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTier(tier).setRequestConditions(requestConditions), timeout,
            null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * Map&lt;String, String&gt; tags = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * long blockSize = 100 * 1024 * 1024; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFileWithResponse&#40;new BlobUploadFromFileOptions&#40;filePath&#41;
     *         .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;
     *         .setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;, timeout,
     *         Context.NONE&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws UncheckedIOException If an I/O error occurs
     * @return Information about the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadFromFileWithResponse(BlobUploadFromFileOptions options,
        Duration timeout, Context context)
        throws UncheckedIOException {
        Mono<Response<BlockBlobItem>> upload =
            this.encryptedBlobAsyncClient.uploadFromFileWithResponse(options)
                .subscriberContext(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String -->
     * <pre>
     * client.downloadToFile&#40;file&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return The blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobProperties downloadToFile(String filePath) {
        return this.downloadToFile(filePath, false);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>If overwrite is set to false, the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default value
     * client.downloadToFile&#40;file, overwrite&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether to overwrite the file, should the file exist.
     * @return The blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobProperties downloadToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return this.downloadToFileWithResponse(filePath, null, null, null, null, false, openOptions, null, Context.NONE)
            .getValue();
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     * <pre>
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * client.downloadToFileWithResponse&#40;file, range, new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;4L * Constants.MB&#41;,
     *     options, null, false, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Duration timeout, Context context) {
        return this.downloadToFileWithResponse(filePath, range, parallelTransferOptions, downloadRetryOptions,
            requestConditions, rangeGetContentMd5, null, timeout, context);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>By default the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown. To override this behavior, provide appropriate
     * {@link OpenOption OpenOptions} </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * Set&lt;OpenOption&gt; openOptions = new HashSet&lt;&gt;&#40;Arrays.asList&#40;StandardOpenOption.CREATE_NEW,
     *     StandardOpenOption.WRITE, StandardOpenOption.READ&#41;&#41;; &#47;&#47; Default options
     *
     * client.downloadToFileWithResponse&#40;file, blobRange, new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;4L * Constants.MB&#41;,
     *     downloadRetryOptions, null, false, openOptions, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link BlobRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     *        transfers parameter is ignored.
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified blob range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions,
        Duration timeout, Context context) {
        final com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.wrapBlobOptions(ModelHelper.populateAndApplyDefaults(parallelTransferOptions));
        return this.downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath).setRange(range)
            .setParallelTransferOptions(finalParallelTransferOptions)
            .setDownloadRetryOptions(downloadRetryOptions).setRequestConditions(requestConditions)
            .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions), timeout, context);
    }

    /**
     * Downloads the entire blob into a file specified by the path.
     *
     * <p>By default the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown. To override this behavior, provide appropriate
     * {@link OpenOption OpenOptions} </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context -->
     * <pre>
     * client.downloadToFileWithResponse&#40;new BlobDownloadToFileOptions&#40;file&#41;
     *     .setRange&#40;new BlobRange&#40;1024, 2018L&#41;&#41;
     *     .setDownloadRetryOptions&#40;new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;&#41;
     *     .setOpenOptions&#40;new HashSet&lt;&gt;&#40;Arrays.asList&#40;StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
     *         StandardOpenOption.READ&#41;&#41;&#41;, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Completed download to file&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobDownloadToFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the blob properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(BlobDownloadToFileOptions options, Duration timeout,
        Context context) {
        context = context == null ? Context.NONE : context;
        BlobProperties initialProperties =
            this.getPropertiesWithResponse(options.getRequestConditions(), timeout, context).getValue();
        if (options.getRequestConditions() == null) {
            options.setRequestConditions(new BlobRequestConditions());
        }
        options.getRequestConditions().setIfMatch(initialProperties.getETag());
        // Todo: Only add if encryptionData not null? Or is this fine?
        context = context.addData(ENCRYPTION_DATA_KEY, initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY));
        return super.downloadToFileWithResponse(options, timeout, context);
    }

    /**
     * Opens a blob input stream to download the blob.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    @Override
    public final BlobInputStream openInputStream() {
        return openInputStream((BlobRange) null, null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param range {@link BlobRange}
     * @param requestConditions An {@link BlobRequestConditions} object that represents the access conditions for the
     * blob.
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    @Override
    public final BlobInputStream openInputStream(BlobRange range, BlobRequestConditions requestConditions) {
        return openInputStream(new BlobInputStreamOptions().setRange(range).setRequestConditions(requestConditions));
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param options {@link BlobInputStreamOptions}
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    @Override
    public BlobInputStream openInputStream(BlobInputStreamOptions options) {
        return openInputStream(options, null);
    }

    /**
     * Opens a blob input stream to download the specified range of the blob.
     *
     * @param options {@link BlobInputStreamOptions}
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    protected BlobInputStream openInputStream(BlobInputStreamOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        BlobProperties initialProperties =
            this.getPropertiesWithResponse(options.getRequestConditions(), null, context).getValue();
        if (options.getRequestConditions() == null) {
            options.setRequestConditions(new BlobRequestConditions());
        }
        options.getRequestConditions().setIfMatch(initialProperties.getETag());
        // Todo: Only add if encryptionData not null? Or is this fine?
        context = context.addData(ENCRYPTION_DATA_KEY, initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY));
        return super.openInputStream(options, context);
    }

    /**
     * Downloads the entire blob into an output stream. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream -->
     * <pre>
     * client.downloadStream&#40;new ByteArrayOutputStream&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Download completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public void downloadStream(OutputStream stream) {
        downloadStreamWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    /**
     * Downloads the entire blob. Uploading data must be done from the {@link BlockBlobClient},
     * {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.downloadContent -->
     * <pre>
     * BinaryData data = client.downloadContent&#40;&#41;;
     * System.out.printf&#40;&quot;Downloaded %s&quot;, data.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.downloadContent -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStream(OutputStream)} to download larger blobs.</p>
     *
     * @return The content of the blob.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData downloadContent() {
        return this.downloadContentWithResponse(null, null, null, null).getValue();
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     * <pre>
     * BlobRange range = new BlobRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * System.out.printf&#40;&quot;Download completed with status %d%n&quot;,
     *     client.downloadStreamWithResponse&#40;new ByteArrayOutputStream&#40;&#41;, range, options, null, false,
     *         timeout, new Context&#40;key2, value2&#41;&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobDownloadResponse downloadStreamWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;
        BlobProperties initialProperties =
            this.getPropertiesWithResponse(requestConditions, timeout, context).getValue();
        if (requestConditions == null) {
            requestConditions = new BlobRequestConditions();
        }
        requestConditions.setIfMatch(initialProperties.getETag());
        // Todo: Only add if encryptionData not null? Or is this fine?
        context = context.addData(ENCRYPTION_DATA_KEY, initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY));
        return super.downloadStreamWithResponse(stream, range, options, requestConditions, getRangeContentMd5,
            timeout, context);
    }

    /**
     * Downloads a range of bytes from a blob into an output stream. Uploading data must be done from the {@link
     * BlockBlobClient}, {@link PageBlobClient}, or {@link AppendBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context -->
     * <pre>
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * BlobDownloadContentResponse contentResponse = client.downloadContentWithResponse&#40;options, null,
     *     timeout, new Context&#40;key2, value2&#41;&#41;;
     * BinaryData content = contentResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Download completed with status %d and content%s%n&quot;,
     *     contentResponse.getStatusCode&#40;&#41;, content.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStreamWithResponse(OutputStream, BlobRange,
     * DownloadRetryOptions, BlobRequestConditions, boolean, Duration, Context)}  to download larger blobs.</p>
     *
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobDownloadContentResponse downloadContentWithResponse(
        DownloadRetryOptions options, BlobRequestConditions requestConditions, Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;
        BlobProperties initialProperties =
            this.getPropertiesWithResponse(requestConditions, timeout, context).getValue();
        if (requestConditions == null) {
            requestConditions = new BlobRequestConditions();
        }
        requestConditions.setIfMatch(initialProperties.getETag());
        // Todo: Only add if encryptionData not null? Or is this fine?
        context = context.addData(ENCRYPTION_DATA_KEY, initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY));
        return super.downloadContentWithResponse(options, requestConditions, timeout, context);
    }

    /**
     * Unsupported.
     */
    @Override
    public AppendBlobClient getAppendBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an append"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public BlockBlobClient getBlockBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as a block"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public PageBlobClient getPageBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an page"
            + " blob client"));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public InputStream openQueryInputStream(String expression) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public Response<InputStream> openQueryInputStreamWithResponse(BlobQueryOptions queryOptions) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public void query(OutputStream stream, String expression) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public BlobQueryResponse queryWithResponse(BlobQueryOptions queryOptions,
        Duration timeout, Context context) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

}
