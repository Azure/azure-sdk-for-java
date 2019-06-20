// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * Claims-based security (CBS) node that authorizes connections with AMQP services.
 *
 * @see <a href="https://www.oasis-open.org/committees/download.php/62097/amqp-cbs-v1.0-wd05.doc">
 * AMPQ Claims-based Security v1.0</a>
 */
public interface CBSNode extends EndpointStateNotifier, Closeable {
    /**
     * Authorizes the caller with the CBS node to access resources for the {@code audience}.
     *
     * @param audience Resource that the callee needs access to.
     * @return A Mono that completes when the authorization is successful and errors if the authorization was
     * unsuccessful.
     */
    Mono<Void> authorize(String audience);
}
