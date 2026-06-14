// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.net.URI;

/**
 * Outcome of a single {@link EndpointProbeClient} probe against one regional endpoint.
 * Package-private data carrier; {@code reason} is included in failure logs for triage.
 */
final class EndpointProbeResult {

    final URI endpoint;
    final boolean success;
    final String reason;

    EndpointProbeResult(URI endpoint, boolean success, String reason) {
        this.endpoint = endpoint;
        this.success = success;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return (endpoint == null ? "<unknown>" : endpoint.toString()) + " -> " + (success ? "GREEN" : "RED")
            + " (" + reason + ")";
    }
}
