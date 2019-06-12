// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.implementation.logging.ServiceLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the re-authorisation of the client to the token audience against the CBS node.
 */
class ActiveClientTokenManager implements Closeable {
    private final ServiceLogger logger = new ServiceLogger(ActiveClientTokenManager.class);
    private final AtomicBoolean hasScheduled = new AtomicBoolean();
    private final Mono<CBSNode> cbsNode;
    private final String tokenAudience;
    private final Duration tokenValidity;
    private final Duration refreshInterval;
    private Timer timer;
    private final Flux<AmqpResponseCode> authorizationResults;
    private FluxSink<AmqpResponseCode> sink;

    ActiveClientTokenManager(Mono<CBSNode> cbsNode, String tokenAudience, Duration tokenValidity, Duration refreshInterval) {
        this.timer = new Timer(tokenAudience + "-tokenManager");
        this.cbsNode = cbsNode;
        this.tokenAudience = tokenAudience;
        this.tokenValidity = tokenValidity;
        this.refreshInterval = refreshInterval;
        this.authorizationResults = Flux.create(sink -> this.sink = sink);
    }

    /**
     * Gets a flux of the periodic authorisation results from the CBS node. Errors are returned on the Flux if
     * authorization is unsuccessful.
     *
     * @return A Flux of authorisation results from the CBS node.
     */
    Flux<AmqpResponseCode> getAuthorizationResults() {
        return authorizationResults;
    }

    /**
     * Invokes an authorisation call on the CBS node.
     */
    Mono<Void> authorize() {
        return cbsNode.map(cbsNode -> cbsNode.authorize(tokenAudience, tokenValidity))
            .doOnSuccess(v -> {
                if (!hasScheduled.getAndSet(true)) {
                    logger.asInfo().log("Scheduling refresh token.");
                    this.timer.schedule(new RefreshAuthorizationToken(), refreshInterval.toMillis());
                }
            }).then();
    }

    @Override
    public void close() {
        this.timer.cancel();
        this.sink.complete();
    }

    private class RefreshAuthorizationToken extends TimerTask {
        @Override
        public void run() {
            authorize().subscribe(
                (Void response) -> sink.next(AmqpResponseCode.ACCEPTED),
                error -> sink.error(error),
                () -> sink.next(AmqpResponseCode.ACCEPTED));
        }
    }
}
