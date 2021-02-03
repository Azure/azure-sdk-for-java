package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionOutputSettings;

public class ConversionInputOptions {

    private final String storageContainerUri;
    private final String storageContainerReadListSas;
    private final String blobPrefix;
    private final String relativeAssetPath;

    public ConversionInputOptions(String storageContainerUri, String storageContainerReadListSas, String blobPrefix, String relativeAssetPath) {
        this.storageContainerUri = storageContainerUri;
        this.storageContainerReadListSas = storageContainerReadListSas;
        this.blobPrefix = blobPrefix;
        this.relativeAssetPath = relativeAssetPath;
    }


    /**
     * Get the storageContainerUri property: The URI of the Azure blob storage container containing the input model.
     *
     * @return the storageContainerUri value.
     */
    public String getStorageContainerUri() {
        return this.storageContainerUri;
    }

    /**
     * Get the storageContainerReadListSas property: A Azure blob storage container shared access signature giving read
     * and list access to the storage container. Optional. If not is not provided the Azure Remote Rendering rendering
     * account needs to be linked with the storage account containing the blob container.
     *
     * @return the storageContainerReadListSas value.
     */
    public String getStorageContainerReadListSas() {
        return this.storageContainerReadListSas;
    }

    /**
     * Get the blobPrefix property: Only Blobs starting with this prefix will be downloaded to perform the conversion.
     *
     * @return the blobPrefix value.
     */
    public String getBlobPrefix() {
        return this.blobPrefix;
    }

    /**
     * Get the relativeAssetPath property: The relative path starting at blobPrefix (or at the container root if
     * blobPrefix is not specified) to the input model. Must point to file with a supported file format ending.
     *
     * @return the relativeAssetPath value.
     */
    public String getRelativeAssetPath() {
        return this.relativeAssetPath;
    }
}
