// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.monitor;

import com.azure.management.monitor.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * The Azure event log entries are of type DiagnosticSettingsCategory.
 */
public interface DiagnosticSettingsCategory
        extends HasInner<DiagnosticSettingsCategoryResourceInner> {
    /**
     * Get the diagnostic settings category name value.
     *
     * @return the diagnostic settings category name
     */
    String name();

    /**
     * Get the categoryType value.
     *
     * @return the categoryType value
     */
    CategoryType type();
}
