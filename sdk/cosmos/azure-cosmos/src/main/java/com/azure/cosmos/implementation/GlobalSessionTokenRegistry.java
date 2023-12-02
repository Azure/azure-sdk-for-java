// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalSessionTokenRegistry {

    private final AtomicInteger sessionTokenCount;
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, PkRangeIdScopedSessionTokenRegistry>> collectionRidToSessionTokenRegistry;

    public GlobalSessionTokenRegistry() {
        this.sessionTokenCount = new AtomicInteger(0);
        this.collectionRidToSessionTokenRegistry = new ConcurrentHashMap<>();
    }

    public void setupCollectionRidScopedRegistry(Long collectionRid, String partitionKey, String pkRangeId, ISessionToken parsedSessionToken) {
        this.collectionRidToSessionTokenRegistry.compute(collectionRid, (rid, collectionRidScopedSessionTokenRegistry) -> {

            if (collectionRidScopedSessionTokenRegistry == null) {
                ConcurrentHashMap<String, PkRangeIdScopedSessionTokenRegistry> collectionRidScopedSessionTokenRegistryInner = new ConcurrentHashMap<>();
                GlobalSessionTokenRegistry.this.mergePkScopedSessionToken(collectionRidScopedSessionTokenRegistryInner, partitionKey, pkRangeId, parsedSessionToken);
                return collectionRidScopedSessionTokenRegistryInner;
            }

            GlobalSessionTokenRegistry.this.mergePkScopedSessionToken(collectionRidScopedSessionTokenRegistry, partitionKey, pkRangeId, parsedSessionToken);
            return collectionRidScopedSessionTokenRegistry;
        });
    }

    public void removeCollectionRidFromRegistry(Long collectionRid) {
        this.collectionRidToSessionTokenRegistry.remove(collectionRid);
    }

    public ConcurrentHashMap<String, PkRangeIdScopedSessionTokenRegistry> resolveCollectionRidScopedSessionTokenRegistry(Long collectionRid) {
        if (this.collectionRidToSessionTokenRegistry.containsKey(collectionRid)) {
            return this.collectionRidToSessionTokenRegistry.get(collectionRid);
        }

        return null;
    }

    public void mergePkScopedSessionToken(ConcurrentHashMap<String, PkRangeIdScopedSessionTokenRegistry> collectionRidScopedSessionTokenRegistry,
                                          String partitionKey,
                                          String pkRangeId,
                                          ISessionToken parsedSessionToken) {

        PkRangeIdScopedSessionTokenRegistry pkRangeIdScopedSessionTokenRegistry = collectionRidScopedSessionTokenRegistry.get(pkRangeId);

        if (pkRangeIdScopedSessionTokenRegistry != null) {
            pkRangeIdScopedSessionTokenRegistry.tryMergeSessionToken(partitionKey, parsedSessionToken);
        }
    }
}
