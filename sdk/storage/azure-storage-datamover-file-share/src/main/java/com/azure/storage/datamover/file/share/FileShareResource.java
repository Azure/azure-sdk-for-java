package com.azure.storage.datamover.file.share;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class FileShareResource extends StorageResource {

    private final ShareFileClient shareFileClient;
    private final ShareDirectoryClient root;

    FileShareResource(ShareFileClient shareFileClient) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
        root = null;
    }

    FileShareResource(ShareFileClient shareFileClient, ShareDirectoryClient root) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    public TransferCapabilities getOutgoingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setReadPermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    public InputStream openInputStream() {
        return shareFileClient.openInputStream();
    }

    @Override
    public long getLength() {
        return shareFileClient.getProperties().getContentLength();
    }

    @Override
    public void consumeInputStream(InputStream inputStream, long length) {
        try {
            if (!shareFileClient.exists()) {
                shareFileClient.create(length);
            }
        } catch (ShareStorageException e) {
            if (root != null && ShareErrorCode.PARENT_NOT_FOUND.equals(e.getErrorCode())) {
                ShareDirectoryClient directoryClient = root;
                List<String> path = getPath();
                for (int i = 0; i < path.size() - 1; i++) {
                    directoryClient = directoryClient.getSubdirectoryClient(path.get(i));
                    directoryClient.createIfNotExists();
                }
                shareFileClient.create(length);
            } else  {
                throw e;
            }
        }
        shareFileClient.upload(inputStream, length, new ParallelTransferOptions());
    }

    @Override
    public String getSasUri() {
        return shareFileClient.getFileUrl() + "?" + shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new ShareSasPermission().setReadPermission(true)));
    }

    @Override
    public void consumeSasUri(String sasUri) {
        try {
            if (!shareFileClient.exists()) {
                // TODO HEAD sas uri.
                shareFileClient.create(10 * 1024 * 1024);
            }
        } catch (ShareStorageException e) {
            if (root != null && ShareErrorCode.PARENT_NOT_FOUND.equals(e.getErrorCode())) {
                ShareDirectoryClient directoryClient = root;
                List<String> path = getPath();
                for (int i = 0; i < path.size() - 1; i++) {
                    directoryClient = directoryClient.getSubdirectoryClient(path.get(i));
                    directoryClient.createIfNotExists();
                }
                // TODO HEAD sas uri.
                shareFileClient.create(10 * 1024 * 1024);
            } else  {
                throw e;
            }
        }

        shareFileClient.beginCopy(sasUri, Collections.emptyMap(), Duration.ofSeconds(1)).waitForCompletion();
    }

    @Override
    public List<String> getPath() {
        String filePath = shareFileClient.getFilePath();
        if (root != null) {
            filePath = filePath.replace(root.getDirectoryPath(), "");
        }
        String[] split = filePath.split("/");
        return Arrays.asList(split);
    }
}
