package com.azure.storage.file.share.resource;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;

public final class ShareStorageResources {

    private ShareStorageResources(){
    }

    public static StorageResourceContainer share(ShareClient shareClient) {
        return new ShareStorageResourceContainer(shareClient);
    }

    public static StorageResourceContainer directory(ShareDirectoryClient directoryClient) {
        return new ShareDirectoryStorageResourceContainer(directoryClient);
    }

    public static StorageResource file(ShareFileClient file) {
        return new ShareFileStorageResource(file);
    }
}
