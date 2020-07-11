// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.AddressResolverExtension;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);
    private final AddressResolverExtension addressResolver;
    private final ConcurrentHashMap<SocketAddress, Set<RntbdAddressCacheToken>> partitionAddressCache;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final AddressResolverExtension addressResolver) {
        checkNotNull(addressResolver, "expected non-null addressResolver");
        this.partitionAddressCache = new ConcurrentHashMap<>();
        this.addressResolver = addressResolver;
    }

    // endregion

    // region Methods

    public void onConnectionEvent(
        final RntbdConnectionEvent event,
        final Instant instant,
        final RntbdEndpoint endpoint) {

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

    public void updateConnectionState(final RntbdAddressCacheToken addressCacheToken) {
        if (addressCacheToken != null) {
            this.updatePartitionAddressCache(addressCacheToken);
        }
    }

    // endregion

    // region Privates

    private Mono<Void> updateAddressCacheAsync(final RntbdEndpoint endpoint) {

        checkNotNull(endpoint, "expected non-null endpoint");

        final Set<RntbdAddressCacheToken> tokens = this.partitionAddressCache.get(endpoint.remoteAddress());
        final Mono<Void> update;

        update = tokens == null
            ? Mono.empty()
            : this.addressResolver.updateAsync(new UnmodifiableList<>(new ArrayList<>(tokens)));

        return update;
    }

    private void updatePartitionAddressCache(final RntbdAddressCacheToken addressCacheToken) {

        logger.debug("Adding addressCacheToken {} to partitionAddressCache", addressCacheToken);

        this.partitionAddressCache.compute(addressCacheToken.getRemoteAddress(), (address, tokens) -> {
            if (tokens == null) {
                tokens = ConcurrentHashMap.newKeySet();
            }
            tokens.add(addressCacheToken);
            return tokens;
        });
    }

    // endregion
}
