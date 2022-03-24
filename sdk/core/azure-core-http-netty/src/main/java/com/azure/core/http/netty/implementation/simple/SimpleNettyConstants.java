// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import io.netty.util.AttributeKey;

public final class SimpleNettyConstants {
    public static final AttributeKey<SimpleRequestContext> REQUEST_CONTEXT_KEY =
        AttributeKey.newInstance("com.azure.core.simple.netty.request.context.key");

    private SimpleNettyConstants() {
    }
}
