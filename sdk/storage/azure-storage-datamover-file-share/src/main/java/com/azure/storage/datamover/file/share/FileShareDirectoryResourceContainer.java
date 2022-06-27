package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferMethod;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class FileShareDirectoryResourceContainer extends StorageResourceContainer {

    private final ShareDirectoryClient shareDirectoryClient;

    FileShareDirectoryResourceContainer(ShareDirectoryClient shareDirectoryClient) {
        this.shareDirectoryClient = Objects.requireNonNull(shareDirectoryClient);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        return shareDirectoryClient.listFilesAndDirectories()
            .stream().filter(
                item -> !item.isDirectory()
            ).map(item -> new FileShareResource(shareDirectoryClient.getFileClient(item.getName())))
            .collect(Collectors.toList());
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            shareDirectoryClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }
}
