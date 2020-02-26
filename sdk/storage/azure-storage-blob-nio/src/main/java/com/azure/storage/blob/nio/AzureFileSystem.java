// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.nio.implementation.util.Utility;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In the hierarchy of this file system, an {@code AzureFileSystem} corresponds to an Azure Blob Storage account. A
 * file store is represented by a container in the storage account. Each container has one root directory.
 *
 * Closing the file system will not block on outstanding operations. Any operations in progress will be allowed to
 * terminate naturally after the file system is closed, though no further operations may be started after the parent
 * file system is closed.
 * {@inheritDoc}
 */
public final class AzureFileSystem extends FileSystem {
    private final ClientLogger logger = new ClientLogger(AzureFileSystem.class);

    // Configuration constants for blob clients.
    /**
     * Expected type: String
     */
    public static final String AZURE_STORAGE_ACCOUNT_KEY = "AzureStorageAccountKey";

    /**
     * Expected type: String
     */
    public static final String AZURE_STORAGE_SAS_TOKEN = "AzureStorageSasToken";

    /**
     * Expected type: com.azure.core.http.policy.HttpLogLevelDetail
     */
    public static final String AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL = "AzureStorageHttpLogDetailLevel";

    /**
     * Expected type: Integer
     */
    public static final String AZURE_STORAGE_MAX_TRIES = "AzureStorageMaxTries";

    /**
     * Expected type: Integer
     */
    public static final String AZURE_STORAGE_TRY_TIMEOUT = "AzureStorageTryTimeout";

    /**
     * Expected type: Long
     */
    public static final String AZURE_STORAGE_RETRY_DELAY_IN_MS = "AzureStorageRetryDelayInMs";

    /**
     * Expected type: Long
     */
    public static final String AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS = "AzureStorageMaxRetryDelayInMs";

    /**
     * Expected type: com.azure.storage.common.policy.RetryPolicyType
     */
    public static final String AZURE_STORAGE_RETRY_POLICY_TYPE = "AzureStorageRetryPolicyType";

    /**
     * Expected type: String
     */
    public static final String AZURE_STORAGE_SECONDARY_HOST = "AzureStorageSecondaryHost";

    /**
     * Expected type: Integer
     */
    public static final String AZURE_STORAGE_UPLOAD_BLOCK_SIZE = "AzureStorageUploadBlockSize";

    /**
     * Expected type: Integer
     */
    public static final String AZURE_STORAGE_DOWNLOAD_RESUME_RETRIES = "AzureStorageDownloadResumeRetries";

    /**
     * Expected type: Boolean
     */
    public static final String AZURE_STORAGE_USE_HTTPS = "AzureStorageUseHttps";
    public static final String AZURE_STORAGE_HTTP_CLIENT = "AzureStorageHttpClient"; // undocumented; for test.

    public static final String AZURE_STORAGE_FILE_STORES = "AzureStorageFileStores";

    static final String PATH_SEPARATOR = "/";

    private static final String AZURE_STORAGE_BLOB_ENDPOINT_TEMPLATE = "%s://%s.blob.core.windows.net";

    static final Map<Class<? extends FileAttributeView>, String> SUPPORTED_ATTRIBUTE_VIEWS;
    static {
        Map<Class<? extends FileAttributeView>, String> map = new HashMap<>();
        map.put(BasicFileAttributeView.class, "basic");
        map.put(UserDefinedFileAttributeView.class, "user");
        map.put(AzureStorageFileAttributeView.class, "azureStorage");
        SUPPORTED_ATTRIBUTE_VIEWS = Collections.unmodifiableMap(map);
    }

    private final AzureFileSystemProvider parentFileSystemProvider;
    private final BlobServiceClient blobServiceClient;
    private final Integer blockSize;
    private final Integer downloadResumeRetries;
    private final Map<String, FileStore> fileStores;
    private FileStore defaultFileStore;
    private boolean closed;

