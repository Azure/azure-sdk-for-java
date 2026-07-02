// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.net.URI;

/**
 * Outcome of a single {@link EndpointProbeClient} probe against one regional endpoint.
 * Package-private data carrier.
 */
final class EndpointProbeResult {

    final URI endpoint;
    final boolean success;

    EndpointProbeResult(URI endpoint, boolean success) {
        this.endpoint = endpoint;
        this.success = success;
    }

    @Override
    public String toString() {
        return (endpoint == null ? "<unknown>" : endpoint.toString()) + " -> " + (success ? "GREEN" : "RED");
    }
}
