// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.implementation.Constants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This type is meant to be a logical grouping of operations and data associated with an azure resource. It is NOT
 * intended to serve as a local cache for any data related to remote resources. It is agnostic to whether the resource
 * is a directory or a file and will not perform any validation of the resource type, though root directories are not
 * supported as they are backed by containers and do not support many file system apis.
 *
 * It also serves as the interface to Storage clients. Any operation that needs to use a client should first build an
 * AzureResource using a path and then use the getter to access the client.
 */
final class AzureResource {
    private static final ClientLogger LOGGER = new ClientLogger(AzureResource.class);

    static final String DIR_METADATA_MARKER = Constants.HeaderConstants.DIRECTORY_METADATA_KEY;

    private final AzurePath path;
    private final BlobClient blobClient;

    // The following are not kept consistent with the service. They are only held here between parsing and putting.
    private BlobHttpHeaders blobHeaders;
    private Map<String, String> blobMetadata;

    AzureResource(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        this.path = validatePathInstanceType(path);
        this.validateNotRoot();
        this.blobClient = this.path.toBlobClient();
    }

    /**
     * Checks for the existence of the parent of the given path. We do not check for the actual marker blob as parents
     * need only weakly exist.
     *
     * If the parent is a root (container), it will be assumed to exist, so it must be validated elsewhere that the
     * container is a legitimate root within this file system.
     */
    boolean checkParentDirectoryExists() throws IOException {
        /*
        If the parent is just the root (or null, which means the parent is implicitly the default directory which is a
        root), that means we are checking a container, which is always considered to exist. Otherwise, perform normal
        existence check.
         */
        Path parent = this.path.getParent();
        return (parent == null || parent.equals(path.getRoot()))
            || new AzureResource(this.path.getParent()).checkDirectoryExists();
    }

    /**
     * Checks whether a directory exists by either being empty or having children.
     */
    boolean checkDirectoryExists() throws IOException {
        DirectoryStatus dirStatus = this.checkDirStatus();
        return dirStatus.equals(DirectoryStatus.EMPTY) || dirStatus.equals(DirectoryStatus.NOT_EMPTY);
    }

    /*
    This method will check specifically whether there is a virtual directory at this location. It must be known before
    that there is no file present at the destination.
     */
    boolean checkVirtualDirectoryExists() throws IOException {
        DirectoryStatus dirStatus = this.checkDirStatus(false);
        return dirStatus.equals(DirectoryStatus.NOT_EMPTY); // Virtual directories cannot be empty
    }

    /**
     * This method will check if a directory is extant and/or empty and accommodates virtual directories. This method
     * will not check the status of root directories.
     */
    DirectoryStatus checkDirStatus() throws IOException {
        if (this.blobClient == null) {
            throw LoggingUtility.logError(LOGGER, new IllegalArgumentException("The blob client was null."));
        }

        /*
         * Do a get properties first on the directory name. This will determine if it is concrete&&exists or is either
         * virtual or doesn't exist.
         */
        BlobProperties props = null;
        boolean exists = false;
        try {
            props = this.getBlobClient().getProperties();
            exists = true;
        } catch (BlobStorageException e) {
            if (e.getStatusCode() != 404) {
                throw LoggingUtility.logError(LOGGER, new IOException(e));
            }
        }

        // Check if the resource is a file or directory before listing
        if (exists && !props.getMetadata().containsKey(AzureResource.DIR_METADATA_MARKER)) {
            return DirectoryStatus.NOT_A_DIRECTORY;
        }

        return checkDirStatus(exists);
    }

    /*
    This method will determine the status of the directory given it is already known whether or not there is an object
    at the target.
     */
    DirectoryStatus checkDirStatus(boolean exists) throws IOException {
        BlobContainerClient containerClient = this.getContainerClient();

        // List on the directory name + '/' so that we only get things under the directory if any
        ListBlobsOptions listOptions = new ListBlobsOptions().setMaxResultsPerPage(2)
            .setPrefix(this.blobClient.getBlobName() + AzureFileSystem.PATH_SEPARATOR)
            .setDetails(new BlobListDetails().setRetrieveMetadata(true));

        /*
         * If listing returns anything, then it is not empty. If listing returns nothing and exists() was true, then it's
         * empty Else it does not exist
         */
        try {
            Iterator<BlobItem> blobIterator = containerClient.listBlobsByHierarchy(AzureFileSystem.PATH_SEPARATOR,
                listOptions, null).iterator();
            if (blobIterator.hasNext()) {
                return DirectoryStatus.NOT_EMPTY;
            } else if (exists) {
                return DirectoryStatus.EMPTY;
            } else {
                return DirectoryStatus.DOES_NOT_EXIST;
            }
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(LOGGER, new IOException(e));
        }
    }

