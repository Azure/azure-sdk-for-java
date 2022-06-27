package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferMethod;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class FileShareResourceContainer extends StorageResourceContainer {

    private final ShareClient shareClient;

    FileShareResourceContainer(ShareClient shareClient) {
        this.shareClient = Objects.requireNonNull(shareClient);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        return shareClient.getRootDirectoryClient()
            .listFilesAndDirectories()
            .stream().filter(
                item -> !item.isDirectory()
            ).map(item -> new FileShareResource(shareClient.getFileClient(item.getName())))
            .collect(Collectors.toList());
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            shareClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }
}
