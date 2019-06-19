// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.exception.AzureException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the re-authorization of the client to the token audience against the CBS node.
 */
class ActiveClientTokenManager implements Closeable {
    private final ClientLogger logger = new ClientLogger(ActiveClientTokenManager.class);
    private final AtomicBoolean hasScheduled = new AtomicBoolean();
    private final AtomicBoolean hasDisposed = new AtomicBoolean();
    private final Mono<CBSNode> cbsNode;
    private final String tokenAudience;
    private final Duration refreshInterval;
    private final Timer timer;
    private final Flux<AmqpResponseCode> authorizationResults;
    private FluxSink<AmqpResponseCode> sink;

    ActiveClientTokenManager(Mono<CBSNode> cbsNode, String tokenAudience, Duration refreshInterval) {
        this.timer = new Timer(tokenAudience + "-tokenManager");
        this.cbsNode = cbsNode;
        this.tokenAudience = tokenAudience;
        this.refreshInterval = refreshInterval;
        this.authorizationResults = Flux.create(sink -> {
            if (hasDisposed.get()) {
                sink.complete();
            } else {
                this.sink = sink;
            }
        });
    }

    /**
     * Gets a flux of the periodic authorization results from the CBS node. Errors are returned on the Flux if
     * authorization is unsuccessful.
     *
     * @return A Flux of authorization results from the CBS node.
     */
    Flux<AmqpResponseCode> getAuthorizationResults() {
        return authorizationResults;
    }

    /**
     * Invokes an authorization call on the CBS node.
     */
    Mono<Void> authorize() {
        if (hasDisposed.get()) {
            return Mono.error(new AzureException("Cannot authorize with CBS node when this token manager has been disposed of."));
        }

        return cbsNode.flatMap(cbsNode -> cbsNode.authorize(tokenAudience))
            .then()
            .doOnSuccess(x -> {
                if (!hasScheduled.getAndSet(true)) {
                    logger.asInfo().log("Scheduling refresh token.");
                    this.timer.schedule(new RefreshAuthorizationToken(), refreshInterval.toMillis());
                }
            });
    }

    @Override
    public void close() {
        if (!hasDisposed.getAndSet(true)) {
            this.timer.cancel();

            if (this.sink != null) {
                this.sink.complete();
            }
        }
    }

    private class RefreshAuthorizationToken extends TimerTask {
        @Override
        public void run() {
            logger.asInfo().log("Refreshing authorization token.");
            authorize().subscribe(
                (Void response) -> {
                    logger.asInfo().log("Response acquired.");
                }, error -> {
                    if ((error instanceof AmqpException) && ((AmqpException) error).isTransient()) {
                        logger.asError().log("Error is transient. Rescheduling authorization task.", error);
                        timer.schedule(new RefreshAuthorizationToken(), refreshInterval.toMillis());
                    } else {
                        logger.asError().log("Error occurred while refreshing token that is not retriable. Not scheduling"
                            + " refresh task. Use ActiveClientTokenManager.authorize() to schedule task again.", error);
                        hasScheduled.set(false);
                    }

                    sink.error(error);
                }, () -> {
                    logger.asInfo().log("Success. Rescheduling refresh authorization task.");
                    sink.next(AmqpResponseCode.ACCEPTED);

                    if (hasScheduled.getAndSet(true)) {
                        timer.schedule(new RefreshAuthorizationToken(), refreshInterval.toMillis());
                    }
                });
        }
    }
}
