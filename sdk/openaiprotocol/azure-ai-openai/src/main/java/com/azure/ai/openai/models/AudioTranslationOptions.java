package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AudioTranslationOptions {

    /*
     * The optional filename or descriptive identifier to associate with with the audio data.
     */
    @Generated
    @JsonProperty(value = "filename")
    private String filename;

    /*
     * The requested format of the translation response data, which will influence the content and detail of the
     * result.
     */
    @Generated
    @JsonProperty(value = "response_format")
    private AudioTranslationFormat translationFormat;

    /*
     * An optional hint to guide the model's style or continue from a prior audio segment. The written language of the
     * prompt should match the primary spoken language of the audio data.
     */
    @Generated
    @JsonProperty(value = "prompt")
    private String prompt;

    /*
     * The sampling temperature, between 0 and 1.
     * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused
     * and deterministic.
     * If set to 0, the model will use log probability to automatically increase the temperature until certain
     * thresholds are hit.
     */
    @Generated
    @JsonProperty(value = "temperature")
    private Double temperature;

    /**
     * Creates an instance of AudioTranslationOptions class.
     */
    @Generated
    @JsonCreator
    public AudioTranslationOptions() {
    }

    /**
     * Get the filename property: The optional filename or descriptive identifier to associate with with the audio
     * data.
     *
     * @return the filename value.
     */
    @Generated
    public String getFilename() {
        return this.filename;
    }

    /**
     * Set the filename property: The optional filename or descriptive identifier to associate with with the audio
     * data.
     *
     * @param filename the filename value to set.
     * @return the AudioTranslationOptions object itself.
     */
    @Generated
    public AudioTranslationOptions setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Get the responseFormat property: The requested format of the translation response data, which will influence the
     * content and detail of the result.
     *
     * @return the responseFormat value.
     */
    @Generated
    public AudioTranslationFormat getTranslationFormat() {
        return this.translationFormat;
    }

    /**
     * Set the responseFormat property: The requested format of the translation response data, which will influence the
     * content and detail of the result.
     *
     * @param translationFormat the responseFormat value to set.
     * @return the AudioTranslationOptions object itself.
     */
    @Generated
    public AudioTranslationOptions setTranslationFormat(AudioTranslationFormat translationFormat) {
        this.translationFormat = translationFormat;
        return this;
    }

    /**
     * Get the prompt property: An optional hint to guide the model's style or continue from a prior audio segment. The
     * written language of the
     * prompt should match the primary spoken language of the audio data.
     *
     * @return the prompt value.
     */
    @Generated
    public String getPrompt() {
        return this.prompt;
    }

    /**
     * Set the prompt property: An optional hint to guide the model's style or continue from a prior audio segment. The
     * written language of the
     * prompt should match the primary spoken language of the audio data.
     *
     * @param prompt the prompt value to set.
     * @return the AudioTranslationOptions object itself.
     */
    @Generated
    public AudioTranslationOptions setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Get the temperature property: The sampling temperature, between 0 and 1.
     * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused
     * and deterministic.
     * If set to 0, the model will use log probability to automatically increase the temperature until certain
     * thresholds are hit.
     *
     * @return the temperature value.
     */
    @Generated
    public Double getTemperature() {
        return this.temperature;
    }

    /**
     * Set the temperature property: The sampling temperature, between 0 and 1.
     * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused
     * and deterministic.
     * If set to 0, the model will use log probability to automatically increase the temperature until certain
     * thresholds are hit.
     *
     * @param temperature the temperature value to set.
     * @return the AudioTranslationOptions object itself.
     */
    @Generated
    public AudioTranslationOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

}
