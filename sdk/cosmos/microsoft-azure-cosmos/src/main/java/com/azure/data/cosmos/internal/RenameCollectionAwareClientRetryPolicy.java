// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.caches.RxClientCollectionCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class RenameCollectionAwareClientRetryPolicy implements IDocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(RenameCollectionAwareClientRetryPolicy.class);

    private final IDocumentClientRetryPolicy retryPolicy;
    private final ISessionContainer sessionContainer;
    private final RxClientCollectionCache collectionCache;
    private RxDocumentServiceRequest request;
    private boolean hasTriggered = false;

    public RenameCollectionAwareClientRetryPolicy(ISessionContainer sessionContainer, RxClientCollectionCache collectionCache, IDocumentClientRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        this.sessionContainer = sessionContainer;
        this.collectionCache = collectionCache;
        this.request = null;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.retryPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        return this.retryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {
            if (!shouldRetryResult.shouldRetry && !this.hasTriggered) {
                CosmosClientException clientException = Utils.as(e, CosmosClientException.class);

                if (this.request == null) {
                    // someone didn't call OnBeforeSendRequest - nothing we can do
                    logger.error("onBeforeSendRequest is not invoked, encountered failure due to request being null", e);
                    return Mono.just(ShouldRetryResult.error(e));
                }

                if (clientException != null && this.request.getIsNameBased() &&
                        Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                        Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
                    // Clear the session token, because the collection name might be reused.
                    logger.warn("Clear the token for named base request {}", request.getResourceAddress());

                    this.sessionContainer.clearTokenByCollectionFullName(request.getResourceAddress());

                    this.hasTriggered = true;

                    String oldCollectionRid = request.requestContext.resolvedCollectionRid;

                    request.forceNameCacheRefresh = true;
                    request.requestContext.resolvedCollectionRid = null;

                    Mono<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);

                    return collectionObs.flatMap(collectionInfo -> {
                        if (!StringUtils.isEmpty(oldCollectionRid) && !StringUtils.isEmpty(collectionInfo.resourceId())) {
                            return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                        }
                        return Mono.just(shouldRetryResult);
                    }).switchIfEmpty(Mono.defer(() -> {
                        logger.warn("Can't recover from session unavailable exception because resolving collection name {} returned null", request.getResourceAddress());
                        return Mono.just(shouldRetryResult);
                    })).onErrorResume(throwable -> {
                        // When resolveCollectionAsync throws an exception ignore it because it's an attempt to recover an existing
                        // error. When the recovery fails we return ShouldRetryResult.noRetry and propagate the original exception to the client

                        logger.warn("Can't recover from session unavailable exception because resolving collection name {} failed with {}", request.getResourceAddress(), throwable.getMessage());
                        if (throwable instanceof Exception) {
                            return Mono.just(ShouldRetryResult.error((Exception) throwable));
                        }
                        return Mono.error(throwable);
                    });
                }
            }
            return Mono.just(shouldRetryResult);
        });
    }
}
