package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;

/**
 * The GenerateImageResult model.
 */
public class GenerateImageResult {

    private BinaryData imageBytes;
    private String revisedPrompt;
    private String base64EncodedString;
    private String blobUrl;

    /**
     * @param imageBytes
     * @param revisedPrompt
     * @param base64EncodedString
     * @param blobUrl
     */
    public GenerateImageResult(BinaryData imageBytes, String revisedPrompt, String base64EncodedString, String blobUrl) {
        this.imageBytes = imageBytes;
        this.revisedPrompt = revisedPrompt;
        this.base64EncodedString = base64EncodedString;
        this.blobUrl = blobUrl;
    }

    /**
     * @return
     */
    public BinaryData getImageBytes() {
        return imageBytes;
    }

    /**
     * @return
     */
    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    /**
     * @return
     */
    public String getBase64EncodedString() {
        return base64EncodedString;
    }

    /**
     * @return
     */
    public String getBlobUrl() {
        return blobUrl;
    }
}
