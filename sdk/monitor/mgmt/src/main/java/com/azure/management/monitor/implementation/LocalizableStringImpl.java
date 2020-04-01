/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.LocalizableString;
import com.azure.management.monitor.models.LocalizableStringInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The {@link LocalizableString} wrapper class implementation.
 */
class LocalizableStringImpl
        extends WrapperImpl<LocalizableStringInner> implements LocalizableString {

    LocalizableStringImpl(LocalizableStringInner innerObject) {
        super(innerObject);
    }

    @Override
    public String value() {
        return this.inner().value();
    }

    @Override
    public String localizedValue() {
        return this.inner().localizedValue();
    }
}
