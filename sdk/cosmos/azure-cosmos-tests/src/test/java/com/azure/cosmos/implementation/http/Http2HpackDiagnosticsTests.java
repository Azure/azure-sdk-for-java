// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http2.Http2HeadersEncoder;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2HpackDiagnosticsTests {

    @Test(groups = "unit")
    public void delegatesToUnderlyingDetector() {
        // The diagnostics wrapper should delegate isSensitive to the real detector
        Http2HpackDiagnostics diagnostics =
            new Http2HpackDiagnostics(CosmosHttp2SensitivityDetector.INSTANCE);

        assertThat(diagnostics.isSensitive("authorization", "type=master&sig=abc"))
            .as("authorization should be sensitive via delegate")
            .isTrue();

        assertThat(diagnostics.isSensitive("User-Agent", "cosmos-sdk/4.0"))
            .as("User-Agent should not be sensitive via delegate")
            .isFalse();
    }

    @Test(groups = "unit")
    public void tracksHeaderCardinality() {
        Http2HpackDiagnostics diagnostics =
            new Http2HpackDiagnostics(Http2HeadersEncoder.NEVER_SENSITIVE);

        // Simulate multiple requests with different authorization values
        for (int i = 0; i < 100; i++) {
            diagnostics.isSensitive("authorization", "sig=" + i);
        }

        // Simulate static headers (same value every time)
        for (int i = 0; i < 100; i++) {
            diagnostics.isSensitive("user-agent", "cosmos-sdk/4.0");
        }

        // Simulate low-cardinality header
        for (int i = 0; i < 100; i++) {
            diagnostics.isSensitive("x-ms-consistency-level", i % 3 == 0 ? "Session" : "Eventual");
        }

        // Verify: log report should complete without error
        diagnostics.logDiagnosticReport();
    }

    @Test(groups = "unit")
    public void tracksMultipleUniqueValues() {
        Http2HpackDiagnostics diagnostics =
            new Http2HpackDiagnostics(Http2HeadersEncoder.NEVER_SENSITIVE);

        // Feed 50 unique values for x-ms-date
        for (int i = 0; i < 50; i++) {
            diagnostics.isSensitive("x-ms-date", "Tue, 18 Mar 2025 03:" + i + ":00 GMT");
        }

        // Feed 1 unique value for content-type (static)
        for (int i = 0; i < 50; i++) {
            diagnostics.isSensitive("content-type", "application/json");
        }

        // The diagnostics report should show x-ms-date with ~50 unique values
        // and content-type with 1 unique value
        diagnostics.logDiagnosticReport();
    }

    @Test(groups = "unit")
    public void handlesNullHpackEncoderGracefully() {
        Http2HpackDiagnostics diagnostics =
            new Http2HpackDiagnostics(Http2HeadersEncoder.NEVER_SENSITIVE);

        // Don't set HpackEncoder — should still produce a report
        diagnostics.isSensitive("test-header", "value");
        diagnostics.logDiagnosticReport();
    }

    @Test(groups = "unit")
    public void respectsBoundedUniqueValueTracking() {
        Http2HpackDiagnostics diagnostics =
            new Http2HpackDiagnostics(Http2HeadersEncoder.NEVER_SENSITIVE);

        // Feed more than the cap (500) unique values
        for (int i = 0; i < 1000; i++) {
            diagnostics.isSensitive("high-cardinality-header", "unique-value-" + i);
        }

        // Should not throw and report should note the cap was reached
        diagnostics.logDiagnosticReport();
    }
}
