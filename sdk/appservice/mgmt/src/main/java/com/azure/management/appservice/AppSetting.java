/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;

/**
 * An immutable client-side representation of an app setting on a web app.
 */
@Fluent
public interface AppSetting {
    /**
     * @return the key of the setting
     */
    String key();

    /**
     * @return the value of the setting
     */
    String value();

    /**
     * @return if the setting sticks to the slot during a swap
     */
    boolean sticky();
}
