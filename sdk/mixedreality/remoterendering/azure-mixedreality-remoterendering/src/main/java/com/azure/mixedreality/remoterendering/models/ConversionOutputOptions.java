package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionOutputSettings;

public class ConversionOutputOptions {

    private final String storageContainerUri;
    private final String storageContainerWriteSas;
    private final String blobPrefix;
    private final String assetFilename;

    public ConversionOutputOptions(String storageContainerUri, String storageContainerWriteSas, String blobPrefix, String assetFilename) {
        this.storageContainerUri = storageContainerUri;
        this.storageContainerWriteSas = storageContainerWriteSas;
        this.blobPrefix = blobPrefix;
        this.assetFilename = assetFilename;
    }

    /**
     * Get the storageContainerUri property: The URI of the Azure blob storage container where the result of the
     * conversion should be written to.
     *
     * @return the storageContainerUri value.
     */
    public String getStorageContainerUri() {
        return this.storageContainerUri;
    }

    /**
     * Get the storageContainerWriteSas property: A Azure blob storage container shared access signature giving write
     * access to the storage container. Optional. If not is not provided the Azure Remote Rendering rendering account
     * needs to be linked with the storage account containing the blob container.
     *
     * @return the storageContainerWriteSas value.
     */
    public String getStorageContainerWriteSas() {
        return this.storageContainerWriteSas;
    }

    /**
     * Get the blobPrefix property: A prefix which gets prepended in front of all files produced by the conversion
     * process. Will be treated as a virtual folder.
     *
     * @return the blobPrefix value.
     */
    public String getBlobPrefix() {
        return this.blobPrefix;
    }

    /**
     * Get the outputAssetFilename property: The file name of the output asset. Must end in '.arrAsset'.
     *
     * @return the outputAssetFilename value.
     */
    public String getAssetFilename() {
        return this.assetFilename;
    }

}