    /**
     * Creates the actual directory marker. This method should only be used when any necessary checks for proper
     * conditions of directory creation (e.g. parent existence) have already been performed. Otherwise,
     * {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} should be preferred.
     *
     * @param requestConditions Any necessary request conditions to pass when creating the directory blob.
     */
    void putDirectoryBlob(BlobRequestConditions requestConditions) {
        this.blobClient.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), this.blobHeaders,
            this.prepareMetadataForDirectory(), null, requestConditions, null, null);
    }

    /*
    Note that this will remove the properties from the list of attributes as it finds them.
     */
    private void extractHttpHeaders(List<FileAttribute<?>> fileAttributes) {
        BlobHttpHeaders headers = new BlobHttpHeaders();
        for (Iterator<FileAttribute<?>> it = fileAttributes.iterator(); it.hasNext();) {
            FileAttribute<?> attr = it.next();
            boolean propertyFound = true;
            switch (attr.name()) {
                case AzureFileSystemProvider.CONTENT_TYPE:
                    headers.setContentType(attr.value().toString());
                    break;
                case AzureFileSystemProvider.CONTENT_LANGUAGE:
                    headers.setContentLanguage(attr.value().toString());
                    break;
                case AzureFileSystemProvider.CONTENT_DISPOSITION:
                    headers.setContentDisposition(attr.value().toString());
                    break;
                case AzureFileSystemProvider.CONTENT_ENCODING:
                    headers.setContentEncoding(attr.value().toString());
                    break;
                case AzureFileSystemProvider.CONTENT_MD5:
                    if ((attr.value() instanceof byte[])) {
                        headers.setContentMd5((byte[]) attr.value());
                    } else {
                        throw LoggingUtility.logError(LOGGER,
                            new UnsupportedOperationException("Content-MD5 attribute must be a byte[]"));
                    }
                    break;
                case AzureFileSystemProvider.CACHE_CONTROL:
                    headers.setCacheControl(attr.value().toString());
                    break;
                default:
                    propertyFound = false;
                    break;
            }

            if (propertyFound) {
                it.remove();
            }
        }

        this.blobHeaders = headers;
    }

    /**
     * Note this should only be used after the headers have been extracted.
     *
     * @param fileAttributes The attributes to convert to metadata.
     */
    private void convertAttributesToMetadata(List<FileAttribute<?>> fileAttributes) {
        Map<String, String> metadata = new HashMap<>();
        for (FileAttribute<?> attr : fileAttributes) {
            metadata.put(attr.name(), attr.value().toString());
        }

        // If no attributes are set, return null so existing metadata is not cleared.
        this.blobMetadata = metadata.isEmpty() ? null : metadata;
    }

    private void validateNotRoot() {
        if (this.path.isRoot()) {
            throw LoggingUtility.logError(LOGGER, new IllegalArgumentException(
                "Root directory not supported. Path: " + this.path));
        }
    }

    private AzurePath validatePathInstanceType(Path path) {
        if (!(path instanceof AzurePath)) {
            throw LoggingUtility.logError(LOGGER, new IllegalArgumentException("This provider cannot operate on "
                + "subtypes of Path other than AzurePath"));
        }
        return (AzurePath) path;
    }

    BlobContainerClient getContainerClient() {
        return new BlobContainerClientBuilder().endpoint(this.blobClient.getBlobUrl())
            .pipeline(this.blobClient.getHttpPipeline())
            .buildClient();
    }

    AzureResource setFileAttributes(List<FileAttribute<?>> attributes) {
        attributes = new ArrayList<>(attributes); // To ensure removing header values from the list is supported.
        extractHttpHeaders(attributes);
        convertAttributesToMetadata(attributes);

        return this;
    }

    AzurePath getPath() {
        return this.path;
    }

    BlobClient getBlobClient() {
        return this.blobClient;
    }

    BlobOutputStream getBlobOutputStream(ParallelTransferOptions pto, BlobRequestConditions rq) {
        BlockBlobOutputStreamOptions options = new BlockBlobOutputStreamOptions()
            .setHeaders(this.blobHeaders)
            .setMetadata(this.blobMetadata)
            .setParallelTransferOptions(pto)
            .setRequestConditions(rq);
        return this.blobClient.getBlockBlobClient().getBlobOutputStream(options);
    }

    private Map<String, String> prepareMetadataForDirectory() {
        if (this.blobMetadata == null) {
            this.blobMetadata = new HashMap<>();
        }
        this.blobMetadata.put(DIR_METADATA_MARKER, "true");
        return this.blobMetadata;
    }
}
