// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * This retry policy is designed to work with in a pair with ClientRetryPolicy.
 * The inner retryPolicy must be a ClientRetryPolicy or a retry policy delegating to it.
 *
 * The expectation that is the outer retry policy in the retry policy chain and nobody can overwrite ShouldRetryResult.
 * Once we clear the session we expect call to fail and throw exception to the client. Otherwise we may violate session consistency.
 */
public class ClearingSessionContainerClientRetryPolicy implements IDocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(ClearingSessionContainerClientRetryPolicy.class);

    private final IDocumentClientRetryPolicy retryPolicy;
    private final ISessionContainer sessionContainer;
    private RxDocumentServiceRequest request;
    private boolean hasTriggered = false;

    public ClearingSessionContainerClientRetryPolicy(ISessionContainer sessionContainer, IDocumentClientRetryPolicy retryPolicy) {
        this.sessionContainer = sessionContainer;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.retryPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {

        return this.retryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {

            if (!shouldRetryResult.shouldRetry && !this.hasTriggered)
            {
                CosmosClientException clientException = Utils.as(e, CosmosClientException.class);

                if (this.request == null) {
                    // someone didn't call OnBeforeSendRequest - nothing we can do
                    logger.error("onBeforeSendRequest is not invoked, encountered failure due to request being null", e);
                    return Mono.just(ShouldRetryResult.error(e));
                }

                if (clientException != null && this.request.getIsNameBased() &&
                        Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                        Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE))
                {
                    // Clear the session token, because the collection name might be reused.
                    logger.warn("Clear the token for named base request {}", request.getResourceAddress());

                    this.sessionContainer.clearTokenByCollectionFullName(request.getResourceAddress());

                    this.hasTriggered = true;
                }
            }

            return Mono.just(shouldRetryResult);
        });
    }
}
