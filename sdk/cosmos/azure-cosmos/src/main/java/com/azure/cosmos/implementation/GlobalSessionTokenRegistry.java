// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalSessionTokenRegistry {

    private final AtomicInteger sessionTokenCount;
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionRidToPkScopedSessionTokens;
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<String>> collectionRidToPksAdded;

    public GlobalSessionTokenRegistry() {
        this.sessionTokenCount = new AtomicInteger(0);
        this.collectionRidToPkScopedSessionTokens = new ConcurrentHashMap<>();
        this.collectionRidToPksAdded = new ConcurrentHashMap<>();
    }

    public void setupCollectionRidScopedRegistry(Long collectionRid, String partitionKey, String pkRangeId, ISessionToken parsedSessionToken) {
        this.collectionRidToPkScopedSessionTokens.compute(collectionRid, (rid, pkScopedSessionTokens) -> {

            if (pkScopedSessionTokens == null) {
                ConcurrentHashMap<String, ISessionToken> pkScopedSessionTokensInner = new ConcurrentHashMap<>();
                pkScopedSessionTokensInner.put(partitionKey, parsedSessionToken);
                this.sessionTokenCount.incrementAndGet();
                return pkScopedSessionTokensInner;
            }

            pkScopedSessionTokens.merge(partitionKey, parsedSessionToken, ISessionToken::merge);

            this.collectionRidToPksAdded.compute(collectionRid, (ridInner, collectionRidScopedPksAdded) -> {

                if (collectionRidScopedPksAdded == null) {
                    collectionRidScopedPksAdded = new ConcurrentLinkedQueue<>();
                }

                collectionRidScopedPksAdded.offer(partitionKey);
                return collectionRidScopedPksAdded;
            });

            return pkScopedSessionTokens;
        });
    }

    public void removeCollectionRidFromRegistry(Long collectionRid) {
        this.collectionRidToPkScopedSessionTokens.remove(collectionRid);
    }

    public ConcurrentHashMap<String, ISessionToken> resolveCollectionRidScopedRegistry(Long collectionRid) {
        if (this.collectionRidToPkScopedSessionTokens.containsKey(collectionRid)) {
            return this.collectionRidToPkScopedSessionTokens.get(collectionRid);
        }

        return null;
    }

    public void updatePkScopedSessionTokens(
        ConcurrentHashMap<String, ISessionToken> pkScopedSessionsForSomeCollectionRid,
        String partitionKey,
        ISessionToken parsedSessionToken,
        Long collectionRid) {

        pkScopedSessionsForSomeCollectionRid.merge(partitionKey, parsedSessionToken, (existingSessionToken, newSessionToken) -> {
            ConcurrentLinkedQueue<String> pkQueueForCollectionRid = this.collectionRidToPksAdded.get(collectionRid);
            pkQueueForCollectionRid.add(partitionKey);
            return existingSessionToken.merge(newSessionToken);
        });
    }

    public ISessionToken resolveSessionToken(
        ConcurrentHashMap<String, ISessionToken> pkScopedSessionsForSomeCollectionRid,
        String partitionKey,
        Long collectionRid) {

        this.collectionRidToPksAdded.get(collectionRid).add(partitionKey);
        return pkScopedSessionsForSomeCollectionRid.get(partitionKey);
    }
}
