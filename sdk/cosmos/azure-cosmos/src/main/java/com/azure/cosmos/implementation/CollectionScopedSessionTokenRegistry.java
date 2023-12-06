// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionScopedSessionTokenRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CollectionScopedSessionTokenRegistry.class);
    private final ConcurrentHashMap<String, ISessionToken> sessionTokens;
    private final ConcurrentLinkedQueue<String> pkQueue;
    private final SessionTokenRegistryMetadata sessionTokenRegistryMetadata;

    public CollectionScopedSessionTokenRegistry() {
        this.sessionTokens = new ConcurrentHashMap<>();
        this.pkQueue = new ConcurrentLinkedQueue<>();
        this.sessionTokenRegistryMetadata = new SessionTokenRegistryMetadata();
    }

    public void tryMergeSessionToken(String partitionKey, ISessionToken parsedSessionToken, AtomicInteger globalSessionTokenCount) {
        this.sessionTokens.compute(partitionKey, (existingPk, existingSessionToken) -> {
            if (existingSessionToken == null) {
                globalSessionTokenCount.incrementAndGet();
                this.sessionTokenRegistryMetadata.recordSessionTokenInsertion();
                this.pkQueue.add(existingPk);
                logger.info("Global session token count : {}", globalSessionTokenCount.get());
                return parsedSessionToken;
            }

            return existingSessionToken.merge(parsedSessionToken);
        });
    }

    public ISessionToken resolvePkScopedSessionToken(String partitionKey) {
        if (this.sessionTokens.containsKey(partitionKey)) {
            this.pkQueue.add(partitionKey);
            return this.sessionTokens.get(partitionKey);
        }
        return null;
    }

    public void evictSessionToken(AtomicInteger globalSessionTokenCount) {
        if (!this.pkQueue.isEmpty()) {
            String pkAdded = this.pkQueue.poll();

            if (pkAdded != null && !pkAdded.isEmpty()) {
                this.sessionTokens.remove(pkAdded);
                this.sessionTokenRegistryMetadata.recordSessionTokenEviction();
                globalSessionTokenCount.decrementAndGet();
            }
        }
    }
}
