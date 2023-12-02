// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PkRangeIdScopedSessionTokenRegistry {

    private final ConcurrentHashMap<String, ISessionToken> sessionTokens;
    private final ReentrantReadWriteLock reentrantReadWriteLock;

    public PkRangeIdScopedSessionTokenRegistry() {
        this.sessionTokens = new ConcurrentHashMap<>();
        this.reentrantReadWriteLock = new ReentrantReadWriteLock();
    }

    public void tryMergeSessionToken(String partitionKey, ISessionToken parsedSessionToken) {
        this.sessionTokens.merge(partitionKey, parsedSessionToken, (oldSessionToken, newSessionToken) -> {
            if (oldSessionToken == null) {
                return newSessionToken;
            }

            return oldSessionToken.merge(newSessionToken);
        });
    }

    public ISessionToken resolvePkScopedSessionToken(String partitionKey) {
        if (this.sessionTokens.containsKey(partitionKey)) {
            return this.sessionTokens.get(partitionKey);
        }
        return null;
    }
}
