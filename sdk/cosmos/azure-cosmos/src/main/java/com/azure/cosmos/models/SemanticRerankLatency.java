// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents latency information for semantic rerank operations.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankLatency {
    @JsonProperty("data_preprocess_time")
    private double dataPreprocessTime;

    @JsonProperty("inference_time")
    private double inferenceTime;

    @JsonProperty("postprocess_time")
    private double postprocessTime;

    /**
     * Creates a new instance of SemanticRerankLatency.
     */
    public SemanticRerankLatency() {
    }

    /**
     * Gets the data preprocessing time in seconds.
     *
     * @return the preprocessing time.
     */
    public double getDataPreprocessTime() {
        return dataPreprocessTime;
    }

    /**
     * Sets the data preprocessing time in seconds.
     *
     * @param dataPreprocessTime the preprocessing time.
     */
    public void setDataPreprocessTime(double dataPreprocessTime) {
        this.dataPreprocessTime = dataPreprocessTime;
    }

    /**
     * Gets the inference time in seconds.
     *
     * @return the inference time.
     */
    public double getInferenceTime() {
        return inferenceTime;
    }

    /**
     * Sets the inference time in seconds.
     *
     * @param inferenceTime the inference time.
     */
    public void setInferenceTime(double inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    /**
     * Gets the post-processing time in seconds.
     *
     * @return the post-processing time.
     */
    public double getPostprocessTime() {
        return postprocessTime;
    }

    /**
     * Sets the post-processing time in seconds.
     *
     * @param postprocessTime the post-processing time.
     */
    public void setPostprocessTime(double postprocessTime) {
        this.postprocessTime = postprocessTime;
    }
}
