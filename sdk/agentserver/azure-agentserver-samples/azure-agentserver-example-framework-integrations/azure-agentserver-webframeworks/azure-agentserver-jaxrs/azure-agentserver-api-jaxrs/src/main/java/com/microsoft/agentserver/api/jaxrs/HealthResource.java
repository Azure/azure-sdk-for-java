// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.HealthApi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS resource classes for Kubernetes-style health probe endpoints.
 * <p>
 * Delegates to a core {@link HealthApi} implementation to determine health status.
 * Returns HTTP 200 OK when healthy, HTTP 503 Service Unavailable when not.
 */
public class HealthResource {

    private final HealthApi healthApi;

    /**
     * Creates a health resource with custom health checks.
     *
     * @param healthApi the health API implementation
     */
    public HealthResource(HealthApi healthApi) {
        this.healthApi = healthApi;
    }

    /**
     * Creates a health resource with default (always-healthy) checks.
     */
    public HealthResource() {
        this(new HealthApi() {
        });
    }

    /**
     * Readiness probe endpoint.
     */
    @Path("/readiness")
    public static class Readiness {
        private final HealthApi healthApi;

        public Readiness(HealthApi healthApi) {
            this.healthApi = healthApi;
        }

        public Readiness() {
            this(new HealthApi() {
            });
        }

        /**
         * Handles GET requests to the readiness probe.
         *
         * @return HTTP 200 OK if ready, 503 if not
         */
        @GET
        public Response getReadiness() {
            if (healthApi.isReady()) {
                return Response.ok().build();
            }
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Liveness probe endpoint.
     */
    @Path("/liveness")
    public static class Liveness {
        private final HealthApi healthApi;

        public Liveness(HealthApi healthApi) {
            this.healthApi = healthApi;
        }

        public Liveness() {
            this(new HealthApi() {
            });
        }

        /**
         * Handles GET requests to the liveness probe.
         *
         * @return HTTP 200 OK if alive, 503 if not
         */
        @GET
        public Response getLiveness() {
            if (healthApi.isAlive()) {
                return Response.ok().build();
            }
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
}
