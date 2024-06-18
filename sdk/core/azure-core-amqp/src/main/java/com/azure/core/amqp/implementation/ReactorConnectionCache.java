// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.INTERVAL_KEY;

/**
 * A cache that is responsible for creating and maintaining a single connection to a message broker. This class
 * is responsible for creating a new connection if the current connection is closed or if there is no current
 * connection.
 *
 * @param <T> The type of connection to cache.
 */
public final class ReactorConnectionCache<T extends ReactorConnection> implements Disposable {
    private static final AmqpException TERMINATED_ERROR
        = new AmqpException(false, "Connection recovery support is terminated.", null);
    private static final String TRY_COUNT_KEY = "tryCount";
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final AmqpRetryOptions retryOptions;
    private final AmqpErrorContext errorContext;
    private final ClientLogger logger;
    private final Mono<T> createOrGetCachedConnection;
    private final Object lock = new Object();
    private volatile boolean terminated;
    // Note: The only reason to have below 'currentConnection' is to close the cached Connection internally
    // upon 'ReactorConnectionCache' termination. We must never expose 'currentConnection' variable to
    // any dependent type; instead, the dependent type must acquire Connection only through the cache route,
    // i.e., by subscribing to 'createOrGetCachedConnection' via 'get()' getter.
    private volatile T currentConnection;

    /**
     * Create a ReactorConnectionCache that is responsible for obtaining a connection, waiting for it to active,
     * caching it, and replaying to subscribers as long as the cached connection is not closed. Upon downstream
     * request for a connection, if the cache finds that the cached connection is closed, then cache is refreshed
     * with new connection.
     *
     * @param connectionSupplier the supplier that provides a new connection object, which is not yet
     *                          connected to the host or active.
     * @param fullyQualifiedNamespace The fully qualified namespace of the remote broker/resource.
     * @param entityPath The relative path to the entity under the fully qualified namespace to which the connection established to.
     * @param retryPolicy the retry configuration to use to obtain a new active connection.
     * @param loggingContext the logger context.
     */
    public ReactorConnectionCache(Supplier<T> connectionSupplier, String fullyQualifiedNamespace, String entityPath,
        AmqpRetryPolicy retryPolicy, Map<String, Object> loggingContext) {
        this.fullyQualifiedNamespace
            = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        // Note: fullyQualifiedNamespace, (to an extent) entity-path are generic enough, but if we find more connection
        // description parameters that are non-generic, i.e., specific to individual messaging services, then consider
        // creating dedicated POJO types to pass around connection description parameters in corresponding libraries
        // rather than polluting shared 'ReactorConnectionCache' type.
        this.entityPath = entityPath;
        Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.retryOptions = retryPolicy.getRetryOptions();
        this.errorContext = new AmqpErrorContext(fullyQualifiedNamespace);
        this.logger
            = new ClientLogger(getClass(), Objects.requireNonNull(loggingContext, "'loggingContext' cannot be null."));
        Objects.requireNonNull(connectionSupplier, "'connectionSupplier' cannot be null.");
        final Mono<T> newConnection = Mono.fromSupplier(() -> {
            if (terminated) {
                logger.info("Connection recovery support is terminated, dropping the request for new connection.");
                throw TERMINATED_ERROR;
            } else {
                return connectionSupplier.get();
            }
        });

        this.createOrGetCachedConnection = newConnection.flatMap(c -> {
            withConnectionId(logger, c.getId()).log("Waiting to connect and active.");

            return c.connectAndAwaitToActive().doOnCancel(() -> {
                if (!c.isDisposed()) {
                    closeConnection(c, logger, "Request was canceled while waiting to connect and active.");
                }
            });
        }).retryWhen(retryWhenSpec(retryPolicy)).<T>handle((c, sink) -> {
            @SuppressWarnings("unchecked")
            final T connection = (T) c;
            final boolean terminated;
            synchronized (lock) {
                terminated = this.terminated;
                currentConnection = connection;
            }
            if (terminated) {
                closeConnection(connection, logger, "Connection recovery support is terminated.");
                sink.error(TERMINATED_ERROR);
            } else {
                withConnectionId(logger, c.getId()).log("Emitting the new active connection.");
                sink.next(connection);
            }
        }).cacheInvalidateIf(c -> {
            if (c.isDisposed()) {
                withConnectionId(logger, c.getId()).log("The connection is closed, requesting a new connection.");
                return true;
            } else {
                // Emit cached connection.
                return false;
            }
        });
    }

    /**
     * Get the Mono that, when subscribed, emits the cached connection if it is active or creates and
     * emits a new connection if the cache is empty or the current cached connection is in closed state.
     *
     * @return a Mono that emits active connection.
     */
    public Mono<T> get() {
        return createOrGetCachedConnection;
    }

