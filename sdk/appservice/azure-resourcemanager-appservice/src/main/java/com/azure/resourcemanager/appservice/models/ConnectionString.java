// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;

/** An immutable client-side representation of a connection string on a web app. */
@Fluent
public interface ConnectionString {
    /**
     * Gets the key of the setting.
     *
     * @return the key of the setting
     */
    String name();

    /**
     * Gets the value of the connection string.
     *
     * @return the value of the connection string
     */
    String value();

    /**
     * Gets the type of the connection string.
     *
     * @return the type of the connection string
     */
    ConnectionStringType type();

    /**
     * Check whether the connection string sticks to the slot during a swap.
     *
     * @return if the connection string sticks to the slot during a swap
     */
    boolean sticky();
}
