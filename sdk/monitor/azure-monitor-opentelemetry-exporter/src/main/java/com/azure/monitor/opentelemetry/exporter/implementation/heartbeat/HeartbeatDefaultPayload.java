// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Container for storing default payload and it's associated helpers.
 */
public class HeartbeatDefaultPayload {

    /**
     * List of default payloads which would be added.
     */
    private static final List<HeartBeatPayloadProviderInterface> defaultPayloadProviders =
        new ArrayList<>();

    static {
        defaultPayloadProviders.add(new DefaultHeartBeatPropertyProvider());
        defaultPayloadProviders.add(new WebAppsHeartbeatProvider());
    }

    private HeartbeatDefaultPayload() {
    }

    /**
     * Callable which delegates calls to providers for adding payload.
     *
     * @param provider The HeartBeat provider
     * @return Callable to perform execution
     */
    public static Callable<Boolean> populateDefaultPayload(HeartbeatExporter provider) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean populatedFields = false;
                for (HeartBeatPayloadProviderInterface payloadProvider : defaultPayloadProviders) {
                    boolean fieldsAreSet = payloadProvider.setDefaultPayload(provider).call();
                    populatedFields = populatedFields || fieldsAreSet;
                }
                return populatedFields;
            }
        };
    }
}
