// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import java.io.IOException;

/**
 * Interface to implement event stream listeners for handling {@link ServerSentEvent}.
 */
@FunctionalInterface
public interface ServerSentEventListener {

    /**
     * Gets called every time an event or data is received.
     *
     * @param sse the instance of {@link ServerSentEvent}
     * @throws IOException if an I/O error occurs
     */
    void onEvent(ServerSentEvent sse) throws IOException;

    /**
     * Gets called if an error has occurred
     *
     * @param throwable Error that occurred
     */
    default void onError(Throwable throwable) {
        // Do nothing
    }

    /**
     * Notify that the connection was closed.
     */
    default void onClose() {
        // Do nothing
    }
}
