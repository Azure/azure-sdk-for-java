// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A key/value store that is propagated between various poll related operations associated with
 * {@link PollerFlux} and {@link SyncPoller} poller. The context also expose activation and
 * latest {@link PollResponse}.
 *
 * @param <T> the type of the poll response.
 */
public final class PollingContext<T> {
    private final ClientLogger logger = new ClientLogger(PollingContext.class);
    private final Map<String, String> map;
    private volatile PollResponse<T> activationResponse;
    private volatile PollResponse<T> latestResponse;

    /**
     * Get a value from the context with the provided key.
     *
     * @param name the key to look for
     * @return the value of the key if exists, else null
     */
    public String getData(String name) {
        return this.map.get(name);
    }

    /**
     * Set a key-value pair in the context.
     *
     * @param name the key
     * @param value the value
     * @return an updated PollingContext
     */
    public PollingContext<T> setData(String name, String value) {
        this.map.put(name, value);
        return this;
    }

    /**
     * @return the activation {@link PollResponse} holding result of activation operation call.
     */
    public PollResponse<T> getActivationResponse() {
        return this.activationResponse;
    }

    /**
     * @return the latest {@link PollResponse} from pollOperation.
     */
    public PollResponse<T> getLatestResponse() {
        return this.latestResponse;
    }

    /**
     * Sets latest {@link PollResponse} from pollOperation.
     *
     * PACKAGE INTERNAL METHOD
     *
     * @param latestResponse the poll response
     */
    void setLatestResponse(PollResponse<T> latestResponse) {
        this.latestResponse = Objects.requireNonNull(latestResponse,
                "'latestResponse' is required.");
    }

    /**
     * Sets activation {@link PollResponse} holding result of activation operation call.
     *
     * PACKAGE INTERNAL METHOD
     *
     * @param activationResponse the activation response
     */
    void setOnetimeActivationResponse(PollResponse<T> activationResponse) {
        if (this.activationResponse != null) {
            throw logger
                    .logExceptionAsError(new IllegalStateException(
                            "setOnetimeActivationResponse can be called only once."));
        } else {
            this.activationResponse = activationResponse;
            this.latestResponse = this.activationResponse;
        }
    }

    PollingContext<T> copy() {
        return new PollingContext<>(this.activationResponse,
                this.latestResponse,
                new HashMap<>(this.map));
    }

    /**
     * Creates PollingContext.
     *
     * Package internal default constructor.
     */
    PollingContext() {
        this.map = new HashMap<>();
    }

    /**
     * Creates PollingContext.
     *
     * @param activationResponse activation poll response holding result of activation operation call.
     * @param latestResponse latest poll response from pollOperation.
     * @param map the map to store context
     */
    private PollingContext(PollResponse<T> activationResponse,
                           PollResponse<T> latestResponse,
                           Map<String, String> map) {
        this.activationResponse = Objects.requireNonNull(activationResponse,
                "'activationResponse' cannot be null.");
        this.latestResponse = Objects.requireNonNull(latestResponse,
                "'latestResponse' cannot be null.");
        this.map = Objects.requireNonNull(map,
                "'map' cannot be null.");
    }
}
