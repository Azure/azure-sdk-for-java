// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpResponseCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages the re-authorisation of the client to the token audience against the CBS node.
 */
class ActiveClientTokenManager implements Closeable {
    private final Mono<CBSNode> cbsNodeMono;
    private final String tokenAudience;
    private final Duration tokenValidity;
    private Timer timer;
    private final Flux<AmqpResponseCode> authorizationResults;
    private FluxSink<AmqpResponseCode> sink;

    ActiveClientTokenManager(Mono<CBSNode> cbsNodeMono, String tokenAudience, Duration tokenValidity, Duration refreshInterval) {
        this.timer = new Timer(tokenAudience + "-tokenManager");
        this.cbsNodeMono = cbsNodeMono;
        this.tokenAudience = tokenAudience;
        this.tokenValidity = tokenValidity;
        this.authorizationResults = Flux.create(sink -> this.sink = sink);
        this.timer.schedule(new RefreshAuthorizationToken(), refreshInterval.toMillis());
    }

    /**
     * Gets a flux of the periodic authorisation results from the CBS node. Errors are returned on the Flux if
     * authorization is unsuccessful.
     *
     * @return A Flux of authorisation results from the CBS node.
     */
    public Flux<AmqpResponseCode> getAuthorizationResults() {
        return authorizationResults;
    }

    @Override
    public void close() {
        this.timer.cancel();
        this.sink.complete();
    }

    private class RefreshAuthorizationToken extends TimerTask {
        @Override
        public void run() {
            cbsNodeMono.map(cbsNode -> {
                cbsNode.authorize(tokenAudience, tokenValidity).subscribe(
                    (Void response) -> sink.next(AmqpResponseCode.ACCEPTED),
                    error -> sink.error(error),
                    () -> sink.next(AmqpResponseCode.ACCEPTED));
                return 0;
            });
        }
    }
}
