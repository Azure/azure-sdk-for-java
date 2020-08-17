// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.AddressResolverExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);
    private final AddressResolverExtension addressResolver;
    private final ConcurrentHashMap<SocketAddress, Set<RntbdAddressCacheToken>> partitionAddressCache;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final AddressResolverExtension addressResolver) {
        this.addressResolver = checkNotNull(addressResolver, "expected non-null addressResolver");
        this.partitionAddressCache = new ConcurrentHashMap<>();
    }

    // endregion

    // region Methods

    public void onConnectionEvent(
        final RntbdConnectionEvent event,
        final Instant time,
        final RntbdEndpoint endpoint) {

        checkNotNull(event, "expected non-null event");
        checkNotNull(time,"expected non-null time" );
        checkNotNull(endpoint, "expected non-null endpoint");

        if (logger.isDebugEnabled()) {
            logger.debug("onConnectionEvent({\"event\":{},\"time\":{},\"endpoint\":{})",
                RntbdObjectMapper.toJson(event),
                RntbdObjectMapper.toJson(time),
                RntbdObjectMapper.toJson(endpoint));
        }

        if (event == RntbdConnectionEvent.READ_EOF || event == RntbdConnectionEvent.READ_FAILURE) {
            this.updateAddressCacheAsync(endpoint).publishOn(Schedulers.parallel())
                .doOnError(error -> logger.warn("Address cache update failed due to ", error))
                .subscribe();
        }
    }

    public void updateConnectionState(final RntbdEndpoint endpoint, final RxDocumentServiceRequest request) {
        this.updatePartitionAddressCache(new RntbdAddressCacheToken(this.addressResolver, endpoint, request));
    }

    // endregion

    // region Privates

    private Mono<Void> updateAddressCacheAsync(final RntbdEndpoint endpoint) {

        final AtomicReference<Mono<Void>> result = new AtomicReference<>(Mono.empty());

        this.partitionAddressCache.computeIfPresent(endpoint.remoteAddress(), (address, tokens) -> {
            result.set(this.addressResolver.updateAsync(new UnmodifiableList<>(new ArrayList<>(tokens))));
            return null;
        });

        return result.get();
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
