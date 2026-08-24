// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.HealthApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC controller for Kubernetes-style health probe endpoints.
 * <p>
 * Delegates to a core {@link HealthApi} implementation to determine health status.
 * Returns HTTP 200 OK when healthy, HTTP 503 Service Unavailable when not.
 */
@RestController
public class HealthController {

    private final HealthApi healthApi;

    /**
     * Creates a health controller with custom health checks.
     *
     * @param healthApi the health API implementation
     */
    public HealthController(HealthApi healthApi) {
        this.healthApi = healthApi;
    }

    /**
     * Readiness probe endpoint.
     *
     * @return HTTP 200 OK if ready, 503 if not
     */
    @GetMapping("/readiness")
    public ResponseEntity<Void> getReadiness() {
        if (healthApi.isReady()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(503).build();
    }

    /**
     * Liveness probe endpoint.
     *
     * @return HTTP 200 OK if alive, 503 if not
     */
    @GetMapping("/liveness")
    public ResponseEntity<Void> getLiveness() {
        if (healthApi.isAlive()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(503).build();
    }
}

