// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class AsyncRntbdRequestRecord extends RntbdRequestRecord {

    private final RntbdRequestTimer timer;

    public AsyncRntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {
        super(args);
        checkNotNull(timer, "expected non-null timer");
        this.timer = timer;
    }

    @Override
    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task);
    }
}
