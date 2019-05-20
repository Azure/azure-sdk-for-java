/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.RequestTimeoutException;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.StoreResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequestRecord extends CompletableFuture<StoreResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestRecord.class);

    private final RntbdRequestArgs args;
    private final RntbdRequestTimer timer;

    public RntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {

        checkNotNull(args, "args");
        checkNotNull(timer, "timer");

        this.args = args;
        this.timer = timer;
    }

    public UUID getActivityId() {
        return this.args.getActivityId();
    }

    public RntbdRequestArgs getArgs() {
        return this.args;
    }

    public long getBirthTime() {
        return this.args.getBirthTime();
    }

    public Duration getLifetime() {
        return this.args.getLifetime();
    }

    public void expire() {
        final RequestTimeoutException error = new RequestTimeoutException(
            String.format("Request timeout interval (%,d ms) elapsed",
                this.timer.getRequestTimeout(TimeUnit.MILLISECONDS)),
            this.args.getPhysicalAddress());
        BridgeInternal.setRequestHeaders(error, this.args.getServiceRequest().getHeaders());
        this.completeExceptionally(error);
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task);
    }

    @Override
    public String toString() {
        return this.args.toString();
    }
}
