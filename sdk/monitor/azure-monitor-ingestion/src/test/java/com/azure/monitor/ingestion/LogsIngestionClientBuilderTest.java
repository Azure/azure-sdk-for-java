// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LogsIngestionClientBuilder}.
 */
public class LogsIngestionClientBuilderTest {

    @Test
    public void testBuilderWithoutEndpoint() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
                () -> new LogsIngestionClientBuilder().buildClient());
        Assertions.assertEquals("endpoint is required to build the client.", ex.getMessage());
    }

    @Test
    public void testBuilderWithoutCredential() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
                () -> new LogsIngestionClientBuilder()
                .endpoint("https://example.com")
                .buildClient());
        Assertions.assertEquals("credential is required to build the client.", ex.getMessage());
    }

    @Test
    public void testBuilderWithInvalidEndpoint() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LogsIngestionClientBuilder()
                        .endpoint("example.com")
                        .buildClient());
        Assertions.assertEquals("'endpoint' must be a valid URL.", ex.getMessage());
    }

}
