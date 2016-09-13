/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

public class RequestInterceptor extends BatchClientBehavior {
    private BatchRequestInterceptHandler _handler;

    public RequestInterceptor() {
        this._handler = new BatchRequestInterceptHandler() {

            @Override
            public void modify(Object request) {
                // DO NOTHING
            }
        };
    }

    public RequestInterceptor(BatchRequestInterceptHandler handler) {
        this._handler = handler;
    }

    public BatchRequestInterceptHandler handler() {
        return _handler;
    }

    public RequestInterceptor withHandler(BatchRequestInterceptHandler handler) {
        this._handler = handler;
        return this;
    }
}
