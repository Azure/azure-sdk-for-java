// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;

public class RntbdOpenChannelPromise extends ChannelPromiseWithExpiryTime {
    public RntbdOpenChannelPromise(Promise<Channel> channelPromise, long expiryTimeInNanos) {
        super(channelPromise, expiryTimeInNanos);
    }
}
