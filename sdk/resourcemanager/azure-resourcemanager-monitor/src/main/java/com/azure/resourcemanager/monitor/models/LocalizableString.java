// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.fluent.models.LocalizableStringInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** The localizable string class. */
public interface LocalizableString extends HasInnerModel<LocalizableStringInner> {
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
