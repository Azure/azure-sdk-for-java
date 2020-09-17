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
        final RequestTimeoutException requestTimeoutException = new RequestTimeoutException(
            reason,
            headers,
            remoteAddress);
        BridgeInternal.setRequestHeaders(requestTimeoutException, args.serviceRequest().getHeaders());

        failFastRecord.whenComplete((response, error) -> {
            metrics.markComplete(failFastRecord);
        });
        failFastRecord.completeExceptionally(requestTimeoutException);

        return failFastRecord;
    }
}
