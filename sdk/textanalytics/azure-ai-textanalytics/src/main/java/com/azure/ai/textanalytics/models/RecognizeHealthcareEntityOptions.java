// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link RecognizePiiEntityOptions} model.
 */
@Fluent
public class RecognizeHealthcareEntityOptions extends TextAnalyticsRequestOptions {
    private StringIndexType stringIndexType;

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link RecognizeHealthcareEntityOptions} object itself.
     */
    @Override
    public RecognizeHealthcareEntityOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Set the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link RecognizeHealthcareEntityOptions} object itself.
     */
    @Override
    public RecognizeHealthcareEntityOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Get the value of stringIndexType.
     *
     * @return The value of domainFilter.
     */
    public StringIndexType getStringIndexType() {
        return stringIndexType;
    }

    /**
     * Set the value of domainFilter.
     *
     * @param stringIndexType It spe
     *
     * @return The RecognizeHealthcareEntityOptions object itself.
     */
    public RecognizeHealthcareEntityOptions setStringIndexType(StringIndexType stringIndexType) {
        this.stringIndexType = stringIndexType;
        return this;
    }
}
