// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PkRangeIdScopedSessionTokenRegistry {

    private static final int GLOBAL_SESSION_TOKEN_COUNT_LIMIT = 1000;
    private static final Logger logger = LoggerFactory.getLogger(PkRangeIdScopedSessionTokenRegistry.class);
    private final LinkedHashMap<String, ISessionToken> sessionTokens;
    private final ReentrantReadWriteLock reentrantReadWriteLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;


    public PkRangeIdScopedSessionTokenRegistry() {
        this.sessionTokens = new LinkedHashMap<>();
        this.reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.readLock = this.reentrantReadWriteLock.readLock();
        this.writeLock = this.reentrantReadWriteLock.writeLock();
    }

    public void tryMergeSessionToken(String partitionKey, ISessionToken parsedSessionToken, AtomicInteger globalSessionTokenCount) {
        this.writeLock.lock();
        try {
            this.sessionTokens.compute(partitionKey, (existingPk, existingSessionToken) -> {
                if (existingSessionToken == null) {
                    globalSessionTokenCount.incrementAndGet();
                    logger.info("Global session token count : {}", globalSessionTokenCount.get());
                    return parsedSessionToken;
                }

                return existingSessionToken.merge(parsedSessionToken);
            });
        } finally {
            this.writeLock.unlock();
        }
    }

    public ISessionToken resolvePkScopedSessionToken(String partitionKey) {
        if (this.sessionTokens.containsKey(partitionKey)) {
            return this.sessionTokens.get(partitionKey);
        }
        return null;
    }
}
