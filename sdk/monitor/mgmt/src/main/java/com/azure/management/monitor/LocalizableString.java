/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.management.monitor.models.LocalizableStringInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * The localizable string class.
 */
public interface LocalizableString
        extends HasInner<LocalizableStringInner> {
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
