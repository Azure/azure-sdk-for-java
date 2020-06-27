// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.SocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);
    private final IAddressResolver addressResolver;
    private final ConcurrentHashMap<SocketAddress, Set<RntbdAddressCacheToken>> partitionAddressCache;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(IAddressResolver addressResolver) {
        this.partitionAddressCache = new ConcurrentHashMap<>();
        this.addressResolver = addressResolver;
    }

    // endregion

    // region Methods

    public void onConnectionEvent(RntbdConnectionEvent event, Instant instant, RntbdEndpoint endpoint) {

        logger.debug("onConnectionEvent fired, connectionEvent: {}, eventTime: {}, serverKey: {}",
            event,
            instant,
            endpoint);

        if (event == RntbdConnectionEvent.READ_EOF || event == RntbdConnectionEvent.READ_FAILURE) {
            this.updateAddressCacheAsync(endpoint).publishOn(Schedulers.parallel())
                .doOnError(error -> logger.warn("Address cache update failed due to ", error))
                .subscribe();
        }
    }

    public void updateConnectionState(RntbdEndpoint endpoint, RntbdAddressCacheToken addressCacheToken) {
        if (addressCacheToken != null) {
            this.updatePartitionAddressCache(endpoint, addressCacheToken);
        }
    }

    public void updateConnectionState(
        final List<RntbdEndpoint> endpoints,
        final RntbdAddressCacheToken addressCacheToken) {

        checkNotNull(endpoints, "expected non-null endpoints");

        if (addressCacheToken != null) {
            for (RntbdEndpoint endpoint : endpoints) {
                this.updatePartitionAddressCache(endpoint, addressCacheToken);
            }
        }
    }

    // endregion

    // region Privates

    private Mono<Void> updateAddressCacheAsync(final RntbdEndpoint endpoint) {

        checkNotNull(endpoint, "expected non-null serverKey");

        final Set<RntbdAddressCacheToken> tokens = this.partitionAddressCache.get(endpoint.remoteAddress());
        final Mono<Void> update;

        return tokens == null
            ? Mono.empty()
            : this.addressResolver.updateAsync(new UnmodifiableList<>(new ArrayList<>(tokens)));
    }

    private void updatePartitionAddressCache(RntbdEndpoint endpoint, RntbdAddressCacheToken addressCacheToken) {

        logger.debug("Adding addressCacheToken {} for endpoint at {} to partitionAddressCache",
            endpoint,
            addressCacheToken);

        this.partitionAddressCache.compute(endpoint.remoteAddress(), (address, tokens) -> {
            if (tokens == null) {
                tokens = ConcurrentHashMap.newKeySet();
            }
            tokens.add(addressCacheToken);
            return tokens;
        });
    }

    // endregion
}
