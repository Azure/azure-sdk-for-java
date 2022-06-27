package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;

public final class FileShareResources {

    private FileShareResources(){
    }

    public static StorageResourceContainer share(ShareClient shareClient) {
        return new FileShareResourceContainer(shareClient);
    }

    public static StorageResourceContainer directory(ShareDirectoryClient directoryClient) {
        return new FileShareDirectoryResourceContainer(directoryClient);
    }

    public static StorageResource file(ShareFileClient file) {
        return new FileShareResource(file);
    }
}
