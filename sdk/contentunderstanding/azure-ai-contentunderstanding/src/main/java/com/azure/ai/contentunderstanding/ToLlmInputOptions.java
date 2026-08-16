// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding;

import com.azure.ai.contentunderstanding.models.AnalysisResult;

/**
 * Options for {@link LlmInputHelper#toLlmInput(AnalysisResult, java.util.Map, ToLlmInputOptions)}.
 */
public final class ToLlmInputOptions {

    private boolean includeFields = true;
    private boolean includeMarkdown = true;

    /**
     * Creates a new instance with default options: both fields and markdown included.
     */
    public ToLlmInputOptions() {
    }

    /**
     * Whether to include structured fields in the output. Defaults to {@code true}.
     * Set to {@code false} for markdown-only output (smaller token footprint, no structured data).
     *
     * @return whether fields are included.
     */
    public boolean isIncludeFields() {
        return includeFields;
    }

    /**
     * Sets whether to include structured fields in the output.
     *
     * @param includeFields whether to include fields.
     * @return this options instance.
     */
    public ToLlmInputOptions setIncludeFields(boolean includeFields) {
        this.includeFields = includeFields;
        return this;
    }

    /**
     * Whether to include markdown content in the output. Defaults to {@code true}.
     * Set to {@code false} for fields-only output.
     *
     * @return whether markdown is included.
     */
    public boolean isIncludeMarkdown() {
        return includeMarkdown;
    }

    /**
     * Sets whether to include markdown content in the output.
     *
     * @param includeMarkdown whether to include markdown.
     * @return this options instance.
     */
    public ToLlmInputOptions setIncludeMarkdown(boolean includeMarkdown) {
        this.includeMarkdown = includeMarkdown;
        return this;
    }
}
