package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferCapabilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

public class DataMover {

    DataMover() {

    }

    public DataTransfer startTransfer(StorageResource from, StorageResource to) {
        TransferCapabilities fromOutgoingTransferCapabilities = from.getOutgoingTransferCapabilities();
        TransferCapabilities toIncomingTransferCapabilities = to.getIncomingTransferCapabilities();

        if (fromOutgoingTransferCapabilities.isCanUseSasUri() && toIncomingTransferCapabilities.isCanUseSasUri()) {
            return transferViaSasUri(from, to);
        } else if (fromOutgoingTransferCapabilities.isCanStream() && toIncomingTransferCapabilities.isCanStream()) {
            return transferViaStreams(from, to);
        }

        throw new IllegalArgumentException("Can't transfer");
    }

    public DataTransfer startTransfer(StorageResource from, StorageResourceContainer to) {
        return null;
    }

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

    private DataTransfer transferViaSasUri(StorageResource from, StorageResource to) {
        DataTransfer dataTransfer = new DataTransfer();

        String sasUri = from.getSasUri();
        to.consumeSasUri(sasUri);

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
