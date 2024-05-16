// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

/**
 * A {@link BatchClientBehavior} that modifies requests to the Batch service.
 */
public class RequestInterceptor extends BatchClientBehavior {
    private BatchRequestInterceptHandler handler;

    /**
     * Initializes a new instance of RequestInterceptor.
     */
    public RequestInterceptor() {
        this.handler = new NoOpInterceptHandler();
    }

    /**
     * Initializes a new instance of RequestInterceptor.
     *
     * @param handler The handler which will intercept requests to the Batch service.
     */
    public RequestInterceptor(BatchRequestInterceptHandler handler) {
        this.handler = handler;
    }

    /**
     * Gets the handler which will intercept requests to the Batch service.
     *
     * @return The handler which will intercept requests to the Batch service.
     */
    public BatchRequestInterceptHandler handler() {
        return handler;
    }

    /**
     * Sets the handler which will intercept requests to the Batch service.
     *
     * @param handler The handler which will intercept requests to the Batch service.
     * @return The current instance.
     */
    public RequestInterceptor withHandler(BatchRequestInterceptHandler handler) {
        this.handler = handler;
        return this;
    }

    private static class NoOpInterceptHandler implements BatchRequestInterceptHandler {
        @Override
        public void modify(Object request) {
            // DO NOTHING
        }
    }
}
