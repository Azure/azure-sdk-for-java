// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import java.time.Duration;

/**
 * Options to configure the IdentityClient.
 */
public class DevToolsClientOptions extends ClientOptions {
    private Duration processTimeout;
    private String subscription;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public DevToolsClientOptions() {
        super();
    }

    /**
     * Creates a copy of dev tools client options from provided client options instance.
     *
     * @param clientOptions the dev tools client options to copy.
     */
    public DevToolsClientOptions(DevToolsClientOptions clientOptions) {
        super(clientOptions);
        this.processTimeout = clientOptions.getProcessTimeout();
        this.subscription = clientOptions.getSubscription();
    }

    /**
     * Creates a copy of dev tools client options from provided client options instance.
     *
     * @param clientOptions the client options to copy.
     */
    public DevToolsClientOptions(ClientOptions clientOptions) {
        super(clientOptions);
    }

    /**
     * Gets the configured process timeout.
     *
     * @return the process timeout.
     */
    public Duration getProcessTimeout() {
        if (processTimeout == null) {
            processTimeout = Duration.ofSeconds(10);
        }
        return processTimeout;
    }

    /**
     * Sets the process timeout.
     *
     * @param processTimeout the process timeout.
     * @return the updated options
     */
    public DevToolsClientOptions setProcessTimeout(Duration processTimeout) {
        this.processTimeout = processTimeout;
        return this;
    }

    /**
     * Gets the configured subscription name/ID.
     *
     * @return the subscription name/ID
     */
    public String getSubscription() {
        return subscription;
    }

    /**
     * Sets the subscription name/ID.
     *
     * @param subscription the subscription name/ID
     * @return the updated options
     */
    public DevToolsClientOptions setSubscription(String subscription) {
        this.subscription = subscription;
        return this;
    }
}
