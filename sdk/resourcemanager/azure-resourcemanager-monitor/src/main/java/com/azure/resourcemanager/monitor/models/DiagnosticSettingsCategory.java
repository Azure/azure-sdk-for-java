// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** The Azure event log entries are of type DiagnosticSettingsCategory. */
public interface DiagnosticSettingsCategory extends HasInnerModel<DiagnosticSettingsCategoryResourceInner> {
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
