// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.BufferAggregator;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.file.datalake.implementation.models.CpkInfo;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathExpiryOptions;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileExpirationOffset;
import com.azure.storage.file.datalake.models.FileQueryAsyncResponse;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.options.DataLakeFileAppendOptions;
import com.azure.storage.file.datalake.options.DataLakeFileFlushOptions;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * This class provides a client that contains file operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a file, deleting a file, renaming a file, setting metadata and
 * http headers, setting and retrieving access control, getting properties, reading a file, and appending and flushing
 * data to write to a file.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemAsyncClient#getFileAsyncClient(String)}.
 *
 * <p>
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class, isAsync = true)
public class DataLakeFileAsyncClient extends DataLakePathAsyncClient {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    static final long MAX_APPEND_FILE_BYTES = 4000L * Constants.MB;

    private static final ClientLogger LOGGER = new ClientLogger(DataLakeFileAsyncClient.class);

    /**
     * Package-private constructor for use by {@link DataLakePathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param fileName The file name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakeFileAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, String fileName, BlockBlobAsyncClient blockBlobAsyncClient,
        AzureSasCredential sasToken, CpkInfo customerProvidedKey, boolean isTokenCredentialAuthenticated) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, fileName, PathResourceType.FILE,
            blockBlobAsyncClient, sasToken, customerProvidedKey, isTokenCredentialAuthenticated);
    }

    DataLakeFileAsyncClient(DataLakePathAsyncClient pathAsyncClient) {
        super(pathAsyncClient.getHttpPipeline(), pathAsyncClient.getAccountUrl(), pathAsyncClient.getServiceVersion(),
            pathAsyncClient.getAccountName(), pathAsyncClient.getFileSystemName(),
            Utility.urlEncode(pathAsyncClient.pathName), PathResourceType.FILE,
            pathAsyncClient.getBlockBlobAsyncClient(), pathAsyncClient.getSasToken(),
            pathAsyncClient.getCpkInfo(), pathAsyncClient.isTokenCredentialAuthenticated());
    }

    /**
     * Gets the URL of the file represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    public String getFileUrl() {
        return getPathUrl();
    }

    /**
     * Gets the path of this file, not including the name of the resource itself.
     *
     * @return The path of the file.
     */
    public String getFilePath() {
        return getObjectPath();
    }

    /**
     * Gets the name of this file, not including its full path.
     *
     * @return The name of the file.
     */
    public String getFileName() {
        return getObjectName();
    }

