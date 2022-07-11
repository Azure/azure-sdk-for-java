package com.azure.storage.file.share.resource;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.resource.StorageResource;
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

class ShareFileStorageResource implements StorageResource {

    private final ShareFileClient shareFileClient;
    private final ShareDirectoryClient root;

    ShareFileStorageResource(ShareFileClient shareFileClient) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
        root = null;
    }

    ShareFileStorageResource(ShareFileClient shareFileClient, ShareDirectoryClient root) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
        this.root = Objects.requireNonNull(root);
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
    public String getUri() {
        return shareFileClient.getFileUrl() + "?" + shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new ShareSasPermission().setReadPermission(true)));
    }

    @Override
    public void consumeUri(String uri) {
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

        shareFileClient.beginCopy(uri, Collections.emptyMap(), Duration.ofSeconds(1)).waitForCompletion();
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

    @Override
    public boolean canConsumeStream() {
        return true;
    }

    @Override
    public boolean canProduceStream() {
        return true;
    }

    @Override
    public boolean canConsumeUri() {
        return true;
    }

    @Override
    public boolean canProduceUri() {
        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setReadPermission(true)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