    AzureFileSystem(AzureFileSystemProvider parentFileSystemProvider, String accountName, Map<String, ?> config)
            throws IOException {
        // A FileSystem should only ever be instantiated by a provider.
        if (Objects.isNull(parentFileSystemProvider)) {
            throw Utility.logError(logger, new IllegalArgumentException("AzureFileSystem cannot be instantiated"
                + " without a parent FileSystemProvider"));
        }
        this.parentFileSystemProvider = parentFileSystemProvider;

        // Read configurations and build client.
        try {
            this.blobServiceClient = this.buildBlobServiceClient(accountName, config);
            this.blockSize = (Integer) config.get(AZURE_STORAGE_UPLOAD_BLOCK_SIZE);
            this.downloadResumeRetries = (Integer) config.get(AZURE_STORAGE_DOWNLOAD_RESUME_RETRIES);

            // Initialize and ensure access to FileStores.
            this.fileStores = this.initializeFileStores(config);
        } catch (RuntimeException e) {
            throw Utility.logError(logger, new IllegalArgumentException("There was an error parsing the configurations "
                + "map. Please ensure all fields are set to a legal value of the correct type."));
        } catch (IOException e) {
            throw Utility.logError(logger,
                new IOException("Initializing FileStores failed. FileSystem could not be opened.", e));
        }

        this.closed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystemProvider provider() {
        return this.parentFileSystemProvider;
    }

    /**
     * Closing the file system will not block on outstanding operations. Any operations in progress will be allowed to
     * terminate naturally after the file system is closed, though no further operations may be started after the
     * parent file system is closed.
     *
     * Once closed, a file system with the same identifier as the one closed may be re-opened.
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
     * Always returns false. It may be the case that the authentication method provided to this file system only
     * supports read operations and hence the file system is implicitly read only in this view, but that does not
     * imply the underlying account/file system is inherently read only. Creating/specifying read only file
     * systems is not supported.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * The separator used in this file system is {@code "/"}.
     *
     * {@inheritDoc}
     */
    @Override
    public String getSeparator() {
        return AzureFileSystem.PATH_SEPARATOR;
    }

    /**
     * The list of root directories corresponds to the list of available file stores and therefore containers specified
     * upon initialization. A root directory always takes the form {@code "&lt;file-store-name&gt;:"}. This list will
     * respect the parameters provided during initialization.
     * <p>
     * If a finite list of containers was provided on start up, this list will not change during the lifetime of this
     * object. If containers are added to the account after initialization, they will be ignored. If a container is
     * deleted or otherwise becomes unavailable, its root directory will still be returned but operations to it will
     * fail. If the file system was set to use all containers in the account, the account will be re-queried and the
     * list may grow or shrink if containers were added or deleted.
     *
     * {@inheritDoc}
     */
    @Override
    public Iterable<Path> getRootDirectories() {
        return fileStores.keySet().stream()
            .map(name -> this.getPath(name + AzurePath.ROOT_DIR_SUFFIX))
            .collect(Collectors.toList());
    }

    /**
     * This list will respect the parameters provided during initialization.
     * <p>
     * If a finite list of containers was provided on start up, this list will not change during the lifetime of this
     * object. If containers are added to the account after initialization, they will be ignored. If a container is
     * deleted or otherwise becomes unavailable, its root directory will still be returned but operations to it will
     * fail. If the file system was set to use all containers in the account, the account will be re-queried and the
     * list may grow or shrink if containers were added or deleted.
     *
     * {@inheritDoc}
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        return this.fileStores.values();
    }

    /**
     * This file system supports the following views:
     * <ul>
     *     <li>{@link java.nio.file.attribute.BasicFileAttributeView}</li>
     *     <li>{@link java.nio.file.attribute.UserDefinedFileAttributeView}</li>
     *     <li>{@link AzureStorageFileAttributeView}</li>
     * </ul>
     *
     * {@inheritDoc}
     */
    @Override
    public Set<String> supportedFileAttributeViews() {
        return new HashSet<>(SUPPORTED_ATTRIBUTE_VIEWS.values());
    }

    /**
     * Each name element will be {@code String}-joined to the other elements by this file system's path separator.
     * Naming conventions and allowed characters are as
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/Naming-and-Referencing-Containers--Blobs--and-Metadata">defined</a>
     * by the Azure Blob Storage service. The root component is interpreted as the container name and all name elements
     * are interpreted as a part of the blob name. The character {@code ':'} is only allowed in the root component and
     * must be the last character of the root component.
     *
     * {@inheritDoc}
     */
    @Override
    public Path getPath(String s, String... strings) {
        return new AzurePath(this, s, strings);
    }

    /**
     * Unsupported.
     *
     * {@inheritDoc}
     */
    @Override
    public PathMatcher getPathMatcher(String s) {
        throw Utility.logError(logger, new UnsupportedOperationException());
    }

    /**
     * Unsupported.
     * {@inheritDoc}
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw Utility.logError(logger, new UnsupportedOperationException());
    }

    /**
     * Unsupported.
     *
     * {@inheritDoc}
     */
    @Override
    public WatchService newWatchService() throws IOException {
        throw Utility.logError(logger, new UnsupportedOperationException());
    }

    String getFileSystemName() {
        return this.blobServiceClient.getAccountName();
    }

    BlobServiceClient getBlobServiceClient() {
        return this.blobServiceClient;
    }

    private BlobServiceClient buildBlobServiceClient(String accountName, Map<String, ?> config) {
        // Build the endpoint.
        String scheme = !config.containsKey(AZURE_STORAGE_USE_HTTPS)
                || (Boolean) config.get(AZURE_STORAGE_USE_HTTPS)
                ? "https" : "http";
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
                .endpoint(String.format(AZURE_STORAGE_BLOB_ENDPOINT_TEMPLATE, scheme, accountName));

        // Set the credentials.
        if (config.containsKey(AZURE_STORAGE_ACCOUNT_KEY)) {
            builder.credential(new StorageSharedKeyCredential(accountName,
                    (String) config.get(AZURE_STORAGE_ACCOUNT_KEY)));
        } else if (config.containsKey(AZURE_STORAGE_SAS_TOKEN)) {
            builder.sasToken((String) config.get(AZURE_STORAGE_SAS_TOKEN));
        } else {
            throw Utility.logError(logger, new IllegalArgumentException(String.format("No credentials were provided. "
                    + "Please specify one of the following when constructing an AzureFileSystem: %s, %s.",
                AZURE_STORAGE_ACCOUNT_KEY, AZURE_STORAGE_SAS_TOKEN)));
        }

        // Configure options and client.
        builder.httpLogOptions(BlobServiceClientBuilder.getDefaultHttpLogOptions()
            .setLogLevel((HttpLogDetailLevel) config.get(AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL)));

        RequestRetryOptions retryOptions = new RequestRetryOptions(
            (RetryPolicyType) config.get(AZURE_STORAGE_RETRY_POLICY_TYPE),
            (Integer) config.get(AZURE_STORAGE_MAX_TRIES),
            (Integer) config.get(AZURE_STORAGE_TRY_TIMEOUT),
            (Long) config.get(AZURE_STORAGE_RETRY_DELAY_IN_MS),
            (Long) config.get(AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS),
            (String) config.get(AZURE_STORAGE_SECONDARY_HOST));
        builder.retryOptions(retryOptions);

        builder.httpClient((HttpClient) config.get(AZURE_STORAGE_HTTP_CLIENT));

        return builder.buildClient();
    }

    private Map<String, FileStore> initializeFileStores(Map<String, ?> config) throws IOException {
        String fileStoreNames = (String) config.get(AZURE_STORAGE_FILE_STORES);
        if (CoreUtils.isNullOrEmpty(fileStoreNames)) {
            throw Utility.logError(logger, new IllegalArgumentException("The list of FileStores cannot be null."));
        }

        Map<String, FileStore> fileStores = new HashMap<>();
        for (String fileStoreName : fileStoreNames.split(",")) {
            FileStore fs = new AzureFileStore(this, fileStoreName);
            if (this.defaultFileStore == null) {
                this.defaultFileStore = fs;
            }
            fileStores.put(fileStoreName, fs);
        }
        return fileStores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AzureFileSystem that = (AzureFileSystem) o;
        return Objects.equals(this.getFileSystemName(), that.getFileSystemName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFileSystemName());
    }

    Path getDefaultDirectory() {
        return this.getPath(this.defaultFileStore.name() + AzurePath.ROOT_DIR_SUFFIX);
    }

    FileStore getFileStore(String name) throws IOException {
        FileStore store = this.fileStores.get(name);
        if (store == null) {
            throw Utility.logError(logger, new IOException("Invalid file store: " + name));
        }
        return store;
    }
}
