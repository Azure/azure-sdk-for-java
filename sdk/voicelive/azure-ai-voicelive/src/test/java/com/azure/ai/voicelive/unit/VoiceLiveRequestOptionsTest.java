// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.models.VoiceLiveRequestOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link VoiceLiveRequestOptions} model.
 * <p>
 * Tests that exercise how the client applies these options (via the package-private
 * {@code VoiceLiveAsyncClient} constructor and the private {@code convertToWebSocketEndpoint} method) remain in
 * {@code com.azure.ai.voicelive.unit.VoiceLiveRequestOptionsClientTest}.
 */
class VoiceLiveRequestOptionsTest {
    private static final HttpHeaderName X_CUSTOM_HEADER = HttpHeaderName.fromString("X-Custom-Header");
    private static final HttpHeaderName X_ANOTHER_HEADER = HttpHeaderName.fromString("X-Another-Header");

    @Test
    void testRequestOptionsWithCustomQueryParameters() {
        // Arrange & Act
        VoiceLiveRequestOptions options
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment")
                .addCustomQueryParameter("region", "eastus");

        // Assert
        assertNotNull(options.getCustomQueryParameters());
        assertEquals(2, options.getCustomQueryParameters().size());
        assertEquals("test-deployment", options.getCustomQueryParameters().get("deployment-id"));
        assertEquals("eastus", options.getCustomQueryParameters().get("region"));
    }

    @Test
    void testRequestOptionsWithCustomHeaders() {
        // Arrange & Act
        VoiceLiveRequestOptions options
            = new VoiceLiveRequestOptions().addCustomHeader("X-Custom-Header", "custom-value")
                .addCustomHeader("X-Another-Header", "another-value");

        // Assert
        assertNotNull(options.getCustomHeaders());
        assertEquals("custom-value", options.getCustomHeaders().getValue(X_CUSTOM_HEADER));
        assertEquals("another-value", options.getCustomHeaders().getValue(X_ANOTHER_HEADER));
    }

    @Test
    void testRequestOptionsFluentApi() {
        // Arrange & Act
        VoiceLiveRequestOptions options = new VoiceLiveRequestOptions();

        VoiceLiveRequestOptions result1 = options.addCustomQueryParameter("key1", "value1");
        VoiceLiveRequestOptions result2 = options.addCustomHeader("Header1", "value1");

        // Assert
        assertSame(options, result1);
        assertSame(options, result2);
    }

    @Test
    void testRequestOptionsSetCustomHeaders() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_CUSTOM_HEADER, "value1");
        headers.set(X_ANOTHER_HEADER, "value2");

        // Act
        VoiceLiveRequestOptions options = new VoiceLiveRequestOptions().setCustomHeaders(headers);

        // Assert
        assertNotNull(options.getCustomHeaders());
        assertEquals("value1", options.getCustomHeaders().getValue(X_CUSTOM_HEADER));
        assertEquals("value2", options.getCustomHeaders().getValue(X_ANOTHER_HEADER));
    }
}
