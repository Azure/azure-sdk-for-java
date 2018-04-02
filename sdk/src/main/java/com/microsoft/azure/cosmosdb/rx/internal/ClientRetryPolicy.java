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
package com.microsoft.azure.cosmosdb.rx.internal;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.RetryOptions;
import com.microsoft.azure.cosmosdb.internal.EndpointManager;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * 
 *  Client policy is combination of endpoint change retry + throttling retry.
        */
public class ClientRetryPolicy implements IDocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(ClientRetryPolicy.class);

    private final static int RetryIntervalInMS = 1000; //Once we detect failover wait for 1 second before retrying request.
    private final static int MaxRetryCount = 120;

    private final IDocumentClientRetryPolicy throttlingRetry;
    private final EndpointManager globalEndpointManager;
    private final boolean enableEndpointDiscovery;
    private int failoverRetryCount;

    private boolean useWriteEndpoint;
    private int sessionTokenRetryCount;
    private boolean isReadRequest;

    public ClientRetryPolicy(EndpointManager globalEndpointManager,
                             boolean enableEndpointDiscovery,
                             RetryOptions retryOptions) {

        this.throttlingRetry = new ResourceThrottleRetryPolicy(
                retryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                retryOptions.getMaxRetryWaitTimeInSeconds());

        this.globalEndpointManager = globalEndpointManager;
        this.failoverRetryCount = 0;
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        this.sessionTokenRetryCount = 0;
    }

    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception e) {
        // Received 403.3 on write region, initiate the endpoint re-discovery
        DocumentClientException clientException = Utils.as(e, DocumentClientException.class);
        if (clientException != null && 
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN))
        {
            logger.warn("Endpoint not writable. Refresh cache and retry");
            return this.shouldRetryOnEndpointFailureAsync();
        }

        // Regional endpoint is not available yet for reads (e.g. add/ online of region is in progress)
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND) &&
                this.isReadRequest)
        {
            logger.warn("Endpoint not available for reads. Refresh cache and retry");
            return this.shouldRetryOnEndpointFailureAsync();
        }

        // Received Connection error (HttpRequestException), initiate the endpoint rediscovery
        if (clientException != null &&
                clientException.getCause() instanceof IOException ||
                e != null && e instanceof IOException) {
            logger.warn("Endpoint not reachable. Refresh cache and retry");
            return this.shouldRetryOnEndpointFailureAsync();
        }

        if (clientException != null && 
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)){
            return Single.just(this.shouldRetryOnSessionNotAvailable());
        }

        return this.throttlingRetry.shouldRetry(e);
    }

    private ShouldRetryResult shouldRetryOnSessionNotAvailable() {
        this.sessionTokenRetryCount++;
        if (!this.enableEndpointDiscovery || this.useWriteEndpoint || this.sessionTokenRetryCount > 1) {
            return ShouldRetryResult.noRetry();
        }

        logger.warn("Read session not available. Retry using write endpoint.");
        this.useWriteEndpoint = true;
        return ShouldRetryResult.retryAfter(Duration.ZERO);
    }

    private Single<ShouldRetryResult> shouldRetryOnEndpointFailureAsync() {
        if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
            logger.warn("ShouldRetryOnEndpointFailureAsync() Not retrying. Retry count = {}", this.failoverRetryCount);
            return Single.just(ShouldRetryResult.noRetry());
        }

        this.failoverRetryCount++;

        // Mark the current read endpoint as unavailable
        if (isReadRequest) {
            this.globalEndpointManager.markEndpointUnavailable();
        }

        // Some requests may be in progress when the endpoint manager and client are closed.
        // In that case, the request won't succeed since the http client is closed.
        // Therefore just skip the retry here to avoid the delay because retrying won't go through in the end.
        if (this.globalEndpointManager.isClosed()) {
            return Single.just(ShouldRetryResult.noRetry());
        } else {
            this.globalEndpointManager.refreshEndpointList();
        }
        Duration retryDelay = Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS);

        return Single.just(ShouldRetryResult.retryAfter(retryDelay));
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        request.useWriteEndpoint = this.useWriteEndpoint;
        request.clearSessionTokenOnSessionReadFailure = this.sessionTokenRetryCount >= 1;
        this.isReadRequest = request.isReadOnlyRequest();
    }
}
