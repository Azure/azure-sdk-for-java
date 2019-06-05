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
 *
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd;

import com.google.common.base.Stopwatch;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequestArgs {

    private static final String className = RntbdRequestArgs.class.getCanonicalName();

    private final UUID activityId;
    private final long birthTime;
    private final Stopwatch lifetime;
    private final URI physicalAddress;
    private final String replicaPath;
    private final RxDocumentServiceRequest serviceRequest;

    public RntbdRequestArgs(final RxDocumentServiceRequest serviceRequest, final URI physicalAddress) {
        this.activityId = UUID.fromString(serviceRequest.getActivityId());
        this.birthTime = System.nanoTime();
        this.lifetime = Stopwatch.createStarted();
        this.physicalAddress = physicalAddress;
        this.replicaPath = StringUtils.stripEnd(physicalAddress.getPath(), "/");
        this.serviceRequest = serviceRequest;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    public long getBirthTime() {
        return this.birthTime;
    }

    public Duration getLifetime() {
        return this.lifetime.elapsed();
    }

    public String getReplicaPath() {
        return this.replicaPath;
    }

    public URI getPhysicalAddress() {
        return this.physicalAddress;
    }

    public RxDocumentServiceRequest getServiceRequest() {
        return this.serviceRequest;
    }

    @Override
    public String toString() {
        return '[' + RntbdRequestArgs.className + '(' + this.serviceRequest.getActivityId()
            + ", origin: " + this.physicalAddress.getScheme() + "://" + this.physicalAddress.getAuthority()
            + ", operationType: " + this.serviceRequest.getOperationType()
            + ", resourceType: " + this.serviceRequest.getResourceType()
            + ", replicaPath: " + this.replicaPath
            + ", lifetime: " + this.getLifetime()
            + ")]";
    }

    public void traceOperation(final Logger logger, final ChannelHandlerContext context, final String operationName, final Object... args) {

        checkNotNull(logger, "logger");

        if (logger.isTraceEnabled()) {
            final BigDecimal lifetime = BigDecimal.valueOf(this.lifetime.elapsed().toNanos(), 6);
            logger.trace("{},{},\"{}({})\",\"{}\",\"{}\"", this.birthTime, lifetime, operationName,
                Stream.of(args).map(arg ->
                    arg == null ? "null" : arg.toString()).collect(Collectors.joining(",")
                ),
                this, context
            );
        }
    }
}
