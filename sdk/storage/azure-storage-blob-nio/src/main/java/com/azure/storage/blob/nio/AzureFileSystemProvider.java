// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobErrorCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CACHE_CONTROL = "Cache-Control";

    private static final String ACCOUNT_QUERY_KEY = "account";
    private static final int COPY_TIMEOUT_SECONDS = 30;

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
            throw LoggingUtility.logError(this.logger, new FileSystemAlreadyExistsException("Name: " + accountName));
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
            throw LoggingUtility.logError(this.logger, new FileSystemNotFoundException("Name: " + accountName));
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
     * Returns an {@link AzureDirectoryStream} for iterating over the contents of a directory.
     *
     * {@inheritDoc}
     * @throws IllegalArgumentException If the path type is not an instance of {@link AzurePath}.
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter)
        throws IOException {
        if (!(path instanceof AzurePath)) {
            throw LoggingUtility.logError(logger, new IllegalArgumentException("This provider cannot operate on "
                + "subtypes of Path other than AzurePath"));
        }

        /*
        Ensure the path is a directory. Note that roots are always directories. The case of an invalid root will be
        caught in instatiating the stream below.

        Possible optimization later is to save the result of the list call to use as the first list call inside the
        stream rather than a list call for checking the status and a list call for listing.
         */
        if (!((AzurePath) path).isRoot() && !(new AzureResource(path).checkDirectoryExists())) {
            throw LoggingUtility.logError(logger, new NotDirectoryException(path.toString()));
        }

        return new AzureDirectoryStream((AzurePath) path, filter);
    }

    /**
     * Creates a new directory at the specified path.
     * <p>
     * The existence of a directory in the {@code AzureFileSystem} is defined on two levels. <i>Weak existence</i> is
     * defined by the presence of a non-zero number of blobs prefixed with the directory's path. This concept is also
     * known as a  <i>virtual directory</i> and enables the file system to work with containers that were pre-loaded
     * with data by another source but need to be accessed by this file system. <i>Strong existence</i> is defined as
     * the presence of an actual storage resource at the given path, which in the case of directories, is a zero-length
     * blob whose name is the directory path with a particular metadata field indicating the blob's status as a
     * directory. This is also known as a <i>concrete directory</i>. Directories created by this file system will
     * strongly exist. Operations targeting directories themselves as the object (e.g. setting properties) will target
     * marker blobs underlying concrete directories. Other operations (e.g. listing) will operate on the blob-name
     * prefix.
     * <p>
     * This method fulfills the nio contract of: "The check for the existence of the file and the creation of the
     * directory if it does not exist are a single operation that is atomic with respect to all other filesystem
     * activities that might affect the directory." More specifically, this method will atomically check for <i>strong
     * existence</i> of another file or directory at the given path and fail if one is present. On the other hand, we
     * only check for <i>weak existence</i> of the parent to determine if the given path is valid. Additionally, the
     * action of checking whether the parent exists, is <i>not</i> atomic with the creation of the directory. Note that
     * while it is possible that the parent may be deleted between when the parent is determined to exist and the
     * creation of the child, the creation of the child will always ensure the existence of a virtual parent, so the
     * child will never be left floating and unreachable. The different checks on parent and child is due to limitations
     * in the Storage service API.
     * <p>
     * There may be some unintuitive behavior when working with directories in this file system, particularly virtual
     * directories(usually those not created by this file system). A virtual directory will disappear as soon as all its
     * children have been deleted. Furthermore, if a directory with the given path weakly exists at the time of calling
     * this method, this method will still return success and create a concrete directory at the target location.
     * In other words, it is possible to "double create" a directory if it first weakly exists and then is strongly
     * created. This is both because it is impossible to atomically check if a virtual directory exists while creating a
     * concrete directory and because such behavior will have minimal side effects--no files will be overwritten and the
     * directory will still be available for writing as intended, though it may not be empty.
     * <p>
     * This method will attempt to extract standard HTTP content headers from the list of file attributes to set them
     * as blob headers. All other attributes will be set as blob metadata. The value of every attribute will be
     * converted to a {@code String} with the exception of the Content-MD5 attribute which expects a {@code byte[]}.
     * When extracting the content headers, the following strings will be used for comparison (constants for these
     * values can be found on this type):
     * <ul>
     *     <li>{@code Content-Type}</li>
     *     <li>{@code Content-Disposition}</li>
     *     <li>{@code Content-Language}</li>
     *     <li>{@code Content-Encoding}</li>
     *     <li>{@code Content-MD5}</li>
     *     <li>{@code Cache-Control}</li>
     * </ul>
     * Note that these properties also have a particular semantic in that if one is specified, all are updated. In other
     * words, if any of the above is set, all those that are not set will be cleared. See the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a> for more
     * information.
     *
     * {@inheritDoc}
     * @throws IllegalArgumentException If the path type is not an instance of {@link AzurePath}.
     */
    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {
        fileAttributes = fileAttributes == null ? new FileAttribute<?>[0] : fileAttributes;

        // Get the destination for the directory. Will throw if path is a root.
        AzureResource azureResource = new AzureResource(path);

        // Check if parent exists. If it does, atomically check if a file already exists and create a new dir if not.
        if (azureResource.checkParentDirectoryExists()) {
            try {
                azureResource.setFileAttributes(Arrays.asList(fileAttributes))
                    .putDirectoryBlob(new BlobRequestConditions().setIfNoneMatch("*"));
            } catch (BlobStorageException e) {
                if (e.getStatusCode() == HttpURLConnection.HTTP_CONFLICT
                    && e.getErrorCode().equals(BlobErrorCode.BLOB_ALREADY_EXISTS)) {
                    throw LoggingUtility.logError(logger,
                        new FileAlreadyExistsException(azureResource.getPath().toString()));
                } else {
                    throw LoggingUtility.logError(logger,
                        new IOException("An error occurred when creating the directory", e));
                }
            }
        } else {
            throw LoggingUtility.logError(logger, new IOException("Parent directory does not exist for path: "
                + azureResource.getPath().toString()));
        }
    }

    /**
     * Deletes the specified resource.
     * <p>
     * This method is not atomic. It is possible to delete a file in use by another process,
     * and doing so will not immediately invalidate any channels open to that file--they will simply start to fail.
     * Root directories cannot be deleted even when empty.
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException If the path type is not an instance of {@link AzurePath}.
     */
    @Override
    public void delete(Path path) throws IOException {
        // Basic validation. Must be an AzurePath. Cannot be a root.
        AzureResource azureResource = new AzureResource(path);

        // Check directory status--possibly throw DirectoryNotEmpty or NoSuchFile.
        DirectoryStatus dirStatus = azureResource.checkDirStatus();
        if (dirStatus.equals(DirectoryStatus.DOES_NOT_EXIST)) {
            throw LoggingUtility.logError(logger, new NoSuchFileException(path.toString()));
        }
        if (dirStatus.equals(DirectoryStatus.NOT_EMPTY)) {
            throw LoggingUtility.logError(logger, new DirectoryNotEmptyException(path.toString()));
        }

        // After all validation has completed, delete the resource.
        try {
            azureResource.getBlobClient().delete();
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw LoggingUtility.logError(logger, new NoSuchFileException(path.toString()));
            }
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    /**
     * Copies the resource at the source location to the destination.
     * <p>
     * This method is not atomic. More specifically, the checks necessary to validate the
     * inputs and state of the file system are not atomic with the actual copying of data. If the copy is triggered,
     * the copy itself is atomic and only a complete copy will ever be left at the destination.
     *
     * In addition to those in the nio javadocs, this method has the following requirements for successful completion.
     * {@link StandardCopyOption#COPY_ATTRIBUTES} must be passed as it is impossible not to copy blob properties;
     * if this option is not passed, an {@link UnsupportedOperationException} will be thrown. Neither the source nor the
     * destination can be a root directory; if either is a root directory, an {@link IllegalArgumentException} will be
     * thrown. The parent directory of the destination must at least weakly exist; if it does not, an
     * {@link IOException} will be thrown. The only supported option other than
     * {@link StandardCopyOption#COPY_ATTRIBUTES} is {@link StandardCopyOption#REPLACE_EXISTING}; the presence of any
     * other option will result in an {@link UnsupportedOperationException}.
     *
     * This method supports both virtual and concrete directories as both the source and destination. Unlike when
     * creating a directory, the existence of a virtual directory at the destination will cause this operation to fail.
     * This is in order to prevent the possibility of overwriting a non-empty virtual directory with a file. Still, as
     * mentioned above, this check is not atomic with the creation of the resultant directory.
     *
     * {@inheritDoc}
     * @throws IllegalArgumentException If the path type is not an instance of {@link AzurePath}.
     * @see #createDirectory(Path, FileAttribute[]) for more information about directory existence.
     */
    @Override
    public void copy(Path source, Path destination, CopyOption... copyOptions) throws IOException {
        // If paths point to the same file, operation is a no-op.
        if (source.equals(destination)) {
            return;
        }

        // Read and validate options.
        // Remove accepted options as we find them. Anything left we don't support.
        boolean replaceExisting = false;
        List<CopyOption> optionsList = new ArrayList<>(Arrays.asList(copyOptions));
        if (!optionsList.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
            throw LoggingUtility.logError(logger, new UnsupportedOperationException(
                "StandardCopyOption.COPY_ATTRIBUTES must be specified as the service will always copy "
                    + "file attributes."));
        }
        optionsList.remove(StandardCopyOption.COPY_ATTRIBUTES);
        if (optionsList.contains(StandardCopyOption.REPLACE_EXISTING)) {
            replaceExisting = true;
            optionsList.remove(StandardCopyOption.REPLACE_EXISTING);
        }
        if (!optionsList.isEmpty()) {
            throw LoggingUtility.logError(logger, new UnsupportedOperationException("Unsupported copy option found. "
                + "Only StandardCopyOption.COPY_ATTRIBUTES and StandareCopyOption.REPLACE_EXISTING are supported."));
        }

        // Validate paths. Build resources.
        // Copying a root directory or attempting to create/overwrite a root directory is illegal.
        AzureResource sourceRes = new AzureResource(source);
        AzureResource destinationRes = new AzureResource(destination);

        // Check destination is not a directory with children.
        DirectoryStatus destinationStatus = destinationRes.checkDirStatus();
        if (destinationStatus.equals(DirectoryStatus.NOT_EMPTY)) {
            throw LoggingUtility.logError(logger, new DirectoryNotEmptyException(destination.toString()));
        }

        /*
        Set request conditions if we should not overwrite. We can error out here if we know something already exists,
        but we will also create request conditions as a safeguard against overwriting something that was created
        between our check and put.
         */
        BlobRequestConditions requestConditions = null;
        if (!replaceExisting) {
            if (!destinationStatus.equals(DirectoryStatus.DOES_NOT_EXIST)) {
                throw LoggingUtility.logError(logger,
                    new FileAlreadyExistsException(destinationRes.getPath().toString()));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
        }

        /*
        More path validation

        Check that the parent for the destination exists. We only need to perform this check if there is nothing
        currently at the destination, for if the destination exists, its parent at least weakly exists and we
        can skip a service call.
         */
        if (destinationStatus.equals(DirectoryStatus.DOES_NOT_EXIST) && !destinationRes.checkParentDirectoryExists()) {
            throw LoggingUtility.logError(logger, new IOException("Parent directory of destination location does not "
                + "exist. The destination path is therefore invalid. Destination: "
                + destinationRes.getPath().toString()));
        }

        /*
        Try to copy the resource at the source path.

        There is an optimization here where we try to do the copy first and only check for a virtual directory if
        there's a 404. In the cases of files and concrete directories, this only requires one request. For virtual
        directories, however, this requires three requests: failed copy, check status, create directory. Depending on
        customer scenarios and how many virtual directories they copy, it could be better to check the directory status
        first and then do a copy or createDir, which would always be two requests for all resource types.
         */
        try {
            SyncPoller<BlobCopyInfo, Void> pollResponse =
                destinationRes.getBlobClient().beginCopy(sourceRes.getBlobClient().getBlobUrl(), null, null, null,
                    null, requestConditions, null);
            pollResponse.waitForCompletion(Duration.ofSeconds(COPY_TIMEOUT_SECONDS));
        } catch (BlobStorageException e) {
            // If the source was not found, it could be because it's a virtual directory. Check the status.
            // If a non-dir resource existed, it would have been copied above. This check is therefore sufficient.
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)
                && !sourceRes.checkDirStatus().equals(DirectoryStatus.DOES_NOT_EXIST)) {
                /*
                We already checked that the parent exists and validated the paths above, so we can put the blob
                directly.
                 */
                destinationRes.putDirectoryBlob(requestConditions);
            } else {
                throw LoggingUtility.logError(logger, new IOException(e));
            }
        } catch (RuntimeException e) { // To better log possible timeout from poller.
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    // Used for checking the status of the root directory. To be implemented later when needed.
    /*int checkRootDirStatus(BlobContainerClient rootClient) {

    }*/

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
            throw LoggingUtility.logError(this.logger, new IllegalArgumentException(
                "URI scheme does not match this provider"));
        }
        if (CoreUtils.isNullOrEmpty(uri.getQuery())) {
            throw LoggingUtility.logError(this.logger, new IllegalArgumentException("URI does not contain a query "
                + "component. FileSystems require a URI of the format \"azb://?account=<account_name>\"."));
        }

        String accountName = Flux.fromArray(uri.getQuery().split("&"))
                .filter(s -> s.startsWith(ACCOUNT_QUERY_KEY + "="))
                .switchIfEmpty(Mono.error(LoggingUtility.logError(this.logger, new IllegalArgumentException(
                        "URI does not contain an \"" + ACCOUNT_QUERY_KEY + "=\" parameter. FileSystems require a URI "
                            + "of the format \"azb://?account=<account_name>\""))))
                .map(s -> s.substring(ACCOUNT_QUERY_KEY.length() + 1))
                .blockLast();

        if (CoreUtils.isNullOrEmpty(accountName)) {
            throw LoggingUtility.logError(logger, new IllegalArgumentException("No account name provided in URI"
                + " query."));
        }

        return accountName;
    }
}
