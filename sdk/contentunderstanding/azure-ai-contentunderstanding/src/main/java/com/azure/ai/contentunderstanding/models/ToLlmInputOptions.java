// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import java.util.Map;

/**
 * Options for {@link LlmInputHelper#toLlmInput(AnalysisResult, ToLlmInputOptions)}.
 */
public final class ToLlmInputOptions {

    private boolean includeFields = true;
    private boolean includeMarkdown = true;
    private Map<String, Object> metadata;

    /**
     * Creates a new instance with default options: both fields and markdown included,
     * no metadata.
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

    /**
     * Optional user-supplied key/value pairs to include in the YAML front matter.
     * Common keys include {@code "source"} (filename), {@code "department"},
     * {@code "batchId"}, etc.
     *
     * <p>Metadata keys are placed after {@code contentType} and before auto-detected
     * keys ({@code timeRange}, {@code category}, {@code pages}).
     *
     * <p>Metadata keys must not conflict with helper-generated front matter keys:
     * {@code contentType}, {@code timeRange}, {@code category}, {@code pages},
     * {@code fields}, or {@code rai_warnings}.
     *
     * @return the metadata map, or {@code null} if not set.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets optional user-supplied metadata to include in the YAML front matter.
     *
     * @param metadata the metadata map. Keys must not conflict with reserved front
     *     matter keys.
     * @return this options instance.
     */
    public ToLlmInputOptions setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}
