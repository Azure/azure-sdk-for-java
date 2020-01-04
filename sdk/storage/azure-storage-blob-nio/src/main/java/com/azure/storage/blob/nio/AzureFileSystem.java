// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 */
public class AzureFileSystem extends FileSystem {
    public static final String AZURE_STORAGE_NIO_ACCOUNT_KEY = "AzureStorageNioAccountKey";
    public static final String AZURE_STORAGE_NIO_SAS_TOKEN = "AzureStorageNioSasToken";
    public static final String AZURE_STORAGE_NIO_AAD_TOKEN = "AzureStorageNioAadToken";
    public static final String AZURE_STORAGE_NIO_HTTP_LOG_DETAIL_LEVEL = "AzureStorageNioHttpLogDetailLevel";
    public static final String AZURE_STORAGE_NIO_MAX_TRIES = "AzureStorageNioMaxTries";
    public static final String AZURE_STORAGE_NIO_TRY_TIMEOUT = "AzureStorageNioTryTimeout";
    public static final String AZURE_STORAGE_NIO_RETRY_DELAY_IN_MS = "AzureStorageNioRetryDelayInMs";
    public static final String AZURE_STORAGE_NIO_MAX_RETRY_DELAY_IN_MS = "AzureStorageNioMaxRetryDelayInMs";
    public static final String AZURE_STORAGE_NIO_RETRY_POLICY_TYPE = "AzureStorageNioRetryPolicyType";
    public static final String AZURE_STORAGE_NIO_SECONDARY_HOST = "AzureStorageNioSecondaryHost";
    public static final String AZURE_STORAGE_NIO_BLOCK_SIZE = "AzureStorageNioBlockSize";
    public static final String AZURE_STORAGE_NIO_DOWNLOAD_RESUME_RETRIES = "AzureStorageNioDownloadResumeRetries";
    public static final String AZURE_STORAGE_NIO_USE_HTTP = "AzureStorageNioUseHttp";
    public static final String AZURE_STORAGE_NIO_FILE_STORES = "AzureStorageNioFileStores";

    private static final String AZURE_STORAGE_ENDPOINT_TEMPLATE = "%s://%s.blob.core.windows.net";

    private final AzureFileSystemProvider parentFileSystemProvider;
    private final BlobServiceClient blobServiceClient;
    private final Integer blockSize;
    private final Integer downloadResumeRetries;
    private final Map<String, AzureFileStore> fileStores;
    private boolean closed;

    AzureFileSystem(AzureFileSystemProvider parentFileSystemProvider, String accountName, Map<String, ?> config)
            throws IOException {
        this.parentFileSystemProvider = parentFileSystemProvider;
        this.blobServiceClient = this.buildBlobServiceClient(accountName, config);
        this.blockSize = Integer.valueOf((String)config.get(AZURE_STORAGE_NIO_BLOCK_SIZE));
        this.downloadResumeRetries = Integer.valueOf((String)config.get(AZURE_STORAGE_NIO_DOWNLOAD_RESUME_RETRIES));
        this.fileStores = this.initializeFileStores(config);
        this.closed = false;

        try {
            this.blobServiceClient.getProperties();
        } catch (Exception e) {
            throw new IOException("Could not instantiate FileSystem. Initial connection failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystemProvider provider() {
        return this.parentFileSystemProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.closed = true;
        this.parentFileSystemProvider.closeFileSystem(this.getFileSystemName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSeparator() {
        return "/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPath(String s, String... strings) {
        return new AzurePath(this, s, strings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathMatcher getPathMatcher(String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    String getFileSystemName() {
        return this.blobServiceClient.getAccountName();
    }

    BlobServiceClient getBlobServiceClient() {
        return this.getBlobServiceClient();
    }

    private BlobServiceClient buildBlobServiceClient(String accountName, Map<String,?> config) {
        String scheme = config.containsKey(AZURE_STORAGE_NIO_USE_HTTP)
                && config.get(AZURE_STORAGE_NIO_USE_HTTP).equals("true")
                ? "http" : "https";
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
                .endpoint(String.format(AZURE_STORAGE_ENDPOINT_TEMPLATE, scheme, accountName));

        if (config.containsKey(AZURE_STORAGE_NIO_ACCOUNT_KEY)) {
            builder.credential(new StorageSharedKeyCredential(accountName,
                    (String)config.get(AZURE_STORAGE_NIO_ACCOUNT_KEY)));
        }
        else if (config.containsKey(AZURE_STORAGE_NIO_SAS_TOKEN)) {
            builder.sasToken((String) config.get(AZURE_STORAGE_NIO_SAS_TOKEN));
        }
        //TODO: Expiry time?
        /*else if (config.containsKey(AZURE_STORAGE_NIO_AAD_TOKEN)) {
            builder.credential(context ->  Mono.just(new AccessToken((String)config.get(AZURE_STORAGE_NIO_AAD_TOKEN),
                    expireTime));
        }*/
        else {
            throw new IllegalArgumentException(String.format("No credentials were provided. Please specify one of the" +
                    " following when constructing an AzureFileSystem: %s, %s, %s.", AZURE_STORAGE_NIO_ACCOUNT_KEY,
                    AZURE_STORAGE_NIO_SAS_TOKEN, AZURE_STORAGE_NIO_AAD_TOKEN));
        }

        builder.httpLogOptions(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.valueOf((String)config.get(AZURE_STORAGE_NIO_HTTP_LOG_DETAIL_LEVEL))));

        RequestRetryOptions retryOptions = new RequestRetryOptions(
                RetryPolicyType.valueOf((String)config.get(AZURE_STORAGE_NIO_RETRY_POLICY_TYPE)),
                Integer.valueOf((String)config.get(AZURE_STORAGE_NIO_MAX_TRIES)),
                Integer.valueOf((String)config.get(AZURE_STORAGE_NIO_TRY_TIMEOUT)),
                Long.valueOf((String)config.get(AZURE_STORAGE_NIO_RETRY_DELAY_IN_MS)),
                Long.valueOf((String)config.get(AZURE_STORAGE_NIO_MAX_RETRY_DELAY_IN_MS)),
                (String)config.get(AZURE_STORAGE_NIO_SECONDARY_HOST));
        builder.retryOptions(retryOptions);

        return builder.buildClient();
    }

    private Map<String, AzureFileStore> initializeFileStores(Map<String, ?> config) throws IOException {
        String fileStoreNames = (String)config.get(AZURE_STORAGE_NIO_FILE_STORES);
        if (CoreUtils.isNullOrEmpty(fileStoreNames)) {
            throw new IllegalArgumentException("The list of FileStores cannot be null.");
        }

        Map<String, AzureFileStore> fileStores = Collections.emptySortedMap();
        for (String fileStoreName : fileStoreNames.split(",")) {
            fileStores.put(fileStoreName, new AzureFileStore(this, fileStoreName));
        }

        return fileStores;
    }
}
