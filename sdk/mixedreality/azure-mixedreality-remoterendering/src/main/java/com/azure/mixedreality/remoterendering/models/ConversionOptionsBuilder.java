package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;

public class ConversionOptionsBuilder {

    private String inputStorageContainerUri;
    private String inputStorageContainerReadListSas;
    private String inputBlobPrefix;
    private String inputRelativeAssetPath;

    private String outputStorageContainerUri;
    private String outputStorageContainerWriteSas;
    private String outputBlobPrefix;
    private String outputAssetFilename;

    public ConversionOptions buildConversionOptions() {
        return new ConversionOptions(
            new ConversionInputOptions(
                inputStorageContainerUri,
                inputStorageContainerReadListSas,
                inputBlobPrefix,
                inputRelativeAssetPath
            ),
            new ConversionOutputOptions(
                outputStorageContainerUri,
                outputStorageContainerWriteSas,
                outputBlobPrefix,
                outputAssetFilename
            ));
    }


    /**
     * Set the inputStorageContainerUri property: The URI of the Azure blob storage container containing the input model.
     *
     * @param inputStorageContainerUri the inputStorageContainerUri value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder inputStorageContainerUri(String inputStorageContainerUri) {
        this.inputStorageContainerUri = inputStorageContainerUri;
        return this;
    }

    /**
     * Set the inputStorageContainerReadListSas property: A Azure blob storage container shared access signature giving read
     * and list access to the storage container. Optional. If not is not provided the Azure Remote Rendering rendering
     * account needs to be linked with the storage account containing the blob container.
     *
     * @param inputStorageContainerReadListSas the inputStorageContainerReadListSas value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder inputStorageContainerReadListSas(String inputStorageContainerReadListSas) {
        this.inputStorageContainerReadListSas = inputStorageContainerReadListSas;
        return this;
    }

    /**
     * Set the inputBlobPrefix property: Only Blobs starting with this prefix will be downloaded to perform the conversion.
     *
     * @param inputBlobPrefix the inputBlobPrefix value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder inputBlobPrefix(String inputBlobPrefix) {
        this.inputBlobPrefix = inputBlobPrefix;
        return this;
    }

    /**
     * Set the inputRelativeAssetPath property: The relative path starting at blobPrefix (or at the container root if
     * blobPrefix is not specified) to the input model. Must point to file with a supported file format ending.
     *
     * @param inputRelativeAssetPath the inputRelativeAssetPath value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder inputRelativeAssetPath(String inputRelativeAssetPath) {
        this.inputRelativeAssetPath = inputRelativeAssetPath;
        return this;
    }


    /**
     * Set the storageContainerUri property: The URI of the Azure blob storage container where the result of the
     * conversion should be written to.
     *
     * @param outputStorageContainerUri the outputStorageContainerUri value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder outputStorageContainerUri(String outputStorageContainerUri) {
        this.outputStorageContainerUri = outputStorageContainerUri;
        return this;
    }

    /**
     * Set the storageContainerWriteSas property: A Azure blob storage container shared access signature giving write
     * access to the storage container. Optional. If not is not provided the Azure Remote Rendering rendering account
     * needs to be linked with the storage account containing the blob container.
     *
     * @param outputStorageContainerWriteSas the storageContainerWriteSas value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder outputStorageContainerWriteSas(String outputStorageContainerWriteSas) {
        this.outputStorageContainerWriteSas = outputStorageContainerWriteSas;
        return this;
    }

    /**
     * Set the blobPrefix property: A prefix which gets prepended in front of all files produced by the conversion
     * process. Will be treated as a virtual folder.
     *
     * @param outputBlobPrefix the blobPrefix value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder outputBlobPrefix(String outputBlobPrefix) {
        this.outputBlobPrefix = outputBlobPrefix;
        return this;
    }

    /**
     * Set the outputAssetFilename property: The file name of the output asset. Must end in '.arrAsset'.
     *
     * @param outputAssetFilename the outputAssetFilename value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptionsBuilder outputAssetFilename(String outputAssetFilename) {
        this.outputAssetFilename = outputAssetFilename;
        return this;
    }
}
