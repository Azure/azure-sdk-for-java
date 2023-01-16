// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.KeyIdentifier;

public class ObjectIdentifierTest {

    /**
     * Tests if invalid URIs throw an exception
     * and that the exception carries the underlying
     * error message
     */
    @Test
    public void testInvalidURIErrorMessage() throws Exception {
        String invalidVaultURI = "https://with^caret.vault.azure.net";
        String errorMessage = null;

        // Extract actual URISyntaxException error message
        try {
            new URI(invalidVaultURI);
            Assert.fail(
                String.format(
                    "Expected URI '%s' to fail with URISyntaxException",
                    invalidVaultURI
                )
            );
        } catch (URISyntaxException uriEx) {
            errorMessage = uriEx.getMessage();
        }

        // Validate URI through an Identifier class and verify error message
        // contents
        try {
            KeyIdentifier keyIdentifier = new KeyIdentifier(
                invalidVaultURI,
                "key-name",
                "1.0"
            );
            Assert.fail(
                String.format(
                    "Expected an error when passing an invalid URI: '%s'",
                    invalidVaultURI
                )
            );
        } catch (InvalidParameterException ipEx) {
            Assert.assertTrue(
                "Did not find 'Not a valid URI' in the error message",
                ipEx.getMessage().contains("Not a valid URI")
            );
            Assert.assertTrue(
                "Did not find the original URISyntaxException message",
                ipEx.getMessage().contains(errorMessage)
            );
            Assert.assertTrue(
                "Did not find the passed URI included in the error message",
                ipEx.getMessage().contains(invalidVaultURI)
            );
        }
    }
}