    /**
     * Creates a new {@link DataLakeFileAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the file,
     * pass {@code null} to use no customer provided key.
     * @return a {@link DataLakeFileAsyncClient} with the specified {@code customerProvidedKey}.
     */
    public DataLakeFileAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new DataLakeFileAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getFileSystemName(), getObjectPath(), this.blockBlobAsyncClient, getSasToken(), finalCustomerProvidedKey,
            isTokenCredentialAuthenticated());
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.delete -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.delete -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteWithResponse#DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.deleteWithResponse&#40;requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteWithResponse#DataLakeRequestConditions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(null /* recursive */, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a file if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteIfExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the file was successfully
     * deleted, {@code false} indicates that the file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a file if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteIfExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathDeleteOptions}
     *
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(DataLakePathDeleteOptions options) {
        try {
            options = options == null ? new DataLakePathDeleteOptions() : options;
            return deleteWithResponse(options.getRequestConditions())
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof DataLakeStorageException && ((DataLakeStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new file and uploads content.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions -->
     * <pre>
     * client.uploadFromFile&#40;filePath&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions -->
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return upload(data, parallelTransferOptions, false);
    }

    /**
     * Creates a new file and uploads content.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#BinaryData-ParallelTransferOptions -->
     * <pre>
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions pto = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setProgressListener&#40;bytesTransferred -&gt; System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * BinaryData.fromFlux&#40;data, length, false&#41;
     *     .flatMap&#40;binaryData -&gt; client.upload&#40;binaryData, pto&#41;&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#BinaryData-ParallelTransferOptions -->
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(BinaryData data, ParallelTransferOptions parallelTransferOptions) {
        return upload(data, parallelTransferOptions, false);
    }

    /**
     * Creates a new file and uploads content.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.uploadFromFile&#40;filePath, overwrite&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether to overwrite, should the file already exist.
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {

        Mono<Void> overwriteCheck;
        DataLakeRequestConditions requestConditions;

        if (overwrite) {
            overwriteCheck = Mono.empty();
            requestConditions = null;
        } else {
            overwriteCheck = exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : Mono.empty());
            requestConditions = new DataLakeRequestConditions()
                .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck
            .then(uploadWithResponse(data, parallelTransferOptions, null, null, requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file and uploads content.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#BinaryData-ParallelTransferOptions-boolean -->
     * <pre>
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions pto = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setProgressListener&#40;bytesTransferred -&gt; System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * BinaryData.fromFlux&#40;data, length, false&#41;
     *     .flatMap&#40;binaryData -&gt; client.upload&#40;binaryData, pto, true&#41;&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#BinaryData-ParallelTransferOptions-boolean -->
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether to overwrite, should the file already exist.
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(BinaryData data, ParallelTransferOptions parallelTransferOptions, boolean overwrite) {
        Mono<Void> overwriteCheck;
        DataLakeRequestConditions requestConditions;

        if (overwrite) {
            overwriteCheck = Mono.empty();
            requestConditions = null;
        } else {
            overwriteCheck = exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : Mono.empty());
            requestConditions = new DataLakeRequestConditions()
                .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck
            .then(uploadWithResponse(new FileParallelUploadOptions(data)
                .setParallelTransferOptions(parallelTransferOptions).setRequestConditions(requestConditions)))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file.
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadWithResponse&#40;data, parallelTransferOptions, headers, metadata, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Uploaded file %n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions.ProgressReporter -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadataMap = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions conditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * ParallelTransferOptions pto = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setProgressListener&#40;bytesTransferred -&gt; System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * client.uploadWithResponse&#40;data, pto, httpHeaders, metadataMap, conditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Uploaded file %n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions.ProgressReporter -->
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            return uploadWithResponse(new FileParallelUploadOptions(data)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setRequestConditions(requestConditions));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadWithResponse&#40;new FileParallelUploadOptions&#40;data&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;
     *     .setMetadata&#40;metadata&#41;.setRequestConditions&#40;requestConditions&#41;
     *     .setPermissions&#40;&quot;permissions&quot;&#41;.setUmask&#40;&quot;umask&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Uploaded file %n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions -->
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions.ProgressReporter -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadataMap = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions conditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * ParallelTransferOptions pto = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setProgressListener&#40;bytesTransferred -&gt; System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * client.uploadWithResponse&#40;new FileParallelUploadOptions&#40;data&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;
     *     .setMetadata&#40;metadata&#41;.setRequestConditions&#40;requestConditions&#41;
     *     .setPermissions&#40;&quot;permissions&quot;&#41;.setUmask&#40;&quot;umask&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Uploaded file %n&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions.ProgressReporter -->
     *
     * @param options {@link FileParallelUploadOptions}
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> uploadWithResponse(FileParallelUploadOptions options) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            DataLakeRequestConditions validatedRequestConditions = options.getRequestConditions() == null
                ? new DataLakeRequestConditions() : options.getRequestConditions();
            /* Since we are creating a file with the request conditions, everything but lease id becomes invalid
             after creation, so remove them for the append/flush calls. */
            DataLakeRequestConditions validatedUploadRequestConditions = new DataLakeRequestConditions()
                .setLeaseId(validatedRequestConditions.getLeaseId());
            final ParallelTransferOptions validatedParallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
            long fileOffset = 0;

            Function<Flux<ByteBuffer>, Mono<Response<PathInfo>>> uploadInChunksFunction = (stream) ->
                uploadInChunks(stream, fileOffset, validatedParallelTransferOptions, options.getHeaders(),
                    validatedUploadRequestConditions);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<PathInfo>>> uploadFullMethod =
                (stream, length) -> uploadWithResponse(stream,
                    fileOffset, length, options.getHeaders(), validatedUploadRequestConditions,
                    validatedParallelTransferOptions.getProgressListener());

            BinaryData binaryData = options.getData();

            // if BinaryData is present, convert it to Flux Byte Buffer
            Flux<ByteBuffer> data = binaryData != null ? binaryData.toFluxByteBuffer() : options.getDataFlux();
            data = UploadUtils.extractByteBuffer(data, options.getOptionalLength(),
                validatedParallelTransferOptions.getBlockSizeLong(), options.getDataStream());

            DataLakePathCreateOptions createOptions = new DataLakePathCreateOptions()
                .setPermissions(options.getPermissions())
                .setUmask(options.getUmask())
                .setPathHttpHeaders(options.getHeaders())
                .setMetadata(options.getMetadata())
                .setRequestConditions(validatedRequestConditions)
                .setEncryptionContext(options.getEncryptionContext());

            return createWithResponse(createOptions)
                .then(UploadUtils.uploadFullOrChunked(data, validatedParallelTransferOptions,
                    uploadInChunksFunction, uploadFullMethod));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private Mono<Response<PathInfo>> uploadInChunks(Flux<ByteBuffer> data, long fileOffset,
        ParallelTransferOptions parallelTransferOptions, PathHttpHeaders httpHeaders,
        DataLakeRequestConditions requestConditions) {

        // Validation done in the constructor.
        BufferStagingArea stagingArea = new BufferStagingArea(parallelTransferOptions.getBlockSizeLong(), MAX_APPEND_FILE_BYTES);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data, parallelTransferOptions);

        ProgressListener progressListener = parallelTransferOptions.getProgressListener();
        ProgressReporter progressReporter = progressListener == null ? null : ProgressReporter.withProgressListener(
            progressListener);

        /*
         Write to the stagingArea and upload the output.
         maxConcurrency = 1 when writing means only 1 BufferAggregator will be accumulating at a time.
         parallelTransferOptions.getMaxConcurrency() appends will be happening at once, so we guarantee buffering of
         only concurrency + 1 chunks at a time.
         */
        return chunkedSource.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            /* Map the data to a tuple 3, of buffer, buffer length, buffer offset */
            .map(bufferAggregator -> Tuples.of(bufferAggregator, bufferAggregator.length(), 0L))
            /* Scan reduces a flux with an accumulator while emitting the intermediate results. */
            /* As an example, data consists of ByteBuffers of length 10-10-5.
               In the map above we transform the initial ByteBuffer to a tuple3 of buff, 10, 0.
               Scan will emit that as is, then accumulate the tuple for the next emission.
               On the second iteration, the middle ByteBuffer gets transformed to buff, 10, 10+0
               (from previous emission). Scan emits that, and on the last iteration, the last ByteBuffer gets
               transformed to buff, 5, 10+10 (from previous emission). */
            .scan((result, source) -> {
                BufferAggregator bufferAggregator = source.getT1();
                long currentBufferLength = bufferAggregator.length();
                long lastBytesWritten = result.getT2();
                long lastOffset = result.getT3();

                return Tuples.of(bufferAggregator, currentBufferLength, lastBytesWritten + lastOffset);
            })
            .flatMapSequential(tuple3 -> {
                BufferAggregator bufferAggregator = tuple3.getT1();
                long currentBufferLength = bufferAggregator.length();
                long currentOffset = tuple3.getT3() + fileOffset;
                final long offset = currentBufferLength + currentOffset;
                Contexts appendContexts = Contexts.empty();
                if (progressReporter != null) {
                    appendContexts.setHttpRequestProgressReporter(progressReporter.createChild());
                }
                return appendWithResponse(bufferAggregator.asFlux(), currentOffset, currentBufferLength,
                    new DataLakeFileAppendOptions().setLeaseId(requestConditions.getLeaseId()), appendContexts.getContext())
                    .map(resp -> offset) /* End of file after append to pass to flush. */
                    .flux();
            }, parallelTransferOptions.getMaxConcurrency(), 1)
            .last()
            .flatMap(length -> flushWithResponse(length, false, false, httpHeaders, requestConditions));
    }

    private Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions, ProgressListener progressListener) {
        Contexts appendContexts = Contexts.empty();
        if (progressListener != null) {
            appendContexts.setHttpRequestProgressReporter(
                ProgressReporter.withProgressListener(progressListener));
        }
        return appendWithResponse(data, fileOffset, length, new DataLakeFileAppendOptions().setLeaseId(requestConditions.getLeaseId()),
            appendContexts.getContext())
            .flatMap(resp -> flushWithResponse(fileOffset + length, false, false, httpHeaders,
                requestConditions));
    }

    /**
     * Creates a new file, with the content of the specified file. By default, this method will not overwrite an
     * existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String -->
     * <pre>
     * client.uploadFromFile&#40;filePath&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String -->
     *
     * @param filePath Path to the upload file
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath) {
        return uploadFromFile(filePath, false);
    }

    /**
     * Creates a new file, with the content of the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.uploadFromFile&#40;filePath, overwrite&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether to overwrite, should the file already exist.
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        Mono<Void> overwriteCheck = Mono.empty();
        DataLakeRequestConditions requestConditions = null;

        // Note that if the file will be uploaded using a putBlob, we also can skip the exists check.
        //
        // Default behavior is to use uploading in chunks when the file size is greater than 100 MB.
        if (!overwrite) {
            if (UploadUtils.shouldUploadInChunks(filePath, ModelHelper.FILE_DEFAULT_MAX_SINGLE_UPLOAD_SIZE, LOGGER)) {
                overwriteCheck = exists().flatMap(exists -> exists
                    ? monoError(LOGGER, new IllegalArgumentException(Constants.FILE_ALREADY_EXISTS))
                    : Mono.empty());
            }

            requestConditions = new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck.then(uploadFromFile(filePath, null, null, null, requestConditions));
    }

    /**
     * Creates a new file, with the content of the specified file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadFromFile&#40;filePath, parallelTransferOptions, headers, metadata, requestConditions&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions) {
        return uploadFromFileWithResponse(filePath, parallelTransferOptions, headers, metadata, requestConditions).then();
    }

    /**
     * Creates a new file, with the content of the specified file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFileWithResponse#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadFromFileWithResponse&#40;filePath, parallelTransferOptions, headers, metadata, requestConditions&#41;
     *     .doOnError&#40;throwable -&gt;
     *         System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt;
     *         System.out.println&#40;&quot;Upload from file succeeded at: &quot; + completion.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFileWithResponse#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the information of the uploaded file.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> uploadFromFileWithResponse(String filePath, ParallelTransferOptions parallelTransferOptions,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions) {
        Long originalBlockSize = (parallelTransferOptions == null)
            ? null
            : parallelTransferOptions.getBlockSizeLong();

        DataLakeRequestConditions validatedRequestConditions = requestConditions == null
            ? new DataLakeRequestConditions() : requestConditions;
        /* Since we are creating a file with the request conditions, everything but lease id becomes invalid
           after creation, so e remove them for the append/flush calls. */
        DataLakeRequestConditions validatedUploadRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(validatedRequestConditions.getLeaseId());

        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(parallelTransferOptions);
        long fileOffset = 0;

        try {
            return Mono.using(() -> UploadUtils.uploadFileResourceSupplier(filePath, LOGGER),
                channel -> {
                    try {
                        long fileSize = channel.size();

                        if (fileSize == 0) {
                            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Size of the file must be "
                                + "greater than 0."));
                        }

                        // By default, if the file is larger than 100 MB chunk it and append it in stages.
                        // But, this is configurable by the user passing options with max single upload size configured.
                        if (UploadUtils.shouldUploadInChunks(filePath,
                            finalParallelTransferOptions.getMaxSingleUploadSizeLong(), LOGGER)) {
                            return createWithResponse(null, null, headers, metadata, validatedRequestConditions)
                                .then(uploadFileChunks(fileOffset, fileSize, finalParallelTransferOptions,
                                    originalBlockSize, headers, validatedUploadRequestConditions, channel));
                        } else {
                            // Otherwise, we know it can be sent in a single request reducing network overhead.
                            return createWithResponse(null, null, headers, metadata, validatedRequestConditions)
                                .then(uploadWithResponse(FluxUtil.readFile(channel), fileOffset, fileSize, headers,
                                    validatedUploadRequestConditions,
                                    finalParallelTransferOptions.getProgressListener()));
                        }
                    } catch (IOException ex) {
                        return Mono.error(ex);
                    }
                },
                channel -> UploadUtils.uploadFileCleanup(channel, LOGGER));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private Mono<Response<PathInfo>> uploadFileChunks(long fileOffset, long fileSize, ParallelTransferOptions parallelTransferOptions,
        Long originalBlockSize, PathHttpHeaders headers, DataLakeRequestConditions requestConditions,
        AsynchronousFileChannel channel) {
        // parallelTransferOptions are finalized in the calling method.

        ProgressListener progressListener = parallelTransferOptions.getProgressListener();
        ProgressReporter progressReporter = progressListener == null ? null : ProgressReporter.withProgressListener(
            progressListener);

        return Flux.fromIterable(sliceFile(fileSize, originalBlockSize, parallelTransferOptions.getBlockSizeLong()))
            .flatMap(chunk -> {
                Flux<ByteBuffer> data = FluxUtil.readFile(channel, chunk.getOffset(), chunk.getCount());

                Contexts appendContexts = Contexts.empty();
                if (progressReporter != null) {
                    appendContexts.setHttpRequestProgressReporter(progressReporter.createChild());
                }
                return appendWithResponse(data, fileOffset + chunk.getOffset(), chunk.getCount(),
                    new DataLakeFileAppendOptions().setLeaseId(requestConditions.getLeaseId()), appendContexts.getContext());
            }, parallelTransferOptions.getMaxConcurrency())
            .then(Mono.defer(() -> flushWithResponse(fileSize, false, false, headers, requestConditions)));
    }

    private static List<FileRange> sliceFile(long fileSize, Long originalBlockSize, long blockSize) {
        List<FileRange> ranges = new ArrayList<>();
        if (fileSize > 100 * Constants.MB && originalBlockSize == null) {
            blockSize = BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        }
        for (long pos = 0; pos < fileSize; pos += blockSize) {
            long count = blockSize;
            if (pos + count > fileSize) {
                count = fileSize - pos;
            }
            ranges.add(new FileRange(pos, count));
        }
        return ranges;
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long -->
     * <pre>
     * client.append&#40;data, offset, length&#41;
     *     .subscribe&#40;
     *         response -&gt; System.out.println&#40;&quot;Append data completed&quot;&#41;,
     *         error -&gt; System.out.printf&#40;&quot;Error when calling append data: %s&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> append(Flux<ByteBuffer> data, long fileOffset, long length) {
        return appendWithResponse(data, fileOffset, length, new DataLakeFileAppendOptions(), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long -->
     * <pre>
     * client.append&#40;data, offset, length&#41;
     *     .subscribe&#40;
     *         response -&gt; System.out.println&#40;&quot;Append data completed&quot;&#41;,
     *         error -&gt; System.out.printf&#40;&quot;Error when calling append data: %s&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> append(BinaryData data, long fileOffset) {
        return appendWithResponse(data, fileOffset, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     *
     * client.appendWithResponse&#40;data, offset, length, contentMd5, leaseId&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Append data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     * @param leaseId By setting lease id, requests will fail if the provided lease does not match the active lease on
     * the file.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        byte[] contentMd5, String leaseId) {
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseId(leaseId)
            .setContentHash(contentMd5)
            .setFlush(null);
        try {
            return withContext(context -> appendWithResponse(data, fileOffset, length, appendOptions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-DataLakeFileAppendOptions -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     * DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setContentHash&#40;contentMd5&#41;
     *     .setFlush&#40;true&#41;;
     *
     * client.appendWithResponse&#40;data, offset, length, appendOptions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Append data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-DataLakeFileAppendOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param appendOptions {@link DataLakeFileAppendOptions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        DataLakeFileAppendOptions appendOptions) {
        return appendWithResponse(data, fileOffset, length, appendOptions, null);
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#BinaryData-long-byte-String -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     * BinaryData data = BinaryData.fromString&#40;&quot;Data!&quot;&#41;;
     *
     * client.appendWithResponse&#40;data, offset, contentMd5, leaseId&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Append data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#BinaryData-long-byte-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     * @param leaseId By setting lease id, requests will fail if the provided lease does not match the active lease on
     * the file.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> appendWithResponse(BinaryData data, long fileOffset, byte[] contentMd5, String leaseId) {
        try {
            Objects.requireNonNull(data);
            Flux<ByteBuffer> fluxData = data.toFluxByteBuffer();
            long length = data.getLength();
            DataLakeFileAppendOptions options = new DataLakeFileAppendOptions()
                .setLeaseId(leaseId)
                .setContentHash(contentMd5)
                .setFlush(null);
            return withContext(context -> appendWithResponse(fluxData, fileOffset, length, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#BinaryData-long-DataLakeFileAppendOptions -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     * DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setContentHash&#40;contentMd5&#41;
     *     .setFlush&#40;true&#41;;
     * BinaryData data = BinaryData.fromString&#40;&quot;Data!&quot;&#41;;
     *
     * client.appendWithResponse&#40;data, offset, appendOptions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Append data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#BinaryData-long-DataLakeFileAppendOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param appendOptions {@link DataLakeFileAppendOptions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> appendWithResponse(BinaryData data, long fileOffset, DataLakeFileAppendOptions appendOptions) {
        Objects.requireNonNull(data);
        Flux<ByteBuffer> fluxData = data.toFluxByteBuffer();
        long length = data.getLength();
        return appendWithResponse(fluxData, fileOffset, length, appendOptions, null);
    }

    Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        DataLakeFileAppendOptions appendOptions, Context context) {

        if (data == null) {
            return Mono.error(new NullPointerException("'data' cannot be null."));
        }

        appendOptions = appendOptions == null ? new DataLakeFileAppendOptions() : appendOptions;
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(appendOptions.getLeaseId());
        PathHttpHeaders headers = new PathHttpHeaders().setTransactionalContentHash(appendOptions.getContentMd5());
        context = context == null ? Context.NONE : context;
        Long leaseDuration = appendOptions.getLeaseDuration() != null ? Long.valueOf(appendOptions.getLeaseDuration()) : null;

        return this.dataLakeStorage.getPaths().appendDataNoCustomHeadersWithResponseAsync(
            data, fileOffset, null, length, null, appendOptions.getLeaseAction(), leaseDuration,
                appendOptions.getProposedLeaseId(), null, appendOptions.isFlush(), headers, leaseAccessConditions,
                getCpkInfo(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     * <p>By default this method will not overwrite existing data.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long -->
     * <pre>
     * client.flush&#40;position&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Flush data completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @return A reactive response containing the information of the created resource.
     * @deprecated See {@link #flush(long, boolean)} instead.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public Mono<PathInfo> flush(long position) {
        return flush(position, false);
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean -->
     * <pre>
     * boolean overwrite = true;
     * client.flush&#40;position, overwrite&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Flush data completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param overwrite Whether to overwrite, should data exist on the file.
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> flush(long position, boolean overwrite) {
        DataLakeRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return flushWithResponse(position, false, false, null, requestConditions).flatMap(FluxUtil::toMono);
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     * boolean retainUncommittedData = false;
     * boolean close = false;
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.flushWithResponse&#40;position, retainUncommittedData, close, httpHeaders,
     *     requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Flush data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param retainUncommittedData Whether uncommitted data is to be retained after the operation.
     * @param close Whether a file changed event raised indicates completion (true) or modification (false).
     * @param httpHeaders {@link PathHttpHeaders httpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions requestConditions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> flushWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions) {
        DataLakeFileFlushOptions flushOptions = new DataLakeFileFlushOptions()
            .setUncommittedDataRetained(retainUncommittedData)
            .setClose(close)
            .setPathHttpHeaders(httpHeaders)
            .setRequestConditions(requestConditions);

        try {
            return withContext(context -> flushWithResponse(position, flushOptions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-DataLakeFileFlushOptions -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * byte[] contentMd5 = new byte[0]; &#47;&#47; Replace with valid md5
     * boolean retainUncommittedData = false;
     * boolean close = false;
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * Integer leaseDuration = 15;
     *
     * DataLakeFileFlushOptions flushOptions = new DataLakeFileFlushOptions&#40;&#41;
     *     .setUncommittedDataRetained&#40;retainUncommittedData&#41;
     *     .setClose&#40;close&#41;
     *     .setPathHttpHeaders&#40;httpHeaders&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setLeaseAction&#40;LeaseAction.ACQUIRE&#41;
     *     .setLeaseDuration&#40;leaseDuration&#41;
     *     .setProposedLeaseId&#40;leaseId&#41;;
     *
     * client.flushWithResponse&#40;position, flushOptions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Flush data completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-DataLakeFileFlushOptions -->
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param flushOptions {@link DataLakeFileFlushOptions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> flushWithResponse(long position, DataLakeFileFlushOptions flushOptions) {
        try {
            return withContext(context -> flushWithResponse(position, flushOptions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<PathInfo>> flushWithResponse(long position, DataLakeFileFlushOptions flushOptions, Context context) {

        PathHttpHeaders httpHeaders = flushOptions.getPathHttpHeaders() == null
            ? new PathHttpHeaders() : flushOptions.getPathHttpHeaders();

        DataLakeRequestConditions requestConditions = flushOptions.getRequestConditions() == null
            ? new DataLakeRequestConditions() : flushOptions.getRequestConditions();

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        Long leaseDuration = flushOptions.getLeaseDuration() != null ? Long.valueOf(flushOptions.getLeaseDuration()) : null;

        context = context == null ? Context.NONE : context;

        return this.dataLakeStorage.getPaths().flushDataWithResponseAsync(null, position, flushOptions.isUncommittedDataRetained(),
                flushOptions.isClose(), (long) 0, flushOptions.getLeaseAction(), leaseDuration, flushOptions.getProposedLeaseId(),
                null, httpHeaders, lac, mac, getCpkInfo(), context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified(),
                response.getDeserializedHeaders().isXMsRequestServerEncrypted() != null,
                response.getDeserializedHeaders().getXMsEncryptionKeySha256())));
    }

    /**
     * Reads the entire file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.read -->
     * <pre>
     * ByteArrayOutputStream downloadData = new ByteArrayOutputStream&#40;&#41;;
     * client.read&#40;&#41;.subscribe&#40;piece -&gt; &#123;
     *     try &#123;
     *         downloadData.write&#40;piece.array&#40;&#41;&#41;;
     *     &#125; catch &#40;IOException ex&#41; &#123;
     *         throw new UncheckedIOException&#40;ex&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.read -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the file data.
     */
    public Flux<ByteBuffer> read() {
        return readWithResponse(null, null, null, false).flatMapMany(FileReadAsyncResponse::getValue);
    }

    /**
     * Reads a range of bytes from a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean -->
     * <pre>
     * FileRange range = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions options = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     *
     * client.readWithResponse&#40;range, options, null, false&#41;.subscribe&#40;response -&gt; &#123;
     *     ByteArrayOutputStream readData = new ByteArrayOutputStream&#40;&#41;;
     *     response.getValue&#40;&#41;.subscribe&#40;piece -&gt; &#123;
     *         try &#123;
     *             readData.write&#40;piece.array&#40;&#41;&#41;;
     *         &#125; catch &#40;IOException ex&#41; &#123;
     *             throw new UncheckedIOException&#40;ex&#41;;
     *         &#125;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link FileRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @return A reactive response containing the file data.
     */
    public Mono<FileReadAsyncResponse> readWithResponse(FileRange range, DownloadRetryOptions options,
        DataLakeRequestConditions requestConditions, boolean getRangeContentMd5) {
        return blockBlobAsyncClient.downloadWithResponse(Transforms.toBlobRange(range),
                Transforms.toBlobDownloadRetryOptions(options), Transforms.toBlobRequestConditions(requestConditions),
                getRangeContentMd5)
            .map(Transforms::toFileReadAsyncResponse)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String -->
     * <pre>
     * client.readToFile&#40;file&#41;.subscribe&#40;response -&gt; System.out.println&#40;&quot;Completed download to file&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return A reactive response containing the file properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> readToFile(String filePath) {
        return readToFile(filePath, false);
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>If overwrite is set to false, the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default value
     * client.readToFile&#40;file, overwrite&#41;.subscribe&#40;response -&gt; System.out.println&#40;&quot;Completed download to file&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether to overwrite the file, should the file exist.
     * @return A reactive response containing the file properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> readToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return readToFileWithResponse(filePath, null, null, null, null, false, openOptions)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>By default the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown. To override this behavior, provide appropriate
     * {@link OpenOption OpenOptions} </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set -->
     * <pre>
     * FileRange fileRange = new FileRange&#40;1024, 2048L&#41;;
     * DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions&#40;&#41;.setMaxRetryRequests&#40;5&#41;;
     * Set&lt;OpenOption&gt; openOptions = new HashSet&lt;&gt;&#40;Arrays.asList&#40;StandardOpenOption.CREATE_NEW,
     *     StandardOpenOption.WRITE, StandardOpenOption.READ&#41;&#41;; &#47;&#47; Default options
     *
     * client.readToFileWithResponse&#40;file, fileRange, null, downloadRetryOptions, null, false, openOptions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Completed download to file&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link FileRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @return A reactive response containing the file properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathProperties>> readToFileWithResponse(String filePath, FileRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options,
        DataLakeRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions) {
        return blockBlobAsyncClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath)
        .setRange(Transforms.toBlobRange(range)).setParallelTransferOptions(parallelTransferOptions)
        .setDownloadRetryOptions(Transforms.toBlobDownloadRetryOptions(options))
        .setRequestConditions(Transforms.toBlobRequestConditions(requestConditions))
        .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
            .map(response -> new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue(), response)));
    }

    /**
     * Moves the file to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String-String -->
     * <pre>
     * DataLakeFileAsyncClient renamedClient = client.rename&#40;fileSystemName, destinationPath&#41;.block&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String-String -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the new file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> rename(String destinationFileSystem, String destinationPath) {
        return renameWithResponse(destinationFileSystem, destinationPath, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Moves the file to another location within the file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions&#40;&#41;;
     *
     * DataLakeFileAsyncClient newRenamedClient = client.renameWithResponse&#40;fileSystemName, destinationPath,
     *     sourceRequestConditions, destinationRequestConditions&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> renameWithResponse(String destinationFileSystem,
        String destinationPath, DataLakeRequestConditions sourceRequestConditions,
        DataLakeRequestConditions destinationRequestConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationFileSystem, destinationPath,
                sourceRequestConditions, destinationRequestConditions, context))
                .map(response -> new SimpleResponse<>(response, new DataLakeFileAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Queries the entire file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.query#String -->
     * <pre>
     * ByteArrayOutputStream queryData = new ByteArrayOutputStream&#40;&#41;;
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * client.query&#40;expression&#41;.subscribe&#40;piece -&gt; &#123;
     *     try &#123;
     *         queryData.write&#40;piece.array&#40;&#41;&#41;;
     *     &#125; catch &#40;IOException ex&#41; &#123;
     *         throw new UncheckedIOException&#40;ex&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.query#String -->
     *
     * @param expression The query expression.
     * @return A reactive response containing the queried data.
     */
    public Flux<ByteBuffer> query(String expression) {
        try {
            return queryWithResponse(new FileQueryOptions(expression)).flatMapMany(FileQueryAsyncResponse::getValue);
        } catch (RuntimeException ex) {
            return fluxError(LOGGER, ex);
        }
    }

    /**
     * Queries the entire file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.queryWithResponse#FileQueryOptions -->
     * <pre>
     * String expression = &quot;SELECT * from BlobStorage&quot;;
     * FileQueryJsonSerialization input = new FileQueryJsonSerialization&#40;&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;;
     * FileQueryDelimitedSerialization output = new FileQueryDelimitedSerialization&#40;&#41;
     *     .setEscapeChar&#40;'&#92;0'&#41;
     *     .setColumnSeparator&#40;','&#41;
     *     .setRecordSeparator&#40;'&#92;n'&#41;
     *     .setFieldQuote&#40;'&#92;''&#41;
     *     .setHeadersPresent&#40;true&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Consumer&lt;FileQueryError&gt; errorConsumer = System.out::println;
     * Consumer&lt;FileQueryProgress&gt; progressConsumer = progress -&gt; System.out.println&#40;&quot;total file bytes read: &quot;
     *     + progress.getBytesScanned&#40;&#41;&#41;;
     * FileQueryOptions queryOptions = new FileQueryOptions&#40;expression&#41;
     *     .setInputSerialization&#40;input&#41;
     *     .setOutputSerialization&#40;output&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setErrorConsumer&#40;errorConsumer&#41;
     *     .setProgressConsumer&#40;progressConsumer&#41;;
     *
     * client.queryWithResponse&#40;queryOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ByteArrayOutputStream queryData = new ByteArrayOutputStream&#40;&#41;;
     *         response.getValue&#40;&#41;.subscribe&#40;piece -&gt; &#123;
     *             try &#123;
     *                 queryData.write&#40;piece.array&#40;&#41;&#41;;
     *             &#125; catch &#40;IOException ex&#41; &#123;
     *                 throw new UncheckedIOException&#40;ex&#41;;
     *             &#125;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.queryWithResponse#FileQueryOptions -->
     *
     * @param queryOptions {@link FileQueryOptions The query options}
     * @return A reactive response containing the queried data.
     */
    public Mono<FileQueryAsyncResponse> queryWithResponse(FileQueryOptions queryOptions) {
        return blockBlobAsyncClient.queryWithResponse(Transforms.toBlobQueryOptions(queryOptions))
            .map(Transforms::toFileQueryAsyncResponse)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletion#FileScheduleDeletionOptions -->
     * <pre>
     * FileScheduleDeletionOptions options = new FileScheduleDeletionOptions&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;;
     *
     * client.scheduleDeletion&#40;options&#41;
     *     .subscribe&#40;r -&gt; System.out.println&#40;&quot;File deletion has been scheduled&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletion#FileScheduleDeletionOptions -->
     *
     * @param options Schedule deletion parameters.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> scheduleDeletion(FileScheduleDeletionOptions options) {
        return scheduleDeletionWithResponse(options).flatMap(FluxUtil::toMono);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions -->
     * <pre>
     * FileScheduleDeletionOptions options = new FileScheduleDeletionOptions&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;;
     *
     * client.scheduleDeletionWithResponse&#40;options&#41;
     *     .subscribe&#40;r -&gt; System.out.println&#40;&quot;File deletion has been scheduled&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions -->
     *
     * @param options Schedule deletion parameters.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> scheduleDeletionWithResponse(FileScheduleDeletionOptions options) {
        try {
            return withContext(context -> scheduleDeletionWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> scheduleDeletionWithResponse(FileScheduleDeletionOptions options, Context context) {
        PathExpiryOptions pathExpiryOptions;
        context = context == null ? Context.NONE : context;
        String expiresOn = null;
        if (options != null && options.getExpiresOn() != null) {
            pathExpiryOptions = PathExpiryOptions.ABSOLUTE;
            expiresOn = new DateTimeRfc1123(options.getExpiresOn()).toString();
        } else if (options != null && options.getTimeToExpire() != null) {
            if (options.getExpiryRelativeTo() == FileExpirationOffset.CREATION_TIME) {
                pathExpiryOptions = PathExpiryOptions.RELATIVE_TO_CREATION;
            } else {
                pathExpiryOptions = PathExpiryOptions.RELATIVE_TO_NOW;
            }
            expiresOn = Long.toString(options.getTimeToExpire().toMillis());
        } else {
            pathExpiryOptions = PathExpiryOptions.NEVER_EXPIRE;
        }
        return this.blobDataLakeStorage.getPaths().setExpiryWithResponseAsync(
            pathExpiryOptions, null,
            null, expiresOn, context)
            .map(rb -> new SimpleResponse<>(rb, null));
    }

}
