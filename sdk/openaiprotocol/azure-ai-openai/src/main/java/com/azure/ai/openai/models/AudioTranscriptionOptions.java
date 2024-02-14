package com.azure.ai.openai.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AudioTranscriptionOptions {

    /*
     * The optional filename or descriptive identifier to associate with with the audio data.
     */

    @JsonProperty(value = "filename")
    private String filename;

    /*
     * The requested format of the transcription response data, which will influence the content and detail of the
     * result.
     */

    @JsonProperty(value = "response_format")
    private AudioTranscriptionFormat transcriptionFormat;

    /*
     * The primary spoken language of the audio data to be transcribed, supplied as a two-letter ISO-639-1 language
     * code
     * such as 'en' or 'fr'.
     * Providing this known input language is optional but may improve the accuracy and/or latency of transcription.
     */

    @JsonProperty(value = "language")
    private String inputLanguage;

    /*
     * An optional hint to guide the model's style or continue from a prior audio segment. The written language of the
     * prompt should match the primary spoken language of the audio data.
     */

    @JsonProperty(value = "prompt")
    private String prompt;

    /*
     * The sampling temperature, between 0 and 1.
     * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused
     * and deterministic.
     * If set to 0, the model will use log probability to automatically increase the temperature until certain
     * thresholds are hit.
     */

    @JsonProperty(value = "temperature")
    private Double temperature;

    /**
     * Creates an instance of AudioTranscriptionOptions class.
     */

    @JsonCreator
    public AudioTranscriptionOptions() {
    }

    /**
     * Get the filename property: The optional filename or descriptive identifier to associate with with the audio
     * data.
     *
     * @return the filename value.
     */

    public String getFilename() {
        return this.filename;
    }

    /**
     * Set the filename property: The optional filename or descriptive identifier to associate with with the audio
     * data.
     *
     * @param filename the filename value to set.
     * @return the AudioTranscriptionOptions object itself.
     */
    public AudioTranscriptionOptions setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Get the responseFormat property: The requested format of the transcription response data, which will influence
     * the content and detail of the result.
     *
     * @return the responseFormat value.
     */

    public AudioTranscriptionFormat getTranscriptionFormat() {
        return this.transcriptionFormat;
    }

    /**
     * Set the responseFormat property: The requested format of the transcription response data, which will influence
     * the content and detail of the result.
     *
     * @param transcriptionFormat the transcriptionFormat value to set.
     * @return the AudioTranscriptionOptions object itself.
     */

    public AudioTranscriptionOptions setTranscriptionFormat(AudioTranscriptionFormat transcriptionFormat) {
        this.transcriptionFormat = transcriptionFormat;
        return this;
    }

    /**
     * Get the language property: The primary spoken language of the audio data to be transcribed, supplied as a
     * two-letter ISO-639-1 language code
     * such as 'en' or 'fr'.
     * Providing this known input language is optional but may improve the accuracy and/or latency of transcription.
     *
     * @return the language value.
     */

    public String getInputLanguage() {
        return this.inputLanguage;
    }

    /**
     * Set the language property: The primary spoken language of the audio data to be transcribed, supplied as a
     * two-letter ISO-639-1 language code
     * such as 'en' or 'fr'.
     * Providing this known input language is optional but may improve the accuracy and/or latency of transcription.
     *
     * @param language the language value to set.
     * @return the AudioTranscriptionOptions object itself.
     */

    public AudioTranscriptionOptions setLanguage(String language) {
        this.inputLanguage = language;
        return this;
    }

    /**
     * Get the prompt property: An optional hint to guide the model's style or continue from a prior audio segment. The
     * written language of the
     * prompt should match the primary spoken language of the audio data.
     *
     * @return the prompt value.
     */

    public String getPrompt() {
        return this.prompt;
    }

    /**
     * Set the prompt property: An optional hint to guide the model's style or continue from a prior audio segment. The
     * written language of the
     * prompt should match the primary spoken language of the audio data.
     *
     * @param prompt the prompt value to set.
     * @return the AudioTranscriptionOptions object itself.
     */

    public AudioTranscriptionOptions setPrompt(String prompt) {
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
     * @return the AudioTranscriptionOptions object itself.
     */

    public AudioTranscriptionOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

}
