package com.azure.ai.openai.implementation.models;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.FileDetails;
import com.azure.core.annotation.Fluent;

@Fluent
public final class UploadAudioFileRequest {

    private final FileDetails file;

    private AudioTranscriptionFormat transcriptionFormat;

    private AudioTranslationFormat translationFormat;

    // AudioTranscriptionOptions field
    private String language;

    private String prompt;

    private Double temperature;

    private String model;

    public FileDetails getFile() {
        return file;
    }

    public AudioTranscriptionFormat getTranscriptionFormat() {
        return transcriptionFormat;
    }

    public UploadAudioFileRequest setTranscriptionFormat(AudioTranscriptionFormat transcriptionFormat) {
        this.transcriptionFormat = transcriptionFormat;
        return this;
    }

    public AudioTranslationFormat getTranslationFormat() {
        return translationFormat;
    }

    public UploadAudioFileRequest setTranslationFormat(AudioTranslationFormat translationFormat) {
        this.translationFormat = translationFormat;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public UploadAudioFileRequest setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public UploadAudioFileRequest setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public String getModel() {
        return model;
    }

    public UploadAudioFileRequest setModel(String model) {
        this.model = model;
        return this;
    }

    public UploadAudioFileRequest(FileDetails file) {
        this.file = file;
    }
}
