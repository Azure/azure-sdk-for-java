package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferMethod;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class FileShareResource extends StorageResource {

    private final ShareFileClient shareFileClient;

    FileShareResource(ShareFileClient shareFileClient) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }

    @Override
    protected Set<TransferMethod> getOutgoingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setReadPermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }
}
