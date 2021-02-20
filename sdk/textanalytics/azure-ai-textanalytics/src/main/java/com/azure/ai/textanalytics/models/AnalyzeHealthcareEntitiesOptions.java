// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeHealthcareEntitiesOptions} model.
 */
@Fluent
public final class AnalyzeHealthcareEntitiesOptions extends TextAnalyticsRequestOptions {
    private StringIndexType stringIndexType;

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    @Override
    public AnalyzeHealthcareEntitiesOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Set the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    @Override
    public AnalyzeHealthcareEntitiesOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Get the value of {@code stringIndexType}.
     *
     * @return The value of {@code stringIndexType}.
     */
    public StringIndexType getStringIndexType() {
        return stringIndexType;
    }

    /**
     * Set the value of {@code stringIndexType}.
     * The {@link StringIndexType#UTF16CODE_UNIT} will be used as default type if there is no value assign to it.
     *
     * @param stringIndexType It used to set the value of string indexing type.
     *
     * @return the {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    public AnalyzeHealthcareEntitiesOptions setStringIndexType(StringIndexType stringIndexType) {
        this.stringIndexType = stringIndexType;
        return this;
    }
}
