package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

@Fluent
public class RecognizeOptions {
    private FormContentType formContentType;
    private boolean includeTextContent;
    private Duration pollInterval;
    private InputStream form;
    private Flux<ByteBuffer> formData;
    private long length;
    private String formUrl;
    static final Duration DEFAULT_DURATION = Duration.ofSeconds(5);

    public RecognizeOptions(final InputStream form, final long length) {
        this.form = Objects.requireNonNull(form, "'form' cannot be null");
        this.length = length;
    }

    public RecognizeOptions(final Flux<ByteBuffer> formData, final long length) {
        this.formData = Objects.requireNonNull(formData, "'formData' cannot be null");
        this.length = length;
    }

    public RecognizeOptions(final String formUrl) {
        this.formUrl = Objects.requireNonNull(formUrl, "'formUrl' cannot be null");
    }

    public FormContentType getFormContentType() {
        return formContentType;
    }

    public boolean isIncludeTextContent() {
        return includeTextContent;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public InputStream getForm() {
        return form;
    }

    public String getFormUrl() {
        return formUrl;
    }

    public long getLength() {
        return length;
    }

    public Flux<ByteBuffer> getFormData() {
        return formData;
    }

    public RecognizeOptions setFormContentType(final FormContentType formContentType) {
        this.formContentType = formContentType;
        return this;
    }

    public RecognizeOptions setIncludeTextContent(final boolean includeTextContent) {
        this.includeTextContent = includeTextContent;
        return this;
    }

    public RecognizeOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_DURATION : pollInterval;
        return this;
    }
}
