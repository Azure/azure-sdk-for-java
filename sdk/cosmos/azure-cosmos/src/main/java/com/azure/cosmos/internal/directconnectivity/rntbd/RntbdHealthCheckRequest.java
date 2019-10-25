// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

final class RntbdHealthCheckRequest {

    public static final ByteBuf MESSAGE = Unpooled.EMPTY_BUFFER;

    private RntbdHealthCheckRequest() {
    }
}
