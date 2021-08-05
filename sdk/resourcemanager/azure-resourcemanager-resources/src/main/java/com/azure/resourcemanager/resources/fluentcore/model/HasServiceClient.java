// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * The wrapper around a service client providing extended functionalities.
 *
 * @param <T> the type of the service client.
 */
public interface HasServiceClient<T> {
    /**
     * @return wrapped service client providing direct access to the underlying
     * auto-generated API implementation, based on Azure REST API
     */
    T serviceClient();
}
