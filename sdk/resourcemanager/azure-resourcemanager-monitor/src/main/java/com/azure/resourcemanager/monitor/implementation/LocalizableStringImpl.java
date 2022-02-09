// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.fluent.models.LocalizableStringInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The {@link LocalizableString} wrapper class implementation. */
class LocalizableStringImpl extends WrapperImpl<LocalizableStringInner> implements LocalizableString {

    LocalizableStringImpl(LocalizableStringInner innerObject) {
        super(innerObject);
    }

    @Override
    public String value() {
        return this.innerModel().value();
    }

    @Override
    public String localizedValue() {
        return this.innerModel().localizedValue();
    }
}
