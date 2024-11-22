// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for storing default payload and its associated helpers.
 */
public class HeartbeatDefaultPayload {

    /**
     * List of default payloads which would be added.
     */
    private static final List<HeartBeatPayloadProviderInterface> defaultPayloadProviders = new ArrayList<>();

    static {
        defaultPayloadProviders.add(new DefaultHeartBeatPropertyProvider());
        defaultPayloadProviders.add(new WebAppsHeartbeatProvider());
    }

    /**
     * Callable which delegates calls to providers for adding payload.
     *
     * @param provider The HeartBeat provider
     * @return Callable to perform execution
     */
    public static Runnable populateDefaultPayload(HeartbeatExporter provider) {
        return () -> {
            for (HeartBeatPayloadProviderInterface payloadProvider : defaultPayloadProviders) {
                payloadProvider.setDefaultPayload(provider).run();
            }
        };
    }

    private HeartbeatDefaultPayload() {
    }
}
