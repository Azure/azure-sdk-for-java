// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestFramer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * This class is copied from {@link RntbdRequestFramer}.
 */
public class ServerRntbdRequestFramer extends LengthFieldBasedFrameDecoder {
    public ServerRntbdRequestFramer() {
        super(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 0, Integer.BYTES, -Integer.BYTES, 0, true);
    }
}
