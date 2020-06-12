package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

/**
 * Options that may be passed when using recognize API's on Form Recognizer client.
 */
@Fluent
public class RecognizeOptions {
    private FormContentType formContentType;
    private boolean includeTextContent;
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private InputStream form;
    private Flux<ByteBuffer> formData;
    private long length;
    private String formUrl;

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param form The {@code InputStream data} of the form to recognize form information from.
     * @param length the exact length of the provided form data.
     */
    public RecognizeOptions(final InputStream form, final long length) {
        this.form = Objects.requireNonNull(form, "'form' cannot be null");
        this.length = length;
    }

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param formData The {@code ByteBuffer data} of the form to recognize form information from.
     * @param length the exact length of the provided form data.
     */
    public RecognizeOptions(final Flux<ByteBuffer> formData, final long length) {
        this.formData = Objects.requireNonNull(formData, "'formData' cannot be null");
        this.length = length;
    }

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param formUrl The source URL to the input form.
     */
    public RecognizeOptions(final String formUrl) {
        this.formUrl = Objects.requireNonNull(formUrl, "'formUrl' cannot be null");
    }

    /**
     * Get the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return the {@code formContentType} value.
     */
    public FormContentType getFormContentType() {
        return formContentType;
    }

    /**
     * Get the boolean which specifies if to include text lines and element references in the result.
     *
     * @return the {@code isIncludeTextContent} value.
     */
    public boolean isIncludeTextContent() {
        return includeTextContent;
    }

    /**
     * Get the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return the {@code pollInterval} value.
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * Get the {@code InputStream data} of the form to recognize form information from.
     *
     * @return the {@code form} value.
     */
    public InputStream getForm() {
        return form;
    }

    /**
     * Get the the source URL to the input form.
     *
     * @return the {@code formUrl} value.
     */
    public String getFormUrl() {
        return formUrl;
    }

    /**
     * Get the exact length of the provided form data. Size of the file must be less than 50 MB.
     *
     * @return the {@code length} value.
     */
    public long getLength() {
        return length;
    }

    /**
     * Get the {@code ByteBuffer data} of the form to recognize form information from.
     *
     * @return the {@code formData} value.
     */
    public Flux<ByteBuffer> getFormData() {
        return formData;
    }

    /**
     * Set the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return the {@code RecognizeOptions} object itself.
     */
    public RecognizeOptions setFormContentType(final FormContentType formContentType) {
        this.formContentType = formContentType;
        return this;
    }

    /**
     * Set the boolean which specifies if to include text lines and element references in the result.
     *
     * @return the {@code isIncludeTextContent} value.
     */
    public RecognizeOptions setIncludeTextContent(final boolean includeTextContent) {
        this.includeTextContent = includeTextContent;
        return this;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return the {@code pollInterval} value.
     */
    public RecognizeOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }
}
