// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;
import static com.azure.core.amqp.implementation.ClientConstants.CALL_SITE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.INTERVAL_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;

/**
 * A cache that holds a single active RequestResponseChannel at a time. The cache is responsible for creating a new
 * RequestResponseChannel if the cache is empty or the current cached RequestResponseChannel is in closed state.
 * <p>
 * The cache is also responsible for terminating the recovery support (i.e. no longer possible to obtain
 * RequestResponseChannel) once the cache is terminated due to {@link RequestResponseChannelCache#dispose()} call or
 * the parent {@link ReactorConnection} is in terminated state.
 * Since the parent {@link ReactorConnection} hosts any RequestResponseChannel object that RequestResponseChannelCache
 * caches, recovery (scoped to the Connection) is impossible once the Connection is terminated
 * (i.e. connection.isDisposed() == true). Which also means RequestResponseChannelCache cannot outlive the Connection.
 */
public final class RequestResponseChannelCache implements Disposable {
    private static final String IS_CACHE_TERMINATED_KEY = "isCacheTerminated";
    private static final String IS_CONNECTION_TERMINATED_KEY = "isConnectionTerminated";
    private static final String TRY_COUNT_KEY = "tryCount";
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    private final ClientLogger logger;
    private final ReactorConnection connection;
    private final Duration activationTimeout;
    private final Mono<RequestResponseChannel> createOrGetCachedChannel;
    private final Object lock = new Object();
    private volatile boolean terminated;
    // Note: The only reason to have below 'currentChannel' is to close the cached RequestResponseChannel internally
    // upon 'RequestResponseChannelCache' termination (via dispose()). Type must never expose 'currentChannel' variable
    // to any dependent type; instead, the dependent type must acquire RequestResponseChannel through the cache route,
    // i.e., by subscribing to 'createOrGetCachedChannel' via 'get()' getter.
    private volatile RequestResponseChannel currentChannel;

