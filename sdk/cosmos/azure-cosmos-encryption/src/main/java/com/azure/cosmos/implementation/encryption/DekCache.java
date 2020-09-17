// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.caches.AsyncCache;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

class DekCache {
    private final AsyncCache<String, CachedDekProperties> DekPropertiesCache = new AsyncCache<>();
    private final AsyncCache<String, InMemoryRawDek> RawDekCache = new AsyncCache<>();
    private final Duration dekPropertiesTimeToLive;

    public DekCache() {
        this(null);
    }

    public DekCache(Duration dekPropertiesTimeToLive) {
        if (dekPropertiesTimeToLive != null) {
            this.dekPropertiesTimeToLive = dekPropertiesTimeToLive;
        } else {
            this.dekPropertiesTimeToLive = Duration.ofMinutes(30);
        }
    }

    public Mono<DataEncryptionKeyProperties> getOrAddDekProperties(
        String dekId,
        Function<String, Mono<DataEncryptionKeyProperties>> fetcher) {
        Mono<CachedDekProperties> cachedDekPropertiesMono = this.DekPropertiesCache.getAsync(
            dekId,
            null,
            () -> this.fetch(dekId, fetcher));

        return cachedDekPropertiesMono.flatMap(cachedDekProperties -> {
                if (cachedDekProperties.getServerPropertiesExpiryUtc().isBefore(Instant.now())) {
                    return this.DekPropertiesCache.getAsync(
                        dekId,
                        null,
                        () -> this.fetch(dekId, fetcher));
                } else {
                    return Mono.just(cachedDekProperties);
                }

            }
        ).map(CachedDekProperties::getServerProperties);
    }

    public Mono<InMemoryRawDek> getOrAddRawDek(
        DataEncryptionKeyProperties dekProperties,
        Function<DataEncryptionKeyProperties, Mono<InMemoryRawDek>> unwrapper) {
        Mono<InMemoryRawDek> inMemoryRawDekMono = this.RawDekCache.getAsync(
            dekProperties.selfLink,
            null,
            () -> unwrapper.apply(dekProperties));

        return inMemoryRawDekMono.flatMap(
            inMemoryRawDek -> {
                if (inMemoryRawDek.getRawDekExpiry().isBefore(Instant.now())) {

                    return this.RawDekCache.getAsync(
                        dekProperties.selfLink,
                        null,
                        () -> unwrapper.apply(dekProperties)
                        /* forceRefresh: true */);
                } else {
                    return Mono.just(inMemoryRawDek);
                }
            }
        );
    }

    public void setDekProperties(String dekId, DataEncryptionKeyProperties dekProperties) {
        CachedDekProperties cachedDekProperties = new CachedDekProperties(dekProperties, Instant.now().plus(this.dekPropertiesTimeToLive));
        this.DekPropertiesCache.set(dekId, cachedDekProperties);
    }

    public void setRawDek(String dekId, InMemoryRawDek inMemoryRawDek) {
        this.RawDekCache.set(dekId, inMemoryRawDek);
    }

    public Mono<Void> remove(String dekId) {
        Mono<CachedDekProperties> cachedDekPropertiesMono = this.DekPropertiesCache.removeAsync(dekId);

        return cachedDekPropertiesMono.flatMap(cachedDekProperties -> this.RawDekCache.removeAsync(dekId)).then();
    }

    private Mono<CachedDekProperties> fetch(
        String dekId,
        Function<String, Mono<DataEncryptionKeyProperties>> fetcher) {
        Mono<DataEncryptionKeyProperties> serverPropertiesMono = fetcher.apply(dekId);
        return serverPropertiesMono.map(serverProperties -> new CachedDekProperties(serverProperties, Instant.now().plus(this.dekPropertiesTimeToLive)));
    }
}
