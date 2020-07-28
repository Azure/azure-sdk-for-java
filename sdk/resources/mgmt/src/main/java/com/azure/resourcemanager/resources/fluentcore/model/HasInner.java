// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * The wrapper around an inner object providing extended functionalities.
 *
 * @param <T> the type of the inner object.
 */
public interface HasInner<T> {
    /**
     * @return wrapped inner object providing direct access to the underlying
     * auto-generated API implementation, based on Azure REST API
     */
    T inner();
}
