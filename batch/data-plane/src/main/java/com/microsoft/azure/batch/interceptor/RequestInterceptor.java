// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

/**
 * A {@link BatchClientBehavior} that modifies requests to the Batch service.
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
     * @param handler The handler which will intercept requests to the Batch service.
     */
    public RequestInterceptor(BatchRequestInterceptHandler handler) {
        this._handler = handler;
    }

    /**
     * Gets the handler which will intercept requests to the Batch service.
     *
     * @return The handler which will intercept requests to the Batch service.
     */
    public BatchRequestInterceptHandler handler() {
        return _handler;
    }

    /**
     * Sets the handler which will intercept requests to the Batch service.
     *
     * @param handler The handler which will intercept requests to the Batch service.
     * @return The current instance.
     */
    public RequestInterceptor withHandler(BatchRequestInterceptHandler handler) {
        this._handler = handler;
        return this;
    }
}
