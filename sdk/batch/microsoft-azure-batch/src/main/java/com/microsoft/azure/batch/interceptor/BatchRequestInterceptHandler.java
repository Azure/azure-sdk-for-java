// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

/**
 * This interface enables an interceptor to modify a request to the Batch service.
 */
public interface BatchRequestInterceptHandler {
    /**
     * Modifies the request to the Batch service.
     *
     * @param request The outgoing Batch service request.
     */
    void modify(Object request);
}
