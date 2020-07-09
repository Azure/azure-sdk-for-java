// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the re-authorization of the client to the token audience against the CBS node.
 */
public class ActiveClientTokenManager implements TokenManager {
    private final ClientLogger logger = new ClientLogger(ActiveClientTokenManager.class);
    private final AtomicBoolean hasScheduled = new AtomicBoolean();
    private final AtomicBoolean hasDisposed = new AtomicBoolean();
    private final Mono<ClaimsBasedSecurityNode> cbsNode;
    private final String tokenAudience;
    private final String scopes;
    private final ReplayProcessor<AmqpResponseCode> authorizationResults = ReplayProcessor.create(1);
    private final FluxSink<AmqpResponseCode> authorizationResultsSink =
        authorizationResults.sink(FluxSink.OverflowStrategy.BUFFER);
    private final EmitterProcessor<Duration> durationSource = EmitterProcessor.create();
    private final FluxSink<Duration> durationSourceSink = durationSource.sink();
    private final AtomicReference<Duration> lastRefreshInterval = new AtomicReference<>(Duration.ofMinutes(1));

    private volatile Disposable subscription;

    public ActiveClientTokenManager(Mono<ClaimsBasedSecurityNode> cbsNode, String tokenAudience, String scopes) {
        this.cbsNode = cbsNode;
        this.tokenAudience = tokenAudience;
        this.scopes = scopes;
    }

    /**
     * Gets a flux of the periodic authorization results from the CBS node. Errors are returned on the Flux if
     * authorization is unsuccessful.
     *
     * @return A Flux of authorization results from the CBS node.
     */
    @Override
    public Flux<AmqpResponseCode> getAuthorizationResults() {
        return authorizationResults;
    }

    /**
     * Invokes an authorization call on the CBS node.
     *
     * @return A Mono that completes with the milliseconds corresponding to when this token should be refreshed.
     */
    @Override
    public Mono<Long> authorize() {
        if (hasDisposed.get()) {
            return Mono.error(new AzureException(
                "Cannot authorize with CBS node when this token manager has been disposed of."));
        }

        return cbsNode.flatMap(cbsNode -> cbsNode.authorize(tokenAudience, scopes))
            .map(expiresOn -> {
                final Duration between = Duration.between(OffsetDateTime.now(ZoneOffset.UTC), expiresOn);

                // We want to refresh the token when 90% of the time before expiry has elapsed.
                final long refreshSeconds = (long) Math.floor(between.getSeconds() * 0.9);

                // This converts it to milliseconds
                final long refreshIntervalMS = refreshSeconds * 1000;

                // If this is the first time authorize is called, the task will not have been scheduled yet.
                if (!hasScheduled.getAndSet(true)) {
                    logger.info("Scheduling refresh token task. scopes[{}]", scopes);

                    final Duration firstInterval = Duration.ofMillis(refreshIntervalMS);
                    lastRefreshInterval.set(firstInterval);
                    authorizationResultsSink.next(AmqpResponseCode.ACCEPTED);
                    subscription = scheduleRefreshTokenTask(firstInterval);
                }

                return refreshIntervalMS;
            });
    }

    @Override
    public void close() {
        if (hasDisposed.getAndSet(true)) {
            return;
        }

        authorizationResultsSink.complete();
        durationSourceSink.complete();

        if (subscription != null) {
            subscription.dispose();
        }
    }

    private Disposable scheduleRefreshTokenTask(Duration initialRefresh) {
        // EmitterProcessor can queue up an initial refresh interval before any subscribers are received.
        durationSourceSink.next(initialRefresh);

        return Flux.switchOnNext(durationSource.map(Flux::interval))
            .flatMap(delay -> {
                logger.info("Refreshing token. scopes[{}] ", scopes);
                return authorize();
            })
            .onErrorContinue(
                error -> (error instanceof AmqpException) && ((AmqpException) error).isTransient(),
                (amqpException, interval) -> {
                    final Duration lastRefresh = lastRefreshInterval.get();

                    logger.error("Error is transient. Rescheduling authorization task at interval {} ms. scopes[{}]",
                        lastRefresh.toMillis(), scopes, amqpException);
                    durationSourceSink.next(lastRefreshInterval.get());
                })
            .subscribe(interval -> {
                logger.info("Authorization successful. Refreshing token in {} ms. scopes[{}]", interval, scopes);
                authorizationResultsSink.next(AmqpResponseCode.ACCEPTED);

                final Duration nextRefresh = Duration.ofMillis(interval);
                lastRefreshInterval.set(nextRefresh);
                durationSourceSink.next(Duration.ofMillis(interval));
            }, error -> {
                    logger.error("Error occurred while refreshing token that is not retriable. Not scheduling"
                        + " refresh task. Use ActiveClientTokenManager.authorize() to schedule task again. audience[{}]"
                        + " scopes[{}]", tokenAudience, scopes, error);

                    // This hasn't been disposed yet.
                    if (!hasDisposed.getAndSet(true)) {
                        hasScheduled.set(false);
                        durationSourceSink.complete();
                        authorizationResultsSink.error(error);
                    }
                });
    }
}
