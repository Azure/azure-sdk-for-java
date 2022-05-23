// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;

public class OpenChannelPromise extends ChannelPromiseWithExpiryTime {
    public OpenChannelPromise(Promise<Channel> channelPromise, long expiryTimeInNanos) {
        super(channelPromise, expiryTimeInNanos);
    }
}
