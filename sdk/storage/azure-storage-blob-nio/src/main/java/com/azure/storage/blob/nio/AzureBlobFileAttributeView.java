package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AzureBlobFileAttributeView implements BasicFileAttributeView {
    private final ClientLogger logger = new ClientLogger(AzureBlobFileAttributes.class);

    private final Path path;

    AzureBlobFileAttributeView(Path path) {
        this.path = path;
    }

    static Map<String, Consumer<Object>> setAttributeConsumers(AzureBlobFileAttributeView view) {
        Map<String, Consumer<Object>> map = new HashMap<>();
        map.put("blobHttpHeaders", obj -> {
            try {
                view.setBlobHttpHeaders((BlobHttpHeaders) obj);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        map.put("lastModifiedTime", attributes::lastModifiedTime);
    }

    @Override
    public String name() {
        return "azureBlob";
    }

    /**
     * Gets a fresh copy every time it is called.
     * @return
     * @throws IOException
     */
    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return new AzureBlobFileAttributes(path);
    }

    public void setBlobHttpHeaders(BlobHttpHeaders headers) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setHttpHeaders(headers);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    public void setMetadata(Map<String, String> metadata) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setMetadata(metadata);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    public void setTier(AccessTier tier) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setAccessTier(tier);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    @Override
    public void setTimes(FileTime fileTime, FileTime fileTime1, FileTime fileTime2) throws IOException {
        throw new UnsupportedOperationException();
    }
}
