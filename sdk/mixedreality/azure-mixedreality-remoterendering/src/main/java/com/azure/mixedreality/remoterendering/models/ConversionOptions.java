package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;

public final class ConversionOptions {

    private String inputStorageContainerUri;
    private String inputStorageContainerReadListSas;
    private String inputBlobPrefix;
    private String inputRelativeAssetPath;

    private String outputStorageContainerUri;
    private String outputStorageContainerWriteSas;
    private String outputBlobPrefix;
    private String outputAssetFilename;

    private Duration pollInterval = Duration.ofSeconds(10);

    /**
     * Get the polling interval to use during long-running beginConversion operations.
     * @return The current polling interval.
     */
    public Duration getPollInterval() { return this.pollInterval; }

    /**
     * Set the polling interval to use during long-running beginConversion operations.
     * @param pollInterval The new period to use for polling.
     */
    public void pollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }


    // input setters
    /**
     * Set the inputStorageContainerUri property: The URI of the Azure blob storage container containing the input model.
     *
     * @param inputStorageContainerUri the inputStorageContainerUri value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptions inputStorageContainerUri(String inputStorageContainerUri) {
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
    public ConversionOptions inputStorageContainerReadListSas(String inputStorageContainerReadListSas) {
        this.inputStorageContainerReadListSas = inputStorageContainerReadListSas;
        return this;
    }

    /**
     * Set the inputBlobPrefix property: Only Blobs starting with this prefix will be downloaded to perform the conversion.
     *
     * @param inputBlobPrefix the inputBlobPrefix value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptions inputBlobPrefix(String inputBlobPrefix) {
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
    public ConversionOptions inputRelativeAssetPath(String inputRelativeAssetPath) {
        this.inputRelativeAssetPath = inputRelativeAssetPath;
        return this;
    }


    // input getters
    /**
     * Get the inputStorageContainerUri property: The URI of the Azure blob storage container containing the input model.
     *
     * @return the inputStorageContainerUri value.
     */
    public String getInputStorageContainerUri() {
        return this.inputStorageContainerUri;
    }

    /**
     * Get the inputStorageContainerReadListSas property: A Azure blob storage container shared access signature giving read
     * and list access to the storage container. Optional. If not is not provided the Azure Remote Rendering rendering
     * account needs to be linked with the storage account containing the blob container.
     *
     * @return the inputStorageContainerReadListSas value.
     */
    public String getInputStorageContainerReadListSas() {
        return this.inputStorageContainerReadListSas;
    }

    /**
     * Get the inputBlobPrefix property: Only Blobs starting with this prefix will be downloaded to perform the conversion.
     *
     * @return the inputBlobPrefix value.
     */
    public String getInputBlobPrefix() {
        return this.inputBlobPrefix;
    }

    /**
     * Get the inputRelativeAssetPath property: The relative path starting at blobPrefix (or at the container root if
     * blobPrefix is not specified) to the input model. Must point to file with a supported file format ending.
     *
     * @return the inputRelativeAssetPath value.
     */
    public String getInputRelativeAssetPath() {
        return this.inputRelativeAssetPath;
    }


    // output setters
    /**
     * Set the storageContainerUri property: The URI of the Azure blob storage container where the result of the
     * conversion should be written to.
     *
     * @param outputStorageContainerUri the outputStorageContainerUri value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptions outputStorageContainerUri(String outputStorageContainerUri) {
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
    public ConversionOptions outputStorageContainerWriteSas(String outputStorageContainerWriteSas) {
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
    public ConversionOptions outputBlobPrefix(String outputBlobPrefix) {
        this.outputBlobPrefix = outputBlobPrefix;
        return this;
    }

    /**
     * Set the outputAssetFilename property: The file name of the output asset. Must end in '.arrAsset'.
     *
     * @param outputAssetFilename the outputAssetFilename value to set.
     * @return the ConversionOptionsBuilder object itself.
     */
    public ConversionOptions outputAssetFilename(String outputAssetFilename) {
        this.outputAssetFilename = outputAssetFilename;
        return this;
    }


    // output getters
    /**
     * Get the outputStorageContainerUri property: The URI of the Azure blob storage container where the result of the
     * conversion should be written to.
     *
     * @return the outputStorageContainerUri value.
     */
    public String getOutputStorageContainerUri() {
        return this.outputStorageContainerUri;
    }

    /**
     * Get the outputStorageContainerWriteSas property: A Azure blob storage container shared access signature giving write
     * access to the storage container. Optional. If it is not provided the Azure Remote Rendering rendering account
     * needs to be linked with the storage account containing the blob container.
     *
     * @return the outputStorageContainerWriteSas value.
     */
    public String getOutputStorageContainerWriteSas() {
        return this.outputStorageContainerWriteSas;
    }

    /**
     * Get the outputBlobPrefix property: A prefix which gets prepended in front of all files produced by the conversion
     * process. Will be treated as a virtual folder.
     *
     * @return the outputBlobPrefix value.
     */
    public String getOutputBlobPrefix() {
        return this.outputBlobPrefix;
    }

    /**
     * Get the outputAssetFilename property: The file name of the output asset. Must end in '.arrAsset'.
     *
     * @return the outputAssetFilename value.
     */
    public String getOutputAssetFilename() {
        return this.outputAssetFilename;
    }
}
