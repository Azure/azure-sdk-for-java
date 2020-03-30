/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.CategoryType;
import com.azure.management.monitor.DiagnosticSettingsCategory;
import com.azure.management.monitor.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The Azure {@link DiagnosticSettingsCategory} wrapper class implementation.
 */
class DiagnosticSettingsCategoryImpl
        extends WrapperImpl<DiagnosticSettingsCategoryResourceInner> implements DiagnosticSettingsCategory {

    DiagnosticSettingsCategoryImpl(DiagnosticSettingsCategoryResourceInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public CategoryType type() {
        return this.inner().categoryType();
    }
}
