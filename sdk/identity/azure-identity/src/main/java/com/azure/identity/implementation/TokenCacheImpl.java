package com.azure.identity.implementation;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.aad.msal4j.ITokenCache;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TokenCacheImpl implements ITokenCacheAccessAspect {
    private static final Logger LOG = LoggerFactory.getLogger(com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect.class);
    private WeakHashMap<Object, OffsetDateTime> cacheAccess;
    private byte[] data;
    private OffsetDateTime lastUpdated;
    private final AtomicBoolean wip;

    TokenCacheImpl(byte[] data) {
        this.data = data;
        lastUpdated = OffsetDateTime.now();
        cacheAccess = new WeakHashMap<>();
        wip = new AtomicBoolean(false);
    }

    public TokenCacheImpl() {
        this.data = new byte[0];
        lastUpdated = OffsetDateTime.now();
        cacheAccess = new WeakHashMap<>();
        wip = new AtomicBoolean(false);
    }


    Mono<Boolean> registerCache(com.microsoft.aad.msal4j.TokenCache tokenCache) {
        return Mono.defer(() -> {
                if (wip.compareAndSet(false, true)) {
                    if (!cacheAccess.containsKey(tokenCache)) {
                        cacheAccess.put(tokenCache, OffsetDateTime.now());
                    }
                    return Mono.just(true);
                }
                return Mono.empty();
            }).doFinally(ignored -> wip.set(false))
            .repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        Mono.defer(() -> {
            if (wip.compareAndSet(false, true)) {
                iTokenCacheAccessContext.tokenCache().deserialize(new String(data, StandardCharsets.UTF_8));
                cacheAccess.put(iTokenCacheAccessContext.tokenCache(), OffsetDateTime.now());
                return Mono.just(true);
            }
            return Mono.empty();
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)))
            .doFinally(ignored -> wip.set(false)).block();
    }

    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        if (iTokenCacheAccessContext.hasCacheChanged()) {
            updateCacheData(iTokenCacheAccessContext.tokenCache()).block();
        }
    }


    private Mono<Boolean> updateCacheData(ITokenCache tokenCache) {
        return Mono.defer(() -> {
            if (wip.compareAndSet(false, true)) {
                if (!cacheAccess.containsKey(tokenCache) || cacheAccess.get(tokenCache).compareTo(lastUpdated) < 0) {
                    com.microsoft.aad.msal4j.TokenCache deserializedCache =
                        JsonHelper.convertJsonToObject(new String(data, StandardCharsets.UTF_8),
                            com.microsoft.aad.msal4j.TokenCache.class);
                    try {
                        JsonNode cache = JsonHelper.mapper.readTree(new String(data, StandardCharsets.UTF_8));
                        JsonNode cacheB = JsonHelper.mapper.readTree(tokenCache.serialize());
                        mergeJsonObjects(cache, cacheB);
                        data = cache.toString().getBytes(StandardCharsets.UTF_8);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }

                } else {
                    data = tokenCache.serialize().getBytes(StandardCharsets.UTF_8);
                }
                lastUpdated = cacheAccess.put(tokenCache, OffsetDateTime.now());
                return Mono.just(true);
            }
            return Mono.empty();
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)))
            .doFinally(ignored -> wip.set(false));
    }

    private static void mergeJsonObjects(JsonNode old, JsonNode update) {
        mergeRemovals(old, update);
        mergeUpdates(old, update);
    }

    private static void mergeUpdates(JsonNode old, JsonNode update) {
        Iterator<String> fieldNames = update.fieldNames();
        while (fieldNames.hasNext()) {
            String uKey = fieldNames.next();
            JsonNode uValue = update.get(uKey);

            // add new property
            if (!old.has(uKey)) {
                if (!uValue.isNull() &&
                    !(uValue.isObject() && uValue.size() == 0)) {
                    ((ObjectNode)old).set(uKey, uValue);
                }
            }
            // merge old and new property
            else {
                JsonNode oValue = old.get(uKey);
                if (uValue.isObject()) {
                    mergeUpdates(oValue, uValue);
                } else {
                    ((ObjectNode)old).set(uKey, uValue);
                }
            }
        }
    }

    private static void mergeRemovals(JsonNode old, JsonNode update) {
        Set<String> msalEntities =
            new HashSet<>(Arrays.asList("Account", "AccessToken", "RefreshToken", "IdToken", "AppMetadata"));

        for (String msalEntity : msalEntities) {
            JsonNode oldEntries = old.get(msalEntity);
            JsonNode newEntries = update.get(msalEntity);
            if (oldEntries != null) {
                Iterator<Map.Entry<String, JsonNode>> iterator = oldEntries.fields();

                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> oEntry = iterator.next();

                    String key = oEntry.getKey();
                    if (newEntries == null || !newEntries.has(key)) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}
