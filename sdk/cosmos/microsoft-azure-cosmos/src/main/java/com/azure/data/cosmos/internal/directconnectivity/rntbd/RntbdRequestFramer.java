// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public final class RntbdRequestFramer extends LengthFieldBasedFrameDecoder {

    public RntbdRequestFramer() {
        super(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 0, Integer.BYTES, -Integer.BYTES, 0, true);
    }
}
