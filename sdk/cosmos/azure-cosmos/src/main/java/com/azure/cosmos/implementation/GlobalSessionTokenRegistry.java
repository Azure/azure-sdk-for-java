// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalSessionTokenRegistry {

    private final AtomicInteger sessionTokenCount;
    private final ConcurrentHashMap<Long, CollectionScopedSessionTokenRegistry> collectionRidToPkScopedSessionTokens;

    public GlobalSessionTokenRegistry() {
        this.sessionTokenCount = new AtomicInteger(0);
        this.collectionRidToPkScopedSessionTokens = new ConcurrentHashMap<>();
    }

    public void setupCollectionRidScopedRegistry(Long collectionRid, String partitionKey, ISessionToken parsedSessionToken) {
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
        collectionScopedSessionTokenRegistry.tryMergeSessionToken(partitionKey, parsedSessionToken, this.sessionTokenCount);
    }
}