    /**
     * Get the connection fully qualified namespace of the remote broker/resource.
     *
     * @return the connection fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Get the relative path to the entity under the fully qualified namespace to which the connection established to.
     *
     * @return the entity path.
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Get the retry option object describing the retry parameters driving the connection recovery.
     *
     * @return The options for connection recovery retries.
     */
    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * The AmqpChannelProcessor has the API 'isChannelClosed()' with impl as
     * "return currentChannel == null || isDisposed();"
     * That API is backing the 'EventHubConsumerAsyncClient::isConnectionClosed()' API,
     * which is used in 'PartitionPumpManager'. That original code introduced seems not correct,
     * but at the moment, it's still being determined what the side effects of removing that would be.
     *
     * @return true if the current cached connection is closed.
     */
    public boolean isCurrentConnectionClosed() {
        return (currentConnection != null && currentConnection.isDisposed()) || terminated;
    }

    /**
     * Terminate so that consumers will no longer be able to request connection. If there is a current (cached)
     * connection then it will be closed.
     */
    @Override
    public void dispose() {
        final ReactorConnection connection;
        synchronized (lock) {
            if (terminated) {
                return;
            }
            terminated = true;
            connection = currentConnection;
        }
        if (connection != null && !connection.isDisposed()) {
            closeConnection(connection, logger, "Terminating the connection recovery support.");
        } else {
            logger.info("Terminating the connection recovery support.");
        }
    }

    @Override
    public boolean isDisposed() {
        return terminated;
    }

    /**
     * Provides the retry spec describing the retry strategy for obtaining new connection when refreshing the cache.
     *
     * @param retryPolicy the retry policy.
     * @return the retry spec.
     */
    Retry retryWhenSpec(AmqpRetryPolicy retryPolicy) {
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
            // v1 AmqpChannelProcessor that v2 ReactorConnectionCache replaces.
            // https://github.com/Azure/azure-sdk-for-java/pull/34122 addresses one source of ISE.
            // Continue to log (to detect any other unknown edge-case) and recover on ISE.
            final boolean shouldRetry = error instanceof TimeoutException
                || (error instanceof AmqpException && ((AmqpException) error).isTransient()
                    || (error instanceof IllegalStateException)
                    || (error instanceof RejectedExecutionException));

            if (!shouldRetry) {
                logger.atError()
                    .addKeyValue(TRY_COUNT_KEY, iteration)
                    .log("Exception is non-retriable, not retrying for a new connection.", error);
                return Mono.error(error);
            }

            final Throwable errorToUse = error instanceof AmqpException
                ? error
                : new AmqpException(true, "Non-AmqpException occurred upstream.", error, errorContext);
            // Using the min of retry attempts and max-retries to compute the 'back-off'.
            // The min is taken so that it never exhaust the retry attempts for transient errors.
            // This will ensure a new connection will be created whenever the underlying transient error
            // is resolved. For e.g. when a network connection is lost for an extended period of time and
            // when the network is restored later, we should be able to recreate a new connection
            // as long as there is at least one subscriber.
            final long attempts = Math.min(iteration, retryPolicy.getMaxRetries());
            final Duration backoff = retryPolicy.calculateRetryDelay(errorToUse, (int) attempts);

            if (backoff == null) {
                logger.atError()
                    .addKeyValue(TRY_COUNT_KEY, iteration)
                    .log("Retry is disabled, not retrying for a new connection.", error);
                return Mono.error(error);
            }

            if (terminated) {
                return Mono.error(TERMINATED_ERROR);
            }

            logger.atInfo()
                .addKeyValue(TRY_COUNT_KEY, iteration)
                .addKeyValue(INTERVAL_KEY, backoff.toMillis())
                .log("Transient error occurred. Retrying.", error);

            return Mono.delay(backoff);
        }));
    }

    /**
     * Often, any connection closure (due to network error) happens outside of cache, and the cache needs to only check
     * if cached connection is in closed state to see if it needs to be refreshed. But there are two cases when cache
     * needs to close a connection explicitly,
     * <ol>
     * <li>When the cache itself is disposed then any cached connection or connection being cached needs to be closed
     * explicitly. Such a cache disposal happens when all clients sharing the connection are closed.</li>
     * <li>If all downstream cancels subscription to the cache while waiting for a new connection to connect and active,
     * then that connection needs to be closed explicitly.</li>
     * </ol>
     *
     * @param c the connection to close.
     * @param logger the logger to log the closing of connection.
     * @param message the message to log.
     */
    private static void closeConnection(ReactorConnection c, ClientLogger logger, String message) {
        final LoggingEventBuilder builder = withConnectionId(logger, c.getId());
        builder.log("closing connection (" + message + ").");
        // Listen and log any terminal signals here, though when connection close finishes, closeAsync()::Mono only
        // emits "completion terminal signal" no matter if there was an error while closing or not.
        c.closeAsync().subscribe(__ -> {
        }, t -> builder.log("connection close finished with error.", t),
            () -> builder.log("connection close finished."));
    }

    private static LoggingEventBuilder withConnectionId(ClientLogger logger, String id) {
        return logger.atInfo().addKeyValue(CONNECTION_ID_KEY, id);
    }
}
