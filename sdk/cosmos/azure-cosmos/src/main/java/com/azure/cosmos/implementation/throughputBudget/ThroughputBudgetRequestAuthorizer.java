// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThroughputBudgetRequestAuthorizer {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputBudgetRequestAuthorizer.class);

    private final AtomicReference<Double> availableThroughput;
    private final AtomicReference<Double> scheduledThroughput;
    private final AtomicInteger rejectedRequests;
    private final AtomicInteger totalRequests;

    public ThroughputBudgetRequestAuthorizer(double scheduledThroughput) {
        this.availableThroughput = new AtomicReference<>(scheduledThroughput);
        this.scheduledThroughput = new AtomicReference<>(scheduledThroughput);
        this.rejectedRequests = new AtomicInteger(0);
        this.totalRequests = new AtomicInteger(0);
    }

    public void setAvailableThroughput(double availableThroughput) {
        this.availableThroughput.set(availableThroughput);
    }

    public void setRejectedRequests(int rejectedRequests) {
        this.rejectedRequests.set(rejectedRequests);
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests.set(totalRequests);
    }

    public Mono<Void> resetThroughput(double scheduledThroughput) {
        this.scheduledThroughput.set(scheduledThroughput);
        this.availableThroughput.getAndAccumulate(scheduledThroughput, (available, refill) -> Math.min(available,0) + refill);

        return Mono.empty();
    }

    public <T> Mono<T> authorizeRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
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
            // TODO: add backoff time
            this.totalRequests.getAndAdd(1);
            this.rejectedRequests.getAndAdd(1);

            // TODO: define a new client substatus code
            RequestRateTooLargeException requestRateTooLargeException = new RequestRateTooLargeException();

            // TODO: add time buffer, so not all requests retried at the same time
            int backoffTimeInSeconds = (int)Math.abs(this.availableThroughput.get() / this.scheduledThroughput.get());
            requestRateTooLargeException.getResponseHeaders().put(
                HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                String.valueOf(backoffTimeInSeconds * 1000));

            logger.info("backoff time: " + backoffTimeInSeconds);

            requestRateTooLargeException.getResponseHeaders().put(
                HttpConstants.HttpHeaders.SUB_STATUS,
                String.valueOf(HttpConstants.SubStatusCodes.THROUGHPUT_BUDGET_GROUP_REQUEST_RATE_TOO_LARGE));

            return Mono.error(requestRateTooLargeException);
        }
    }

    public double getScheduledThoughput() { return this.scheduledThroughput.get(); }

    public double getAvailableThroughput() {
        return availableThroughput.get();
    }

    public int getRejectedRequests() {
        return rejectedRequests.get();
    }

    public int getTotalRequests() {
        return totalRequests.get();
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
        logger.info("Current available RU is: " + this.availableThroughput.get());
    }

    public Mono<Double> calculateLoadFactor() {
        return Mono.just((this.scheduledThroughput.get() - this.availableThroughput.get()) / this.scheduledThroughput.get());
    }
}
