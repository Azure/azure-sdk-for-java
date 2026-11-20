// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document.models;

import com.azure.core.annotation.Fluent;

/**
 * The configurable options to pass when submitting a single document translation request.
 */
@Fluent
public final class DocumentTranslateOptions {

    /*
     * Specifies source language of the input document.
     * If this parameter isn't specified, automatic language detection is applied to determine the source language.
     * For example if the source document is written in English, then use sourceLanguage=en.
     */
    private String sourceLanguage;

    /*
     * A string specifying the category (domain) of the translation. This parameter is used to get translations
     * from a customized system built with Custom Translator. Add the Category ID from your Custom Translator
     * project details to this parameter to use your deployed customized system. Default value is: general.
     */
    private String category;

    /*
     * Deployment name of the custom translation model for the translation request.
     */
    private String deploymentName;

    /*
     * Specifies that the service is allowed to fall back to a general system when a custom system doesn't exist.
     * Possible values are: true (default) or false.
     */
    private Boolean allowFallback;

    /*
     * Optional boolean parameter to translate text within an image in the document.
     */
    private Boolean translateTextWithinImage;

    /**
     * Creates an instance of DocumentTranslateOptions class.
     */
    public DocumentTranslateOptions() {
    }

    /**
     * Get the sourceLanguage property: Specifies source language of the input document.
     * If this parameter isn't specified, automatic language detection is applied to determine the source language.
     * For example if the source document is written in English, then use sourceLanguage=en.
     *
     * @return the sourceLanguage value.
     */
    public String getSourceLanguage() {
        return this.sourceLanguage;
    }

    /**
     * Set the sourceLanguage property: Specifies source language of the input document.
     * If this parameter isn't specified, automatic language detection is applied to determine the source language.
     * For example if the source document is written in English, then use sourceLanguage=en.
     *
     * @param sourceLanguage the sourceLanguage value to set.
     * @return the DocumentTranslateOptions object itself.
     */
    public DocumentTranslateOptions setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
        return this;
    }

    /**
     * Get the category property: A string specifying the category (domain) of the translation. This parameter is used
     * to get translations from a customized system built with Custom Translator. Add the Category ID from your Custom
     * Translator project details to this parameter to use your deployed customized system. Default value is: general.
     *
     * @return the category value.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Set the category property: A string specifying the category (domain) of the translation. This parameter is used
     * to get translations from a customized system built with Custom Translator. Add the Category ID from your Custom
     * Translator project details to this parameter to use your deployed customized system. Default value is: general.
     *
     * @param category the category value to set.
     * @return the DocumentTranslateOptions object itself.
     */
    public DocumentTranslateOptions setCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Get the deploymentName property: Deployment name of the custom translation model for the translation request.
     *
     * @return the deploymentName value.
     */
    public String getDeploymentName() {
        return this.deploymentName;
    }

    /**
     * Set the deploymentName property: Deployment name of the custom translation model for the translation request.
     *
     * @param deploymentName the deploymentName value to set.
     * @return the DocumentTranslateOptions object itself.
     */
    public DocumentTranslateOptions setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }

    /**
     * Get the allowFallback property: Specifies that the service is allowed to fall back to a general system when a
     * custom system doesn't exist. Possible values are: true (default) or false.
     *
     * @return the allowFallback value.
     */
    public Boolean isAllowFallback() {
        return this.allowFallback;
    }

    /**
     * Set the allowFallback property: Specifies that the service is allowed to fall back to a general system when a
     * custom system doesn't exist. Possible values are: true (default) or false.
     *
     * @param allowFallback the allowFallback value to set.
     * @return the DocumentTranslateOptions object itself.
     */
    public DocumentTranslateOptions setAllowFallback(Boolean allowFallback) {
        this.allowFallback = allowFallback;
        return this;
    }

    /**
     * Get the translateTextWithinImage property: Optional boolean parameter to translate text within an image in the
     * document.
     *
     * @return the translateTextWithinImage value.
     */
    public Boolean isTranslateTextWithinImage() {
        return this.translateTextWithinImage;
    }

    /**
     * Set the translateTextWithinImage property: Optional boolean parameter to translate text within an image in the
     * document.
     *
     * @param translateTextWithinImage the translateTextWithinImage value to set.
     * @return the DocumentTranslateOptions object itself.
     */
    public DocumentTranslateOptions setTranslateTextWithinImage(Boolean translateTextWithinImage) {
        this.translateTextWithinImage = translateTextWithinImage;
        return this;
    }
}
