/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

/**
 * This interface enables an interceptor to modify a request.
 */
public interface BatchRequestInterceptHandler {
    /**
     * modify the request
     *
     * @param request outgoing request
     */
    void modify(Object request);
}
