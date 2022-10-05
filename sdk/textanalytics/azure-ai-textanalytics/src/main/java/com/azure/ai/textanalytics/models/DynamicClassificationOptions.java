package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The {@link DynamicClassificationOptions} model.
 */
@Fluent
public final class DynamicClassificationOptions extends TextAnalyticsRequestOptions {
    private List<String> categories;
    private ClassificationType type;
    /**
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link DynamicClassificationOptions} object itself.
     */
    @Override
    public DynamicClassificationOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Sets the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return The {@link DynamicClassificationOptions} object itself.
     */
    @Override
    public DynamicClassificationOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Sets the value of service logs disable status.
     *
     * @param disableServiceLogs The default value of this property is 'false', except for methods like
     * 'beginAnalyzeHealthcareEntities' and 'recognizePiiEntities'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link DynamicClassificationOptions} object itself.
     */
    @Override
    public DynamicClassificationOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        super.setServiceLogsDisabled(disableServiceLogs);
        return this;
    }

    /**
     * Gets the value of {@code categories}
     *
     * @return The value of {@code categories}.
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Sets the value of {@code categories}.
     *
     * @param categories the
     *
     * @return The DynamicClassificationOptions object itself.
     */
    public DynamicClassificationOptions setCategories(List<String> categories) {
        this.categories = categories;
        return this;
    }

    public ClassificationType getClassificationType() {
        return type;
    }

    public DynamicClassificationOptions setClassificationType(ClassificationType type) {
        this.type = type;
        return this;
    }
}
