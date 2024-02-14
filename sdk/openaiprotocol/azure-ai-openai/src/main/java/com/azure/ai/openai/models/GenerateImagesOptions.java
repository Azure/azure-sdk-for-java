package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GenerateImagesOptions {

    /*
     * The number of images to generate.
     * Dall-e-2 models support values between 1 and 10.
     * Dall-e-3 models only support a value of 1.
     */
    @Generated
    @JsonProperty(value = "n")
    private Integer imageCount;

    /*
     * The desired dimensions for generated images.
     * Dall-e-2 models support 256x256, 512x512, or 1024x1024.
     * Dall-e-3 models support 1024x1024, 1792x1024, or 1024x1792.
     */
    @Generated
    @JsonProperty(value = "size")
    private ImageSize size;

    /*
     * The format in which image generation response items should be presented.
     */
    @Generated
    @JsonProperty(value = "response_format")
    private ImageGenerationResponseFormat responseFormat;

    /*
     * The desired image generation quality level to use.
     * Only configurable with dall-e-3 models.
     */
    @Generated
    @JsonProperty(value = "quality")
    private ImageGenerationQuality quality;

    /*
     * The desired image generation style to use.
     * Only configurable with dall-e-3 models.
     */
    @Generated
    @JsonProperty(value = "style")
    private ImageGenerationStyle style;

    /*
     * A unique identifier representing your end-user, which can help to monitor and detect abuse.
     */
    @Generated
    @JsonProperty(value = "user")
    private String user;

    /**
     * Creates an instance of GenerateImagesOptions class.
     *
     */
    @Generated
    @JsonCreator
    public GenerateImagesOptions() {
    }

    /**
     * Get the n property: The number of images to generate.
     * Dall-e-2 models support values between 1 and 10.
     * Dall-e-3 models only support a value of 1.
     *
     * @return the n value.
     */
    @Generated
    public Integer getImageCount() {
        return this.imageCount;
    }

    /**
     * Set the n property: The number of images to generate.
     * Dall-e-2 models support values between 1 and 10.
     * Dall-e-3 models only support a value of 1.
     *
     * @param imageCount the n value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
        return this;
    }

    /**
     * Get the size property: The desired dimensions for generated images.
     * Dall-e-2 models support 256x256, 512x512, or 1024x1024.
     * Dall-e-3 models support 1024x1024, 1792x1024, or 1024x1792.
     *
     * @return the size value.
     */
    @Generated
    public ImageSize getSize() {
        return this.size;
    }

    /**
     * Set the size property: The desired dimensions for generated images.
     * Dall-e-2 models support 256x256, 512x512, or 1024x1024.
     * Dall-e-3 models support 1024x1024, 1792x1024, or 1024x1792.
     *
     * @param size the size value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setSize(ImageSize size) {
        this.size = size;
        return this;
    }

    /**
     * Get the responseFormat property: The format in which image generation response items should be presented.
     *
     * @return the responseFormat value.
     */
    @Generated
    public ImageGenerationResponseFormat getResponseFormat() {
        return this.responseFormat;
    }

    /**
     * Set the responseFormat property: The format in which image generation response items should be presented.
     *
     * @param responseFormat the responseFormat value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setResponseFormat(ImageGenerationResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    /**
     * Get the quality property: The desired image generation quality level to use.
     * Only configurable with dall-e-3 models.
     *
     * @return the quality value.
     */
    @Generated
    public ImageGenerationQuality getQuality() {
        return this.quality;
    }

    /**
     * Set the quality property: The desired image generation quality level to use.
     * Only configurable with dall-e-3 models.
     *
     * @param quality the quality value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setQuality(ImageGenerationQuality quality) {
        this.quality = quality;
        return this;
    }

    /**
     * Get the style property: The desired image generation style to use.
     * Only configurable with dall-e-3 models.
     *
     * @return the style value.
     */
    @Generated
    public ImageGenerationStyle getStyle() {
        return this.style;
    }

    /**
     * Set the style property: The desired image generation style to use.
     * Only configurable with dall-e-3 models.
     *
     * @param style the style value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setStyle(ImageGenerationStyle style) {
        this.style = style;
        return this;
    }

    /**
     * Get the user property: A unique identifier representing your end-user, which can help to monitor and detect
     * abuse.
     *
     * @return the user value.
     */
    @Generated
    public String getUser() {
        return this.user;
    }

    /**
     * Set the user property: A unique identifier representing your end-user, which can help to monitor and detect
     * abuse.
     *
     * @param user the user value to set.
     * @return the GenerateImagesOptions object itself.
     */
    @Generated
    public GenerateImagesOptions setUser(String user) {
        this.user = user;
        return this;
    }
}
