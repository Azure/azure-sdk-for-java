// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.CategoryType;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The Azure {@link DiagnosticSettingsCategory} wrapper class implementation. */
class DiagnosticSettingsCategoryImpl extends WrapperImpl<DiagnosticSettingsCategoryResourceInner>
    implements DiagnosticSettingsCategory {

    DiagnosticSettingsCategoryImpl(DiagnosticSettingsCategoryResourceInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public CategoryType type() {
        return this.innerModel().categoryType();
    }
}
