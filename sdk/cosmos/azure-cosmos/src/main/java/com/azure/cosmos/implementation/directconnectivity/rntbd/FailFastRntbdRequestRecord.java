// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.http.HttpHeaders;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public class FailFastRntbdRequestRecord extends RntbdRequestRecord {
    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestRecord.class);

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
        AtomicInteger concurrentRequests,
        RntbdMetrics metrics,
        SocketAddress remoteAddress) {

        FailFastRntbdRequestRecord failFastRecord = new FailFastRntbdRequestRecord(args);

        logger.debug(
            "\n  [{}]\n  {}\n  created FailFastRntbdRequestRecord {} ",
            failFastRecord,
            args,
            concurrentRequests);

        final String reason = lenientFormat(
            "Failed due to too many (%s) concurrent requests.",
            concurrentRequestsSnapshot);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, failFastRecord.activityId().toString());

        failFastRecord.whenComplete((response, error) -> {
            concurrentRequests.decrementAndGet();
            metrics.markComplete(failFastRecord);
        });

        final RequestTimeoutException requestTimeoutException = new RequestTimeoutException(
            reason,
            headers,
            remoteAddress);

        BridgeInternal.setRequestHeaders(requestTimeoutException, args.serviceRequest().getHeaders());
        failFastRecord.completeExceptionally(requestTimeoutException);

        return failFastRecord;
    }
}
