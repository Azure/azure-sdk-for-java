// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

/**
 * Interface for Kubernetes-style health probe endpoints.
 * <p>
 * Implementations indicate whether the application is ready to serve traffic
 * (readiness) and whether the process is alive (liveness).
 * <p>
 * The default implementation always returns {@code true} for both probes.
 * Override to add custom health checks (e.g., database connectivity, downstream
 * service availability).
 */
public interface HealthApi {

    /**
     * Returns whether the application is ready to serve requests.
     * <p>
     * A {@code false} return value causes the orchestrator to stop routing traffic
     * to this instance until it becomes ready again.
     *
     * @return {@code true} if ready, {@code false} otherwise
     */
    default boolean isReady() {
        return true;
    }

    /**
     * Returns whether the application process is alive and responsive.
     * <p>
     * A {@code false} return value causes the orchestrator to restart the container.
     *
     * @return {@code true} if alive, {@code false} otherwise
     */
    default boolean isAlive() {
        return true;
    }
}

