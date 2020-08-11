// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.fluent.inner.LocalizableStringInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** The localizable string class. */
public interface LocalizableString extends HasInner<LocalizableStringInner> {
    /**
     * Get the value value.
     *
     * @return the value value
     */
    String value();

    /**
     * Get the localizedValue value.
     *
     * @return the localizedValue value
     */
    String localizedValue();
}
