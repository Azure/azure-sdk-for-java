// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.AddressResolverExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public class RntbdConnectionStateListener {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);

    private final AddressResolverExtension addressResolver;
    private final ConcurrentHashMap<SocketAddress, Set<RntbdAddressCacheToken>> partitionAddressCache;
    private final BiFunction<RntbdEndpoint, RxDocumentServiceRequest, Mono<Void>> update;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(
        final AddressResolverExtension addressResolver,
        final UpdateStrategy updateStrategy) {

        checkNotNull(addressResolver, "expected non-null addressResolver");
        checkNotNull(updateStrategy, "expected non-null updateStrategy");

        this.addressResolver = addressResolver;
        this.partitionAddressCache = new ConcurrentHashMap<>();

        switch (updateStrategy) {
            case DEFERRED:
                this.update = this::removeAddressCache;
                break;
            case IMMEDIATE:
                this.update = this::updateAddressCacheAsync;
                break;
            default:
                throw new IllegalArgumentException(lenientFormat("illegal updateStrategy: %s", updateStrategy));
        }
    }

    // endregion

    // region Methods

    public void onConnectionEvent(
        final RntbdConnectionEvent event,
        final Instant time,
        final RntbdEndpoint endpoint,
        final RxDocumentServiceRequest request) {

        checkNotNull(event, "expected non-null event");
        checkNotNull(time,"expected non-null time" );
        checkNotNull(endpoint, "expected non-null endpoint");
        checkNotNull(request, "expected non-null request");

        if (logger.isDebugEnabled()) {
            logger.debug("onConnectionEvent({\"event\":{},\"time\":{},\"endpoint\":{},\"request\":{})",
                RntbdObjectMapper.toJson(event),
                RntbdObjectMapper.toJson(time),
                RntbdObjectMapper.toJson(endpoint),
                RntbdObjectMapper.toJson(request));
        }

        if (event == RntbdConnectionEvent.READ_EOF || event == RntbdConnectionEvent.READ_FAILURE) {

            this.updateAddressCacheAsync(endpoint, request).publishOn(Schedulers.parallel())
                .doOnError(error -> {
                    logger.warn("Address cache update failed due to ", error);
                    endpoint.close();
                })
                .doOnNext(result -> endpoint.close())
                .subscribe();
        }
    }

    public void updateConnectionState(final RntbdEndpoint endpoint, final RxDocumentServiceRequest request) {
        this.updatePartitionAddressCache(new RntbdAddressCacheToken(this.addressResolver, endpoint, request));
    }

    // endregion

    // region Privates

    private Mono<Void> removeAddressCache(final RntbdEndpoint endpoint, final RxDocumentServiceRequest request) {
        this.partitionAddressCache.computeIfPresent(endpoint.remoteAddress(), (address, tokens) -> {
            this.addressResolver.removeAddressResolverURI(request);
            return null;
        });
        return Mono.empty();
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> updateAddressCacheAsync(final RntbdEndpoint endpoint, final RxDocumentServiceRequest request) {

        final Mono<?>[] result = new Mono<?>[] { null };

        this.partitionAddressCache.computeIfPresent(endpoint.remoteAddress(), (address, tokens) -> {
            result[0] = this.addressResolver.updateAsync(request, new UnmodifiableList<>(new ArrayList<>(tokens)));
            return null;
        });

        return result[0] == null ? Mono.empty() : (Mono<Void>) result[0];
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

    // region Types

    public enum UpdateStrategy{
        DEFERRED,
        IMMEDIATE
    }
}
