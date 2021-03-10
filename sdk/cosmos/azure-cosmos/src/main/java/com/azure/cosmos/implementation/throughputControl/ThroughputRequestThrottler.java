// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is the place where we tracking the RU usage, and make decision whether we should block the request.
 */
public class ThroughputRequestThrottler {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputRequestThrottler.class);

    private final AtomicReference<Double> availableThroughput;
    private final AtomicReference<Double> scheduledThroughput;
    private final ReentrantReadWriteLock.WriteLock throughputWriteLock;
    private final ReentrantReadWriteLock.ReadLock throughputReadLock;

    public ThroughputRequestThrottler(double scheduledThroughput) {
        this.availableThroughput = new AtomicReference<>(scheduledThroughput);
        this.scheduledThroughput = new AtomicReference<>(scheduledThroughput);
        ReentrantReadWriteLock throughputReadWriteLock = new ReentrantReadWriteLock();
        this.throughputWriteLock = throughputReadWriteLock.writeLock();
        this.throughputReadLock = throughputReadWriteLock.readLock();
    }

    public double renewThroughputUsageCycle(double scheduledThroughput) {
        try {
            this.throughputWriteLock.lock();
            double throughputUsagePercentage = (this.scheduledThroughput.get() - this.availableThroughput.get()) / this.scheduledThroughput.get();
            this.scheduledThroughput.set(scheduledThroughput);
            this.updateAvailableThroughput();

            return throughputUsagePercentage;
        } finally {
            this.throughputWriteLock.unlock();
        }
    }

    private void updateAvailableThroughput() {
        // The base rule is: If RU is overused during the current cycle, the over used part will be deducted from the next cyclle
        // If RU is not fully utilized during the current cycle, it will be voided.
        this.availableThroughput.getAndAccumulate(this.scheduledThroughput.get(), (available, refill) -> Math.min(available,0) + refill);
    }

    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        try {
            this.throughputReadLock.lock();
            if (this.availableThroughput.get() > 0) {
                return originalRequestMono
                    .doOnSuccess(response -> this.trackRequestCharge(response))
                    .doOnError(throwable -> this.trackRequestCharge(throwable));
            } else {
                // there is no enough throughput left, block request
                RequestRateTooLargeException requestRateTooLargeException = new RequestRateTooLargeException();

                int backoffTimeInMilliSeconds = (int)Math.floor(Math.abs(this.availableThroughput.get() * 1000 / this.scheduledThroughput.get()));

                requestRateTooLargeException.getResponseHeaders().put(
                    HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                    String.valueOf(backoffTimeInMilliSeconds));

                requestRateTooLargeException.getResponseHeaders().put(
                    HttpConstants.HttpHeaders.SUB_STATUS,
                    String.valueOf(HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_REQUEST_RATE_TOO_LARGE));

                if (request.requestContext != null) {
                    BridgeInternal.setResourceAddress(requestRateTooLargeException, request.requestContext.resourcePhysicalAddress);
                }

                return Mono.error(requestRateTooLargeException);
            }
        } finally {
            this.throughputReadLock.unlock();
        }

    }

    private <T> void trackRequestCharge (T response) {
        try {
            // Read lock is enough here.
            this.throughputReadLock.lock();
            double requestCharge = 0;
            if (response instanceof StoreResponse) {
                requestCharge = ((StoreResponse)response).getRequestCharge();
            } else if (response instanceof RxDocumentServiceResponse) {
                requestCharge = ((RxDocumentServiceResponse)response).getRequestCharge();
            } else if (response instanceof Throwable) {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap((Throwable) response), CosmosException.class);
                if (cosmosException != null) {
                    requestCharge = cosmosException.getRequestCharge();
                }
            }
            this.availableThroughput.getAndAccumulate(requestCharge, (available, consumed) -> available - consumed);
        } finally {
            this.throughputReadLock.unlock();
        }

    }

    public double getAvailableThroughput() {
        return this.availableThroughput.get();
    }

    public double getScheduledThroughput() {
        return this.scheduledThroughput.get();
    }
}
