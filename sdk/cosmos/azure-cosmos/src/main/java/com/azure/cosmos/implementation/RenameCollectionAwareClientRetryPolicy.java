// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class RenameCollectionAwareClientRetryPolicy extends DocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(RenameCollectionAwareClientRetryPolicy.class);

    private final DocumentClientRetryPolicy retryPolicy;
    private final ISessionContainer sessionContainer;
    private final RxClientCollectionCache collectionCache;
    private RxDocumentServiceRequest request;
    private boolean hasTriggered = false;

    public RenameCollectionAwareClientRetryPolicy(ISessionContainer sessionContainer, RxClientCollectionCache collectionCache, DocumentClientRetryPolicy retryPolicy) {
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
    public RetryContext getRetryContext() {
        if (this.retryPolicy != null) {
            return this.retryPolicy.getRetryContext();
        } else {
            return null;
        }
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        return this.retryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {
            if (!shouldRetryResult.shouldRetry && !this.hasTriggered) {
                CosmosException clientException = Utils.as(e, CosmosException.class);

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

                    Mono<Utils.ValueHolder<DocumentCollection>> collectionObs = this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request);

                    return collectionObs.flatMap(collectionValueHolder -> {
                        if (collectionValueHolder.v == null) {
                            logger.warn("Can't recover from session unavailable exception because resolving collection name {} returned null", request.getResourceAddress());
                        } else if (!StringUtils.isEmpty(oldCollectionRid) && !StringUtils.isEmpty(collectionValueHolder.v.getResourceId())) {
                            return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                        }
                        return Mono.just(shouldRetryResult);
                    }).onErrorResume(throwable -> {
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
