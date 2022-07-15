package com.azure.storage.common.datamover;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Data mover can move data from one place to the other.
 */
public class DataMover {

    DataMover() {

    }

    /**
     * Starts a transfer of a single storage resource.
     * @param from a source storage resource.
     * @param to a destination storage resource.
     * @return A {@link DataTransfer} that can be used to monitor the transfer.
     */
    public DataTransfer startTransfer(StorageResource from, StorageResource to) {

        if (from.canProduceUrl() && to.canConsumeUrl()) {
            return transferViaUri(from, to);
        } else if (from.canProduceInputStream() && to.canConsumeInputStream()) {
            return transferViaStreams(from, to);
        }

        throw new IllegalArgumentException("Can't transfer");
    }

    /**
     * Starts a transfer of a single storage resource.
     * @param from a source storage resource.
     * @param to a destination storage resource container.
     * @return A {@link DataTransfer} that can be used to monitor the transfer.
     */
    public DataTransfer startTransfer(StorageResource from, StorageResourceContainer to) {
        return null;
    }

    /**
     * Starts a transfer of multiple storage resources that reside in storage resource container.
     * @param from a source storage resource container.
     * @param to a destination storage resource container.
     * @return A {@link DataTransfer} that can be used to monitor the transfer.
     */
    public DataTransfer startTransfer(StorageResourceContainer from, StorageResourceContainer to) {
        DataTransfer dataTransfer = new DataTransfer();
        for (StorageResource fromResource : from.listResources()) {
            List<String> path = fromResource.getPath();
            StorageResource toResource = to.getStorageResource(path);
            startTransfer(fromResource, toResource);
        }
        dataTransfer.latch.countDown();
        return dataTransfer;
    }

    private DataTransfer transferViaUri(StorageResource from, StorageResource to) {
        DataTransfer dataTransfer = new DataTransfer();

        String uri = from.getUrl();
        to.consumeUrl(uri);

        dataTransfer.latch.countDown();
        return dataTransfer;
    }

    private DataTransfer transferViaStreams(StorageResource from, StorageResource to) {
        DataTransfer dataTransfer = new DataTransfer();
        long length = from.getLength();
        try (InputStream is = from.openInputStream()) {
            to.consumeInputStream(is, length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        dataTransfer.latch.countDown();
        return dataTransfer;
    }

}
