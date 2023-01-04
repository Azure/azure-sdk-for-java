// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
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

public final class RecoverableReactorConnection {
    private static final AmqpException TERMINATED_ERROR = new AmqpException(false, "Connection recovery support is terminated.", null);
    private static final String TRY_COUNT_KEY = "tryCount";
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final AmqpRetryOptions retryOptions;
    private final ClientLogger logger;
    private final AmqpErrorContext errorContext;
    private volatile ReactorConnection currentConnection;
    private final Mono<ReactorConnection> createOrGetCachedConnection;
    private volatile boolean terminated;

    /**
     * Create a RecoverableReactorConnection that is responsible for obtaining a connection, caching it,
     * and replaying to subscribers as long as the cached connection is not closed. Upon downstream request
     * for a connection, if it finds that the cached connection is closed, then cache is refreshed with
     * new connection.
     *
     * @param connectionSupplier the supplier that provides a new connection object, which is not yet
     *                          connected to the host or active.
     * @param fullyQualifiedNamespace The connection FQDN of the remote broker/resource.
     * @param entityPath The relative path to the entity under the FQDN to which the connection established to.
     * @param retryPolicy the retry configuration to use to obtain a new active connection.
     * @param errorContext the error context.
     * @param loggingContext the logger context.
     */
    public RecoverableReactorConnection(Supplier<ReactorConnection> connectionSupplier,
                                        String fullyQualifiedNamespace,
                                        String entityPath,
                                        AmqpRetryPolicy retryPolicy,
                                        AmqpErrorContext errorContext,
                                        Map<String, Object> loggingContext) {
        Objects.requireNonNull(connectionSupplier, "'connectionSupplier' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        // Note: If we find more connection description parameters that are non-generic, i.e., specific to individual
        // messaging services, then consider creating dedicated POJO types in individual libraries rather than polluting
        // shared 'RecoverableReactorConnection' type. FQDN, entity-path still treated as generic.
        this.entityPath = entityPath;
        Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.retryOptions = retryPolicy.getRetryOptions();
        this.errorContext = Objects.requireNonNull(errorContext, "'errorContext' cannot be null.");
        this.logger = new ClientLogger(getClass(), Objects.requireNonNull(loggingContext, "'loggingContext' cannot be null."));

        final Mono<ReactorConnection> newConnection = Mono.fromSupplier(() -> {
            if (terminated) {
                logger.info("Connection recovery support is terminated, dropping the request for new connection.");
                throw TERMINATED_ERROR;
            } else {
                return connectionSupplier.get();
            }
        });

        this.createOrGetCachedConnection = newConnection
            .flatMap(c -> {
                logger.atInfo()
                    .addKeyValue(CONNECTION_ID_KEY, c.getId())
                    .log("Waiting to connect and become active.");

                return c.connectAndAwaitToActive()
                    .doOnCancel(() -> {
                        if (!c.isDisposed()) {
                            c.closeAsync(closeSignal("The connection request was canceled while waiting to active."))
                                .subscribe();
                        }
                    });
            })
            .retryWhen(retryWhenSpec(retryPolicy))
            .<ReactorConnection>handle((c, sink) -> {
                currentConnection = c;
                if (terminated) {
                    currentConnection.closeAsync(closeSignal("Connection recovery support is terminated.")).subscribe();
                    sink.error(TERMINATED_ERROR);
                } else {
                    logger.atInfo()
                        .addKeyValue(CONNECTION_ID_KEY, c.getId())
                        .log("Emitting the new active connection.");
                    sink.next(c);
                }
            }).cacheInvalidateIf(c -> {
                if (c.isDisposed()) {
                    logger.atInfo().addKeyValue(CONNECTION_ID_KEY, c.getId())
                        .log("The connection is closed, requesting a new connection.");
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
    public Mono<ReactorConnection> getConnection() {
        return createOrGetCachedConnection;
    }

    /**
     * Get the connection FQDN of the remote broker/resource.
     *
     * @return the connection FQDN.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Get the relative path to the entity under the FQDN to which the connection established to.
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
     * Terminate so that consumers will no longer be able to request connection. If there is a current (cached)
     * connection then it will be closed.
     */
    public void terminate() {
        if (!terminated) {
            terminated = true;
            if (currentConnection != null && !currentConnection.isDisposed()) {
                currentConnection.closeAsync(closeSignal("Terminating the connection recovery support.")).subscribe();
            } else {
                logger.info("Terminating the connection recovery support.");
            }
        }
    }

    private Retry retryWhenSpec(AmqpRetryPolicy retryPolicy) {
        return Retry.from(retrySignals -> retrySignals
            .concatMap(retrySignal -> {
                final Retry.RetrySignal signal = retrySignal.copy();
                final Throwable error = signal.failure();
                final long iteration = signal.totalRetriesInARow();

                if (error == null) {
                    return Mono.error(new IllegalStateException("RetrySignal::failure() not expected to be null."));
                }

                // There are exceptions that will not be AmqpExceptions like IllegalStateExceptions or
                // RejectedExecutionExceptions when attempting an operation that is closed or if the IO
                // signal is accidentally closed, retrying in these cases as well.
                final boolean shouldRetry = error instanceof TimeoutException
                    || (error instanceof AmqpException && ((AmqpException) error).isTransient()
                    || (error instanceof IllegalStateException)
                    || (error instanceof RejectedExecutionException));

                if (!shouldRetry) {
                    logger.atWarning()
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
                    logger.atWarning()
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

    private static AmqpShutdownSignal closeSignal(String message) {
        return new AmqpShutdownSignal(false, false, message);
    }
}
