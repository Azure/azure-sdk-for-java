// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link RecognizeCustomEntitiesOptions} model.
 */
@Fluent
public final class RecognizeCustomEntitiesOptions {
    private String autoDetectionDefaultLanguage;
    private String displayName;
    private boolean includeStatistics;
    private boolean disableServiceLogs;

    /**
     * Gets auto detection fallback language code.
     *
     * @return Auto detection fallback language code.
     */
    public String getAutoDetectionDefaultLanguage() {
        return autoDetectionDefaultLanguage;
    }

    /**
     * Sets auto detection fallback language code.
     *
     * @param language Auto detection fallback language code.
     *
     * @return The {@link RecognizeCustomEntitiesOptions} object itself.
     */
    public RecognizeCustomEntitiesOptions setAutoDetectionDefaultLanguage(String language) {
        autoDetectionDefaultLanguage = language;
        return this;
    }

    /**
     * Gets display name of the operation.
     *
     * @return Display name of the operation.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets display name of the operation.
     *
     * @param displayName Display name of the operation.
     *
     * @return The {@link RecognizeCustomEntitiesOptions} object itself.
     */
    public RecognizeCustomEntitiesOptions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the value of {@code includeStatistics}.
     *
     * @return The value of {@code includeStatistics}.
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * Set the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link RecognizeCustomEntitiesOptions} object itself.
     */
    public RecognizeCustomEntitiesOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }

    /**
     * Gets the value of {@code disableServiceLogs}.
     *
     * @return The value of {@code disableServiceLogs}. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Sets the value of {@code disableServiceLogs}.
     *
     * @param disableServiceLogs The default value of this property is 'false'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link MultiLabelClassifyAction} object itself.
     */
    public RecognizeCustomEntitiesOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }
}
