// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobItem;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Is asynchronously closeable. Closing the stream from any thread will cause the stream to stop returning elements.
 *
 * {@inheritDoc}
 */
public class AzureDirectoryStream implements DirectoryStream<Path> {
    private final ClientLogger logger = new ClientLogger(AzureDirectoryStream.class);

    private final AzureResource azureResource;
    private final DirectoryStream.Filter<? super Path> filter;
    private boolean iteratorRequested = false;
    boolean closed = false;

    AzureDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        this.azureResource = new AzureResource(path);
        this.filter = filter;
    }

    @Override
    public Iterator<Path> iterator() {
        if (this.iteratorRequested) {
            throw LoggingUtility.logError(logger,
                new IllegalStateException("Only one iterator may be requested from a given directory stream"));
        }
        this.iteratorRequested = true;
        return new AzureDirectoryIterator(this, this.azureResource, this.filter);
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    private static class AzureDirectoryIterator implements Iterator<Path> {
        private final ClientLogger logger = new ClientLogger(AzureDirectoryIterator.class);

        private final AzureDirectoryStream parentStream;
        private final DirectoryStream.Filter<? super Path> filter;
        private final Iterator<BlobItem> blobIterator;
        private final AzureResource azureResource;
        private Path bufferedNext = null;

        AzureDirectoryIterator(AzureDirectoryStream parentStream, AzureResource resource,
            DirectoryStream.Filter<? super Path> filter)  {
            this.parentStream = parentStream;
            this.filter = filter;
            this.azureResource = resource;
            this.blobIterator = resource.getContainerClient()
                .listBlobsByHierarchy(resource.getBlobClient().getBlobName() + AzureFileSystem.PATH_SEPARATOR)
                .iterator();
        }

        @Override
        public boolean hasNext() {
            // Closing the parent stream halts iteration.
            if (parentStream.closed) {
                return false;
            }

            // In case a customer calls hasNext multiple times in a row. If we've buffered an element, we have a next.
            if (this.bufferedNext != null) {
                return true;
            }

            /*
            Search for a new element that passes the filter and buffer it when found. If no such element is found,
            return false.
             */
            while (blobIterator.hasNext()) {
                Path nextPath = getNextListResult();
                try {
                    if (filter.accept(nextPath)) {
                        this.bufferedNext = nextPath;
                        return true;
                    }
                } catch (IOException e) {
                    throw LoggingUtility.logError(logger, new DirectoryIteratorException(e));
                }
            }
            return false;
        }

        @Override
        public Path next() {
            if (this.bufferedNext == null) {
                if (!this.hasNext()) { // This will populate cachedNext in the process.
                    throw LoggingUtility.logError(logger, new NoSuchElementException());
                }
            }
            Path next = this.bufferedNext; // cachedNext will have been populated by hasNext()
            this.bufferedNext = null;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Path getNextListResult() {
            // Strip the root if it is present so we can relativize the list result, which never has a root.
            Path withoutRoot = azureResource.getPath();
            if (withoutRoot.isAbsolute()) {
                withoutRoot = azureResource.getPath().getRoot().relativize(azureResource.getPath());
            }

            /*
            Listing results return the full blob path, and we don't want to duplicate the path we listed off of, so
            we relativize to remove it.
             */
            String blobName = this.blobIterator.next().getName();
            Path relativeResult = withoutRoot.relativize(
                this.azureResource.getPath().getFileSystem().getPath(blobName));

            // Resolve the cleaned list result against the original path for the final result.
            return azureResource.getPath().resolve(relativeResult);
        }
    }
}
