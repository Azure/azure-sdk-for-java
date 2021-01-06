// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThroughputRequestAuthorizer {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputRequestAuthorizer.class);

    private final AtomicReference<Double> availableThroughput;
    private final AtomicReference<Double> scheduledThroughput;
    private final AtomicInteger rejectedRequests;
    private final AtomicInteger totalRequests;

    public ThroughputRequestAuthorizer(double scheduledThroughput) {
        this.availableThroughput = new AtomicReference<>(0d);
        this.scheduledThroughput = new AtomicReference<>(scheduledThroughput);
        this.rejectedRequests = new AtomicInteger(0);
        this.totalRequests = new AtomicInteger(0);
    }

    public Mono<ThroughputRequestAuthorizer> init() {
        // No-overflow: the availableThroughput will never be larger than scheduled throughput
        // But if RU is over used in one cycle, the over used RU will be rolled over to the next cycle
        this.updateAvailableThroughput();
        return Mono.just(this);
    }

    public Mono<Void> renewThroughputUsageCycle(double scheduledThroughput) {
        this.scheduledThroughput.set(scheduledThroughput);
        this.updateAvailableThroughput();
        return Mono.empty();
    }

    private void updateAvailableThroughput() {
        this.availableThroughput.getAndAccumulate(this.scheduledThroughput.get(), (available, refill) -> Math.min(available,0) + refill);
    }

    public <T> Mono<T> authorize(Mono<T> nextRequestMono) {
        if (this.availableThroughput.get() > 0) {
            return nextRequestMono
                .doOnSuccess(response -> {
                    this.trackRequestCharge(response);
                    this.totalRequests.getAndAdd(1);
                })
                .doOnError(throwable -> {
                    this.trackRequestCharge(throwable);
                    this.totalRequests.getAndAdd(1);
                });
        } else {
            // there is no enough throughput left, block request
            this.totalRequests.getAndAdd(1);
            this.rejectedRequests.getAndAdd(1);

            RequestRateTooLargeException requestRateTooLargeException = new RequestRateTooLargeException();

            int backoffTimeInMilliSeconds = (int)Math.floor(Math.abs(this.availableThroughput.get() * 1000 / this.scheduledThroughput.get()));
            requestRateTooLargeException.getResponseHeaders().put(
                HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                String.valueOf(backoffTimeInMilliSeconds));

            requestRateTooLargeException.getResponseHeaders().put(
                HttpConstants.HttpHeaders.SUB_STATUS,
                String.valueOf(HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_GROUP_REQUEST_RATE_TOO_LARGE));

            requestRateTooLargeException.getResponseHeaders().put(HttpConstants.HttpHeaders.LSN, "0");

            return Mono.error(requestRateTooLargeException);
        }
    }

    private <T> void trackRequestCharge (T response) {
        double requestCharge = 0;
        if (response instanceof StoreResponse) {
            requestCharge = ((StoreResponse)response).getRequestCharge();
        } else if (response instanceof RxDocumentServiceResponse) {
            requestCharge = ((RxDocumentServiceResponse)response).getRequestCharge();
        } else if (response instanceof Throwable) {
            CosmosException cosmosException = Utils.as(response, CosmosException.class);
            if (cosmosException != null) {
                requestCharge = cosmosException.getRequestCharge();
            }
        }
        this.availableThroughput.getAndAccumulate(requestCharge, (available, consumed) -> available - consumed);
    }
}
