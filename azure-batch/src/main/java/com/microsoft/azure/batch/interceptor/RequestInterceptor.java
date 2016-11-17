/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

/**
 * This class enables an interceptor to modify a request
 */
public class RequestInterceptor extends BatchClientBehavior {
    private BatchRequestInterceptHandler _handler;

    /**
     * Initializes a new instance of RequestInterceptor.
     */
    public RequestInterceptor() {
        this._handler = new BatchRequestInterceptHandler() {

            @Override
            public void modify(Object request) {
                // DO NOTHING
            }
        };
    }

    /**
     * Initializes a new instance of RequestInterceptor.
     *
     * @param handler the interceptor for the instance
     */
    public RequestInterceptor(BatchRequestInterceptHandler handler) {
        this._handler = handler;
    }

    /**
     * Gets the BatchRequestInterceptHandler.
     *
     * @return The BatchRequestInterceptHandler
     */
    public BatchRequestInterceptHandler handler() {
        return _handler;
    }

    /**
     * Sets the BatchRequestInterceptHandler.
     *
     * @param handler the BatchRequestInterceptHandler
     * @return The instance of RequestInterceptor
     */
    public RequestInterceptor withHandler(BatchRequestInterceptHandler handler) {
        this._handler = handler;
        return this;
    }
}
