// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * Extended options that may be passed when recognizing custom form on Form Recognizer client.
 */
@Fluent
public class RecognizeCustomFormsOptions extends RecognizeOptions {
    private final String modelId;

    /**
     * Create a {@code RecognizeCustomFormsOptions option} object
     *
     * @param form The {@code InputStream data} of the form to recognize form information from.
     * @param length the exact length of the provided form data.
     * @param modelId The UUID string format custom trained model Id to be used.
     */
    public RecognizeCustomFormsOptions(final InputStream form, final long length, final String modelId) {
        super(form, length);
        this.modelId = modelId;
    }

    /**
     * Create a {@code RecognizeCustomFormsOptions option} object
     *
     * @param formData The {@code ByteBuffer data} of the form to recognize form information from.
     * @param length The exact length of the provided form data
     * @param modelId The UUID string format custom trained model Id to be used.
     */
    public RecognizeCustomFormsOptions(final Flux<ByteBuffer> formData, final long length, final String modelId) {
        super(formData, length);
        this.modelId = modelId;
    }

    /**
     * Create a {@code RecognizeCustomFormsOptions option} object
     *
     * @param formUrl The source URL to the input form.
     * @param modelId The UUID string format custom trained model Id to be used.
     */
    public RecognizeCustomFormsOptions(final String formUrl, final String modelId) {
        super(formUrl);
        this.modelId = modelId;
    }

    /**
     * Get the UUID string format custom trained model Id to be used.
     *
     * @return the {@code modelId} value
     */
    public String getModelId() {
        return modelId;
    }


    @Override
    public RecognizeCustomFormsOptions setFormContentType(FormContentType formContentType) {
        super.setFormContentType(formContentType);
        return this;
    }

    @Override
    public RecognizeCustomFormsOptions setIncludeFieldElement(boolean includeFieldElement) {
        super.setIncludeFieldElement(includeFieldElement);
        return this;
    }

    @Override
    public RecognizeCustomFormsOptions setPollInterval(Duration pollInterval) {
        super.setPollInterval(pollInterval);
        return this;
    }
}
