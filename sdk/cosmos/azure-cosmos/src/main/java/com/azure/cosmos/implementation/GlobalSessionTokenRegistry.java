// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalSessionTokenRegistry {

    private static final Logger logger = LoggerFactory.getLogger(GlobalSessionTokenRegistry.class);
    private static final int SESSION_TOKEN_COUNT_THRESHOLD = 5_000;
    private final AtomicInteger sessionTokenCount;
    private final ConcurrentHashMap<Long, CollectionScopedSessionTokenRegistry> collectionRidToPkScopedSessionTokens;

    public GlobalSessionTokenRegistry() {
        this.sessionTokenCount = new AtomicInteger(0);
        this.collectionRidToPkScopedSessionTokens = new ConcurrentHashMap<>();
    }

    public void setupCollectionRidScopedRegistry(Long collectionRid, String partitionKey, ISessionToken parsedSessionToken) {
        this.tryEvict(null);
        this.collectionRidToPkScopedSessionTokens.compute(collectionRid, (rid, collectionScopedSessionTokenRegistry) -> {

            if (collectionScopedSessionTokenRegistry == null) {
                CollectionScopedSessionTokenRegistry collectionScopedSessionTokenRegistryInner = new CollectionScopedSessionTokenRegistry();
                collectionScopedSessionTokenRegistryInner.tryMergeSessionToken(partitionKey, parsedSessionToken, this.sessionTokenCount);
                return collectionScopedSessionTokenRegistryInner;
            }

            collectionScopedSessionTokenRegistry.tryMergeSessionToken(partitionKey, parsedSessionToken, this.sessionTokenCount);
            return collectionScopedSessionTokenRegistry;
        });
    }

    public CollectionScopedSessionTokenRegistry resolveCollectionScopedSessionTokenRegistry(Long collectionRid) {
        return this.collectionRidToPkScopedSessionTokens.get(collectionRid);
    }

    public void removeCollectionScopedSessionTokenRegistry(Long collectionRid) {
        this.collectionRidToPkScopedSessionTokens.remove(collectionRid);
    }

    public void tryMergeSessionToken(
        CollectionScopedSessionTokenRegistry collectionScopedSessionTokenRegistry,
        String partitionKey,
        ISessionToken parsedSessionToken) {
        this.tryEvict(collectionScopedSessionTokenRegistry);
        collectionScopedSessionTokenRegistry.tryMergeSessionToken(partitionKey, parsedSessionToken, this.sessionTokenCount);
    }

    private void tryEvict(CollectionScopedSessionTokenRegistry collectionScopedSessionTokenRegistry) {
        if (this.sessionTokenCount.get() >= SESSION_TOKEN_COUNT_THRESHOLD) {
            if (collectionScopedSessionTokenRegistry != null) {
                collectionScopedSessionTokenRegistry.evictSessionToken(this.sessionTokenCount);
                logger.info("Evicted session token");
            } else {
                for (Long collectionRid : this.collectionRidToPkScopedSessionTokens.keySet()) {
                    this.collectionRidToPkScopedSessionTokens.get(collectionRid).evictSessionToken(this.sessionTokenCount);
                    logger.info("Evicted session token");
                    break;
                }
            }
        }
    }
}
