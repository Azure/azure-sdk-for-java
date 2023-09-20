// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {
    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);
    private final RntbdEndpoint endpoint;
    private final RntbdConnectionStateListenerMetrics metrics;
    private final ConcurrentHashMap<String, Uri> addressUriMap;
    private final ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor;
    private final AddressSelector addressSelector;
    private final AtomicBoolean endpointValidationInProgress = new AtomicBoolean(false);

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(
        final RntbdEndpoint endpoint,
        final ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
        final AddressSelector addressSelector) {
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
        this.metrics = new RntbdConnectionStateListenerMetrics();
        this.addressUriMap = new ConcurrentHashMap<>();
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;
        this.addressSelector = addressSelector;
    }

    // endregion

    // region Methods

    public void onBeforeSendRequest(Uri addressUri) {
        checkNotNull(addressUri, "Argument 'addressUri' should not be null");
        //Important: always track the latest value
        this.addressUriMap.compute(addressUri.getURIAsString(), (key, existingValue) -> addressUri);
    }

    public void onException(Throwable exception) {
        checkNotNull(exception, "expect non-null exception");

        this.metrics.record();

        // * An operation could fail due to an IOException which indicates a connection reset by the server,
        // * or a channel closes unexpectedly because the server stopped taking requests
        // * or the channel has been shutdown gracefully
        if (exception instanceof IOException) {
            if (exception instanceof ClosedChannelException) {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, exception));
            } else {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_FAILURE, exception));
            }
        } else if (exception instanceof RntbdRequestManager.UnhealthyChannelException) {
            // A channel is closed due to Rntbd health check
            this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_FAILURE, exception));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Will not raise the connection state change event for error", exception);
            }
        }
    }

    public RntbdConnectionStateListenerMetrics getMetrics() {
        return this.metrics;
    }

    public void openConnectionIfNeeded() {

        // do not fail here, just log
        // this attempts to make the open connections flow
        // best effort
        if (this.proactiveOpenConnectionsProcessor == null) {
            logger.warn("proactiveOpenConnectionsProcessor is null");
            return;
        }

        Optional<Uri> addressUriOptional = this.addressUriMap.values().stream().findFirst();
        Uri addressUri;

        if (addressUriOptional.isPresent()) {
            addressUri = addressUriOptional.get();
        } else {
            logger.debug("addressUri cannot be null...");
            return;
        }

        // connection state listener will attempt to open a closed connection only
        // when the endpoint / address uri is used by a container which was part of
        // the connection warmup flow
        if (!this.proactiveOpenConnectionsProcessor.isAddressUriUnderOpenConnectionsFlow(addressUri.getURIAsString())) {
            return;
        }

        // connection state listener submits an open connection task for an endpoint
        // and only submits the task for that endpoint if the previous task has been
        // completed. It is okay to lose a task, since each task will attempt to attain a certain no. of
        // connection as denoted by the min required no. of connections for that endpoint
        if (this.endpointValidationInProgress.compareAndSet(false, true)) {
            Mono.fromFuture(this.proactiveOpenConnectionsProcessor.submitOpenConnectionTaskOutsideLoop(
                "",
                this.endpoint.serviceEndpoint(),
                addressUri,
                this.endpoint.getMinChannelsRequired()
            ))
            .doFinally(signalType -> this.endpointValidationInProgress.compareAndSet(true, false))
            .subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
            .subscribe();
        }
    }
    // endregion

    // region Privates

    private int onConnectionEvent(final RntbdConnectionEvent event, final Throwable exception) {

        checkNotNull(exception, "expected non-null exception");

        if (event == RntbdConnectionEvent.READ_EOF || event == RntbdConnectionEvent.READ_FAILURE) {
            if (logger.isDebugEnabled()) {
                logger.debug("onConnectionEvent({\"event\":{},\"time\":{},\"endpoint\":{},\"cause\":{})",
                    event,
                    RntbdObjectMapper.toJson(Instant.now()),
                    RntbdObjectMapper.toJson(this.endpoint),
                    RntbdObjectMapper.toJson(exception));
            }

            // When idleEndpointTimeout reached, SDK will close all existing channels,
            // which will translate into ClosedChannelException which does not mean server is in unhealthy status.
            // But it makes sense to make the server as unhealthy as it is safer to validate the server health again for future requests
            for (Uri addressUri : this.addressUriMap.values()) {
                addressUri.setUnhealthy();
            }

            return addressUriMap.size();
        }

        return 0;
    }

    public void attemptBackgroundAddressRefresh(RxDocumentServiceRequest request, Exception exception) {

        if (request.requestContext == null) {
            return;
        }

        AtomicBoolean isRequestCancelledOnTimeout = request.requestContext.isRequestCancelledOnTimeout();

        if (isRequestCancelledOnTimeout == null
            || !isRequestCancelledOnTimeout.get()
            || !shouldRefreshForException(exception)) {
            return;
        }

        final boolean forceAddressRefresh = request.requestContext.forceRefreshAddressCache;

        this.addressSelector
            .resolveAddressesAsync(request, forceAddressRefresh)
            .publishOn(Schedulers.boundedElastic())
            .doOnSubscribe(ignore -> {
                logger.debug("Background refresh of addresses started!");
            })
            .doFinally(signalType -> {
                logger.debug("Background refresh of addresses finished!");
            })
            .subscribe(
                ignoreResult -> {
                },
                throwable -> logger.warn("Background address refresh failed with {}", throwable.getMessage(), throwable)
            );
    }
    // endregion

    private boolean shouldRefreshForException(Exception exception) {
        return exception instanceof ConnectTimeoutException
            || exception instanceof InvalidPartitionException
            || exception instanceof PartitionIsMigratingException
            || exception instanceof PartitionKeyRangeIsSplittingException
            || exception instanceof GoneException;
    }
}