    /**
     * Creates RequestResponseChannelCache to cache RequestResponseChannel.
     *
     * @param connection the connection on which the session of the cached RequestResponseChannel gets hosted.
     * @param entityPath the entity path.
     * @param sessionName the session hosting the cached RequestResponseChannel.
     * @param linksName the link name prefix for the underlying send and receive links backing the RequestResponseChannel.
     * @param retryPolicy the retry policy.
     */
    public RequestResponseChannelCache(ReactorConnection connection, String entityPath, String sessionName,
        String linksName, AmqpRetryPolicy retryPolicy) {
        Objects.requireNonNull(connection, "'connection' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(sessionName, "'sessionName' cannot be null.");
        Objects.requireNonNull(linksName, "'linksName' cannot be null.");
        Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");

        final Map<String, Object> loggingContext = new HashMap<>(2);
        loggingContext.put(CONNECTION_ID_KEY, connection.getId()); // E.g., 'MF_0f4c2e_1680070221023'
        loggingContext.put(LINK_NAME_KEY, linksName);              // E.g., 'cbs', '{entityPath}-mgmt' (E.g., 'q0-mgmt')
        this.logger = new ClientLogger(RequestResponseChannelCache.class, loggingContext);

        this.connection = connection;
        this.activationTimeout = retryPolicy.getRetryOptions().getTryTimeout();

        final Mono<RequestResponseChannel> newChannel = Mono.defer(() -> {
            final RecoveryTerminatedException terminatedError = checkRecoveryTerminated("new-channel");
            if (terminatedError != null) {
                // 'retryWhenSpec' function inspects 'RecoveryTerminatedException' and propagated to downstream as
                // 'RequestResponseChannelClosedException'
                return Mono.error(terminatedError);
            }
            return this.connection.newRequestResponseChannel(sessionName, linksName, entityPath);
        });

        this.createOrGetCachedChannel = newChannel.flatMap(c -> {
            return awaitToActive(c, activationTimeout, logger);
        }).retryWhen(retryWhenSpec(retryPolicy)).<RequestResponseChannel>handle((c, sink) -> {
            final RecoveryTerminatedException terminatedError;
            synchronized (lock) {
                // Here in 'cache-refresh route', the {terminated-read, currentChannel-write} block is synchronized with
                // {terminated-write, currentChannel-read} block in 'close route'. This synchronization ensure channel
                // is not leaked (i.e. missing close) if the 'cache-refresh route' and 'close route' runs concurrently.
                terminatedError = checkRecoveryTerminated("cache-refresh");
                this.currentChannel = c;
            }
            if (terminatedError != null) {
                if (!c.isDisposed()) {
                    c.closeAsync().subscribe();
                }
                sink.error(terminatedError.propagate());
            } else {
                logger.atInfo().log("Emitting the new active channel.");
                sink.next(c);
            }
        }).cacheInvalidateIf(c -> {
            if (c.isDisposedOrDisposalInInProgress()) {
                logger.atInfo().log("The channel is closed, requesting a new channel.");
                return true;
            } else {
                // emit cached channel.
                return false;
            }
        });
    }

    /**
     * Get the Mono that, when subscribed, emits the cached RequestResponseChannel if it is active or creates and
     * emits a new RequestResponseChannel if the cache is empty or the current cached RequestResponseChannel is in
     * closed state.
     *
     * @return a Mono that emits active RequestResponseChannel.
     */
    public Mono<RequestResponseChannel> get() {
        return createOrGetCachedChannel;
    }

    /**
     * Terminate the cache such that it is no longer possible to obtain RequestResponseChannel using {@link #get()}.
     * If there is a current (cached) RequestResponseChannel then it will be closed.
     */
    @Override
    public void dispose() {
        closeAsync().subscribe();
    }

    /**
     * Terminate the cache such that it is no longer possible to obtain RequestResponseChannel using {@link this#get()}.
     * If there is a current (cached) RequestResponseChannel then it will be closed.
     *
     * @return a Mono that completes when the cache is terminated.
     */
    Mono<Void> closeAsync() {
        final RequestResponseChannel cached;
        synchronized (lock) {
            if (terminated) {
                return isClosedMono.asMono();
            }
            terminated = true;
            cached = currentChannel;
        }

        if (cached == null || cached.isDisposed()) {
            logger.atInfo().log("closing the channel-cache.");
            isClosedMono.emitEmpty(FAIL_FAST);
            return isClosedMono.asMono();
        } else {
            return cached.closeAsync().doOnEach(signal -> {
                if (signal.isOnError() || signal.isOnComplete()) {
                    logger.atInfo().log("closing the cached channel and the channel-cache.");
                    isClosedMono.emitEmpty(FAIL_FAST);
                }
            });
        }
    }

    @Override
    public boolean isDisposed() {
        return terminated;
    }

    private Retry retryWhenSpec(AmqpRetryPolicy retryPolicy) {
        return Retry.from(retrySignals -> retrySignals.concatMap(retrySignal -> {
            final Retry.RetrySignal signal = retrySignal.copy();
            final Throwable error = signal.failure();
            final long iteration = signal.totalRetriesInARow();

            if (error == null) {
                return Mono.error(new IllegalStateException("RetrySignal::failure() not expected to be null."));
            }

            // There are exceptions that will not be AmqpExceptions like IllegalStateException (ISE)
            // or RejectedExecutionException when attempting an operation that is closed or if the IO
            // signal is accidentally closed, retrying in these cases as well. This is inherited from
            // v1 AmqpChannelProcessor that v2 RequestResponseChannelCache replaces.
            final boolean shouldRetry = error instanceof TimeoutException
                || (error instanceof AmqpException && ((AmqpException) error).isTransient()
                    || (error instanceof IllegalStateException)
                    || (error instanceof RejectedExecutionException));

            if (!shouldRetry) {
                logger.atWarning()
                    .addKeyValue(TRY_COUNT_KEY, iteration)
                    .log("Exception is non-retriable, not retrying for a new channel.", error);
                if (error instanceof RecoveryTerminatedException) {
                    return Mono.error(((RecoveryTerminatedException) error).propagate());
                } else {
                    return Mono.error(error);
                }
            }

            final Throwable errorToUse = error instanceof AmqpException
                ? error
                : new AmqpException(true, "Non-AmqpException occurred upstream.", error, null);
            // Using the min of retry attempts and max-retries to compute the 'back-off'.
            // The min is taken so that it never exhaust the retry attempts for transient errors.
            // This will ensure a new channel will be created whenever the underlying transient error
            // is resolved (as long as there is at least one subscriber).
            final long attempts = Math.min(iteration, retryPolicy.getMaxRetries());
            final Duration backoff = retryPolicy.calculateRetryDelay(errorToUse, (int) attempts);

            if (backoff == null) {
                logger.atWarning()
                    .addKeyValue(TRY_COUNT_KEY, iteration)
                    .log("Retry is disabled, not retrying for a new channel.", error);
                return Mono.error(error);
            }

            logger.atInfo()
                .addKeyValue(TRY_COUNT_KEY, iteration)
                .addKeyValue(INTERVAL_KEY, backoff.toMillis())
                .log("Transient error occurred. Retrying.", error);

            return Mono.delay(backoff);
        }));
    }

    /**
     * Check if this cache is in a state where the cache refresh (i.e. recovery of RequestResponseChannel) is no longer
     * possible.
     * <p>
     * The recovery mechanism is terminated once the cache is terminated due to {@link #dispose()} or
     * {@link #closeAsync()} call or the parent {@link ReactorConnection} is in terminated state.
     * Since the parent {@link ReactorConnection} hosts any RequestResponseChannel that RequestResponseChannelCache
     * caches, recovery (scoped to the Connection) is impossible once the Connection is terminated
     * (i.e. connection.isDisposed() == true). This also means RequestResponseChannelCache cannot outlive the Connection.
     *
     * @param callSite the call site checking the recovery termination (for logging).
     * @return {@link RecoveryTerminatedException} if the recovery is terminated, {@code null} otherwise.
     */
    private RecoveryTerminatedException checkRecoveryTerminated(String callSite) {
        final boolean isCacheTerminated = terminated;
        final boolean isConnectionTerminated = connection.isDisposed();
        if (isCacheTerminated || isConnectionTerminated) {
            logger.atInfo()
                .addKeyValue(IS_CACHE_TERMINATED_KEY, isCacheTerminated)
                .addKeyValue(IS_CONNECTION_TERMINATED_KEY, isConnectionTerminated)
                .addKeyValue(CALL_SITE_KEY, callSite)
                .log("Channel recovery support is terminated.");
            return new RecoveryTerminatedException(connection.getId(), isCacheTerminated, isConnectionTerminated);
        }
        return null;
    }

    /**
     * Wait for the channel to be active with a timeout.
     * <p>
     * If the activation timeout or if the channel state transition to completed without being active, then an error
     * will be raised.
     * </p>
     * <p>
     * This API will close the channel if it times out or downstream cancels before becoming active. If the channel
     * state completes without being active, this API will not try to close the channel, since completion signal means
     * it is already closed (See self-close call in RequestResponseChannel endpointStates error and completion handler).
     * </p>
     * @param channel the channel to await to be active.
     * @param timeout the activation timeout.
     * @param logger the logger.
     * @return the channel that is active.
     */
    private static Mono<RequestResponseChannel> awaitToActive(RequestResponseChannel channel, Duration timeout,
        ClientLogger logger) {
        logger.atInfo().log("Waiting for channel to active.");
        return channel.getEndpointStates()
            .filter(s -> s == AmqpEndpointState.ACTIVE)
            .next()
            .switchIfEmpty(Mono.error(() -> new AmqpException(true, "Channel completed without being active.", null)))
            .timeout(timeout, Mono.defer(() -> {
                final String timeoutMessage = "Timeout waiting for channel to be active";
                logger.atInfo().addKeyValue("timeout", timeout).log(timeoutMessage);
                final AmqpException timeoutError = new AmqpException(true, timeoutMessage + " (" + timeout + ")", null);
                return channel.closeAsync().then(Mono.error(timeoutError));
            }))
            .doOnCancel(() -> {
                logger.atInfo().log("The channel request was canceled while waiting to active.");
                if (!channel.isDisposed()) {
                    channel.closeAsync().subscribe();
                }
            })
            .thenReturn(channel);
    }

    /**
     * The error type (internal to the cache) representing the termination of recovery support, which means cache cannot
     * be refreshed any longer.
     *
     * @See {@link RequestResponseChannelCache#checkRecoveryTerminated(String)}.
     */
    private static final class RecoveryTerminatedException extends RuntimeException {
        private final String connectionId;
        private final String message;

        RecoveryTerminatedException(String connectionId, boolean isCacheTerminated, boolean isConnectionTerminated) {
            this.connectionId = connectionId;
            this.message = String.format("%s:%b %s:%b", IS_CACHE_TERMINATED_KEY, isCacheTerminated,
                IS_CONNECTION_TERMINATED_KEY, isConnectionTerminated);
        }

        /**
         * Translate this recovery terminated error to {@link RequestResponseChannelClosedException} to propagate
         * to the downstream of the {@link RequestResponseChannelCache}.
         * <p>
         * Termination of the recovery (due to Cache or Connection termination) means any cached RequestResponseChannel
         * is terminated or no new RequestResponseChannel can host on the Connection. In this case, we intentionally
         * propagate 'RequestResponseChannelClosedException' to downstream. If the downstream is a part async chain with
         * the {@link ReactorConnectionCache} as upstream, then the chain may retry on this specific error type to obtain
         * a new  Connection and a new RequestResponseChannelCache which provides RequestResponseChannel hosted on this
         * new Connection. Examples of such async chains are those that enable Producer and Consumer recovery.
         *
         * @return the {@link RequestResponseChannelClosedException}.
         */
        RequestResponseChannelClosedException propagate() {
            return new RequestResponseChannelClosedException(connectionId, message);
        }
    }
}
