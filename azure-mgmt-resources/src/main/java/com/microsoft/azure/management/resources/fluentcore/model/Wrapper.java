/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

/**
 * The wrapper around an inner object providing extended functionalities.
 *
 * @param <T> the type of the inner object.
 */
public interface Wrapper<T> {
    /**
     * @return wrapped inner object providing direct access to the underlying
     * auto-generated API implementation, based on Azure REST API
     */
    T inner();

    /**
     * Set the wrapped inner model.
     * (For internal use only)
     *
     * @param inner the new inner model
     */
    void setInner(T inner);
}
