package com.azure.ai.formrecognizer.models;

import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

public class RecognizeCustomFormsOptions extends RecognizeOptions {
    private final String modelId;

    public RecognizeCustomFormsOptions(final InputStream form, final long length, final String modelId) {
        super(form, length);
        this.modelId = Objects.requireNonNull(modelId, "'modelId' cannot be null");
    }

    public RecognizeCustomFormsOptions(final Flux<ByteBuffer> formData, final long length, final String modelId) {
        super(formData, length);
        this.modelId = Objects.requireNonNull(modelId, "'modelId' cannot be null");
    }

    public RecognizeCustomFormsOptions(final String formUrl, final String modelId) {
        super(formUrl);
        this.modelId = Objects.requireNonNull(modelId, "'modelId' cannot be null");
    }

    public String getModelId() {
        return modelId;
    }

    @Override
    public RecognizeCustomFormsOptions setContentType(FormContentType contentType) {
        return (RecognizeCustomFormsOptions) super.setContentType(contentType);
    }

    @Override
    public RecognizeOptions setIncludeTextContent(boolean includeTextContent) {
        return super.setIncludeTextContent(includeTextContent);
    }

    @Override
    public RecognizeOptions setPollInterval(Duration pollInterval) {
        return super.setPollInterval(pollInterval);
    }
}
