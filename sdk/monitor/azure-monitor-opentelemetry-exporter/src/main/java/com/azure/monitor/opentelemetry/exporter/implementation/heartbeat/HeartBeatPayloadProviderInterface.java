// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

/**
 * <h1>Interface for setting default properties</h1>
 *
 * <p>This interface is used to set the default payload of a provider and defines implementation for
 * some helper methods to assist it.
 *
 * <p>The default concrete implementations are {@link DefaultHeartBeatPropertyProvider} and {@link
 * WebAppsHeartbeatProvider}
 */
public interface HeartBeatPayloadProviderInterface {

    /**
     * Returns a callable which can be executed to set the payload based on the parameters.
     *
     * @param provider The current heartbeat provider
     * @return Callable which can be executed to add the payload
     */
    Runnable setDefaultPayload(HeartbeatExporter provider);
}
