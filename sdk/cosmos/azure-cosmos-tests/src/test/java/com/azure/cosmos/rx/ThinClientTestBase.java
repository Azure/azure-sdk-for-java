// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Stateless helper holder for thin client E2E tests: shared constants and static endpoint-routing
 * assertions used across thin-client test classes. This is intentionally NOT a TestNG base class -
 * no test class extends it and it owns no client/container lifecycle; consumers only static-import
 * the assertion helpers and reference the shared constants.
 */
public final class ThinClientTestBase {

    static final String THIN_CLIENT_ENDPOINT_INDICATOR = ":10250/";
    static final String ID_FIELD = "id";
    static final String PARTITION_KEY_FIELD = "mypk";
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ThinClientTestBase() {
    }

    /**
     * Asserts that all requests in the diagnostics were routed through the thin client endpoint.
     */
    public static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        // Delegate to the shared TestSuiteBase implementation so the thin-client routing invariant
        // (every non-QueryPlan request via the thin-client endpoint; QueryPlan may use the classic
        // gateway) and null-endpoint handling are applied consistently across all thin-client tests.
        TestSuiteBase.assertThinClientEndpointUsed(diagnostics);
    }

    /**
     * Asserts that NO requests in the diagnostics were routed through the thin client endpoint,
     * confirming the gateway client used the standard :443 path.
     */
    public static void assertGatewayEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();
        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();
        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            assertThat(requestInfo.getEndpoint())
                .as("Gateway client must not route through thin client endpoint, but found: " + requestInfo.getEndpoint())
                .doesNotContain(THIN_CLIENT_ENDPOINT_INDICATOR);
        }
    }
}
