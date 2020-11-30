// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.http.HttpHeaders;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public class FailFastRntbdRequestRecord extends RntbdRequestRecord {
    private static final Logger logger = LoggerFactory.getLogger(FailFastRntbdRequestRecord.class);

    private FailFastRntbdRequestRecord(final RntbdRequestArgs args) {
        super(args);
    }

    @Override
    public Timeout newTimeout(final TimerTask task) {
        throw new IllegalArgumentException("newTimeout must never be called for fail fast records.");
    }

    public static FailFastRntbdRequestRecord createAndFailFast(
        RntbdRequestArgs args,
        long concurrentRequestsSnapshot,
        RntbdMetrics metrics,
        SocketAddress remoteAddress) {

        FailFastRntbdRequestRecord failFastRecord = new FailFastRntbdRequestRecord(args);

        logger.debug(
            "\n  [{}]\n  {}\n  created FailFastRntbdRequestRecord {} ",
            failFastRecord,
            args,
            concurrentRequestsSnapshot);

        final String reason = lenientFormat(
            "Failed due to too many (%s) concurrent requests.",
            concurrentRequestsSnapshot);
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, failFastRecord.activityId().toString());

        // When admission control blocks a request due to excessive pendingAcquisition queue length
        // the error should be handled upstream as a transient connectivity error for which we know
        // the request was never flushed to the wire - which means retries are functionally safe for both
        // reads and writes
        final GoneException admissionControlBlocksRequestException = new GoneException(
            reason,
            headers,
            remoteAddress);
        BridgeInternal.setRequestHeaders(admissionControlBlocksRequestException, args.serviceRequest().getHeaders());

        failFastRecord.whenComplete((response, error) -> {
            metrics.markComplete(failFastRecord);
        });
        failFastRecord.completeExceptionally(admissionControlBlocksRequestException);

        return failFastRecord;
    }
}
