// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
    private final Timer timer;
    private final Flux<AmqpResponseCode> authorizationResults;
    private FluxSink<AmqpResponseCode> sink;

    // last refresh interval in milliseconds.
    private AtomicLong lastRefreshInterval = new AtomicLong();

    public ActiveClientTokenManager(Mono<ClaimsBasedSecurityNode> cbsNode, String tokenAudience, String scopes) {
        this.timer = new Timer(tokenAudience + "-tokenManager");
        this.cbsNode = cbsNode;
        this.tokenAudience = tokenAudience;
        this.scopes = scopes;
        this.authorizationResults = Flux.create(sink -> {
            if (hasDisposed.get()) {
                sink.complete();
            } else {
                this.sink = sink;
            }
        });

        lastRefreshInterval.set(Duration.ofMinutes(1).getSeconds() * 1000);
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

                lastRefreshInterval.set(refreshIntervalMS);

                // If this is the first time authorize is called, the task will not have been scheduled yet.
                if (!hasScheduled.getAndSet(true)) {
                    logger.info("Scheduling refresh token task.");
                    scheduleRefreshTokenTask(refreshIntervalMS);
                }

                return refreshIntervalMS;
            });
    }

    @Override
    public void close() {
        if (!hasDisposed.getAndSet(true)) {
            if (this.sink != null) {
                this.sink.complete();
            }

            this.timer.cancel();
        }
    }

    private void scheduleRefreshTokenTask(Long refreshIntervalInMS) {
        try {
            timer.schedule(new RefreshAuthorizationToken(), refreshIntervalInMS);
        } catch (IllegalStateException e) {
            logger.warning("Unable to schedule RefreshAuthorizationToken task.", e);
            hasScheduled.set(false);
        }
    }

    private class RefreshAuthorizationToken extends TimerTask {
        @Override
        public void run() {
            logger.info("Refreshing authorization token.");
            authorize().subscribe(
                (Long refreshIntervalInMS) -> {

                    if (hasDisposed.get()) {
                        logger.info("Token manager has been disposed of. Not rescheduling.");
                        return;
                    }

                    logger.info("Authorization successful. Refreshing token in {} ms.", refreshIntervalInMS);
                    sink.next(AmqpResponseCode.ACCEPTED);

                    scheduleRefreshTokenTask(refreshIntervalInMS);
                }, error -> {
                    if ((error instanceof AmqpException) && ((AmqpException) error).isTransient()) {
                        logger.error("Error is transient. Rescheduling authorization task.", error);
                        scheduleRefreshTokenTask(lastRefreshInterval.get());
                    } else {
                        logger.error("Error occurred while refreshing token that is not retriable. Not scheduling"
                            + " refresh task. Use ActiveClientTokenManager.authorize() to schedule task again.", error);
                        hasScheduled.set(false);
                    }

                    sink.error(error);
                });
        }
    }
}
