/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

/**
 * Defines values for ApplicationGatewayOperationalState.
 */
public final class ApplicationGatewayOperationalState {
    /** Static value Stopped for ApplicationGatewayOperationalState. */
    public static final String STOPPED = "Stopped";

    /** Static value Starting for ApplicationGatewayOperationalState. */
    public static final String STARTING = "Starting";

    /** Static value Running for ApplicationGatewayOperationalState. */
    public static final String RUNNING = "Running";

    /** Static value Stopping for ApplicationGatewayOperationalState. */
    public static final String STOPPING = "Stopping";

    private ApplicationGatewayOperationalState() {
    }
}
