// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.nio.implementation.util.Utility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The {@code AzureFileSystemProvider} is Azure Storage's implementation of the nio interface on top of Azure Blob
 * Storage.
 * <p>
 * Particular care should be taken when working with a remote storage service. This implementation makes no guarantees
 * on behavior or state should other processes operate on the same data concurrently; file systems from this provider
 * will assume they have exclusive access to their data and will behave without regard for potential of interfering
 * applications. Moreover, remote file stores introduce higher latencies. Therefore, particular care must be taken when
 * managing concurrency: race conditions are more likely to manifest and network failures occur more frequently than
 * disk failures. These and other such distributed application scenarios must be considered when working with this file
 * system. While the {@code AzureFileSystem} will ensure it takes appropriate steps towards robustness and reliability,
 * the application developer must design around these failure scenarios and have fallback and retry options available.
 * <p>
 * The Azure Blob Storage service backing these APIs is not a true FileSystem, nor is it the goal of this implementation
 * to force Azure Blob Storage to act like a full-fledged file system. Some APIs and scenarios will remain unsupported
 * indefinitely until they may be sensibly implemented. Other APIs may experience lower performance than is expected
 * because of the number of network requests needed to ensure correctness.
 * <p>
 * The scheme for this provider is {@code "azb"}, and the format of the URI to identify an {@code AzureFileSystem} is
 * {@code "azb://?account=&lt;accountName&gt;"}. The name of the Storage account is used to uniquely identify the file
 * system.
 * <p>
 * An {@link AzureFileSystem} is backed by an account. An {@link AzureFileStore} is backed by a container. Any number of
 * containers may be specified as file stores upon creation of the file system. When a file system is created,
 * it will try to retrieve the properties of each container to ensure connection to the account. If any of the
 * containers does not exist, it will be created. Failure to access or create containers as necessary will result in
 * an exception and failure to create the file system. Any data existing in the containers will be preserved and
 * accessible via the file system, though customers should be aware that it must be in a format understandable by
 * the types in this package or behavior will be undefined.
 * <p>
 * {@link #newFileSystem(URI, Map)} will check for the following keys in the configuration map and expect the named
 * types. Any entries not listed here will be ignored. Note that {@link AzureFileSystem} has public constants defined
 * for each of the keys for convenience.
 * <ul>
 *     <li>{@code AzureStorageAccountKey:}{@link String}</li>
 *     <li>{@code AzureStorageSasToken:}{@link String}</li>
 *     <li>{@code AzureStorageHttpLogDetailLevel:}{@link com.azure.core.http.policy.HttpLogDetailLevel}</li>
 *     <li>{@code AzureStorageMaxTries:}{@link Integer}</li>
 *     <li>{@code AzureStorageTryTimeout:}{@link Integer}</li>
 *     <li>{@code AzureStorageRetryDelayInMs:}{@link Long}</li>
 *     <li>{@code AzureStorageMaxRetryDelayInMs:}{@link Long}</li>
 *     <li>{@code AzureStorageRetryPolicyType:}{@link com.azure.storage.common.policy.RetryPolicyType}</li>
 *     <li>{@code AzureStorageSecondaryHost:}{@link String}</li>
 *     <li>{@code AzureStorageSecondaryHost:}{@link Integer}</li>
 *     <li>{@code AzureStorageBlockSize:}{@link Integer}</li>
 *     <li>{@code AzureStorageDownloadResumeRetries:}{@link Integer}</li>
 *     <li>{@code AzureStorageUseHttps:}{@link Boolean}</li>
 *     <li>{@code AzureStorageFileStores:}{@link Iterable}&lt;String&gt;}</li>
 * </ul>
 * <p>
 * Either an account key or a sas token must be specified. If both are provided, the account key will be preferred. If
 * a sas token is specified, the customer must take care that it has appropriate permissions to perform the actions
 * demanded of the file system in a given workflow, including the initial connection check specified above. Furthermore,
 * it must have an expiry time that lasts at least until the file system is closed as there is no token refresh offered
 * at this time. The same token will be applied to all containers.
 * <p>
 * An iterable of file stores must also be provided; each entry should simply be the name of a container. The first
 * container listed will be considered the default file store and the root directory of which will be the file system's
 * default directory. All other values listed are used to configure the underlying
 * {@link com.azure.storage.blob.BlobServiceClient}. Please refer to that type for more information on these values.
 *
 * @see FileSystemProvider
 */
public final class AzureFileSystemProvider extends FileSystemProvider {
    private final ClientLogger logger = new ClientLogger(AzureFileSystemProvider.class);

    private static final String ACCOUNT_QUERY_KEY = "account";

    private final ConcurrentMap<String, FileSystem> openFileSystems;


    // Specs require a public zero argument constructor.
    /**
     * Creates an AzureFileSystemProvider.
     */
    public AzureFileSystemProvider() {
        this.openFileSystems = new ConcurrentHashMap<>();
    }

    /**
     * Returns {@code "azb".}
     */
    @Override
    public String getScheme() {
        return "azb";
    }

    /**
     * The format of a {@code URI} identifying a file system is {@code "azb://?account=&lt;accountName&gt;"}.
     * <p>
     * Once closed, a file system with the same identifier may be reopened.
     * {@inheritDoc}
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> config) throws IOException {
        String accountName = extractAccountName(uri);

        if (this.openFileSystems.containsKey(accountName)) {
            throw Utility.logError(this.logger, new FileSystemAlreadyExistsException("Name: " + accountName));
        }

        AzureFileSystem afs = new AzureFileSystem(this, accountName, config);
        this.openFileSystems.put(accountName, afs);

        return afs;
    }

    /**
     * The format of a {@code URI} identifying an file system is {@code "azb://?account=&lt;accountName&gt;"}.
     * <p>
     * Trying to retrieve a closed file system will throw a {@link FileSystemNotFoundException}. Once closed, a
     * file system with the same identifier may be reopened.
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem(URI uri) {
        String accountName = extractAccountName(uri);
        if (!this.openFileSystems.containsKey(accountName)) {
            throw Utility.logError(this.logger, new FileSystemNotFoundException("Name: " + accountName));
        }
        return this.openFileSystems.get(accountName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set,
            FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter)
        throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Path path) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSameFile(Path path, Path path1) throws IOException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions)
        throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }

    void closeFileSystem(String fileSystemName) {
        this.openFileSystems.remove(fileSystemName);
    }

    private String extractAccountName(URI uri) {
        if (!uri.getScheme().equals(this.getScheme())) {
            throw Utility.logError(this.logger, new IllegalArgumentException(
                "URI scheme does not match this provider"));
        }
        if (CoreUtils.isNullOrEmpty(uri.getQuery())) {
            throw Utility.logError(this.logger, new IllegalArgumentException("URI does not contain a query "
                + "component. FileSystems require a URI of the format \"azb://?account=<account_name>\"."));
        }

        String accountName = Flux.fromArray(uri.getQuery().split("&"))
                .filter(s -> s.startsWith(ACCOUNT_QUERY_KEY + "="))
                .switchIfEmpty(Mono.error(Utility.logError(this.logger, new IllegalArgumentException(
                        "URI does not contain an \"" + ACCOUNT_QUERY_KEY + "=\" parameter. FileSystems require a URI "
                            + "of the format \"azb://?account=<account_name>\""))))
                .map(s -> s.substring(ACCOUNT_QUERY_KEY.length() + 1))
                .blockLast();

        if (CoreUtils.isNullOrEmpty(accountName)) {
            throw Utility.logError(logger, new IllegalArgumentException("No account name provided in URI query."));
        }

        return accountName;
    }
}
