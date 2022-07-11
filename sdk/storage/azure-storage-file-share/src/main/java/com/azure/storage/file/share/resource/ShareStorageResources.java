package com.azure.storage.file.share.resource;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;

/**
 * A class with factories for file share resources.
 */
public final class ShareStorageResources {

    private ShareStorageResources(){
    }

    /**
     * Creates {@link StorageResourceContainer} representing a share.
     * @param shareClient The share client.
     * @return A {@link StorageResourceContainer} representing a share.
     */
    public static StorageResourceContainer share(ShareClient shareClient) {
        return new ShareStorageResourceContainer(shareClient);
    }

    /**
     * Creates {@link StorageResourceContainer} representing a directory on the share.
     * @param directoryClient The directory client.
     * @return A {@link StorageResourceContainer} representing a directory on the share.
     */
    public static StorageResourceContainer directory(ShareDirectoryClient directoryClient) {
        return new ShareDirectoryStorageResourceContainer(directoryClient);
    }

    /**
     * Creates {@link StorageResource} representing a file on the share.
     * @param shareFileClient The share client.
     * @return A {@link StorageResource} representing a file on the share.
     */
    public static StorageResource file(ShareFileClient shareFileClient) {
        return new ShareFileStorageResource(shareFileClient);
    }
}
