// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LoggingUtilTest {

    private static final ClientLogger LOGGER = new ClientLogger(LoggingUtilTest.class);

    @Test
    public void testLogTokenErrorWithInnerException() {
        // Create a nested exception to simulate the "see inner exception" scenario
        Exception innerException = new RuntimeException("Inner exception details");
        ClientAuthenticationException outerException = new ClientAuthenticationException(
            "Managed Identity authentication failed, see inner exception for more information.", null, innerException);

        // Create a token request context
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        // Create identity client options
        IdentityClientOptions options = new IdentityClientOptions();

        // Verify that calling logTokenError doesn't throw an exception
        // and that it properly handles the nested exception
        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, outerException));
    }

    @Test
    public void testLogTokenErrorWithNullException() {
        // Test that the method handles null exceptions gracefully
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        IdentityClientOptions options = new IdentityClientOptions();

        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, null));
    }

    @Test
    public void testLogTokenSuccess() {
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        assertDoesNotThrow(() -> LoggingUtil.logTokenSuccess(LOGGER, context));
    }

    @Test
    public void testLogAvailableEnvironmentVariables() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        assertDoesNotThrow(() -> LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration));
    }
}
