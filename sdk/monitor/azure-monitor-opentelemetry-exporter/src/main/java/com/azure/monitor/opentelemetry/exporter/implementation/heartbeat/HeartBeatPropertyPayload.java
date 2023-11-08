// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import reactor.util.annotation.Nullable;

/**
 * Defines the Payload class to store and send heartbeat properties and allowing to keep track of
 * updates to them.
 */
public class HeartBeatPropertyPayload {

    /**
     * Value of payload on initialization. Ready for transmission.
     */
    private String payloadValue = "";

    /**
     * Is this healthy property or not.
     */
    private boolean isHealthy = false;

    /**
     * Returns the payload value.
     */
    String getPayloadValue() {
        return payloadValue;
    }

    /**
     * This is used to set the payload.
     *
     * @param payloadValue value of the property
     */
    public void setPayloadValue(@Nullable String payloadValue) {
        if (payloadValue != null && !this.payloadValue.equals(payloadValue)) {
            this.payloadValue = payloadValue;
        }
    }

    /**
     * Returns the value of payload is healthy.
     */
    public boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Sets the health of the payload.
     *
     * @param healthy boolean value representing the health.
     */
    public void setHealthy(boolean healthy) {
        this.isHealthy = healthy;
    }
}
