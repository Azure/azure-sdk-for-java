// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyVaultEndpointValidationTest {
    @Test
    public void acceptsSameOriginAbsoluteHttpUrlsWithoutChangingContinuation() {
        // URI-level coverage only, not a guarantee of transport support for every host syntax.
        String[][] origins = {
            { "https://vault.azure.net", "HTTPS://VAULT.AZURE.NET:443" },
            { "https://vault.usgovcloudapi.net:443", "https://VAULT.USGOVCLOUDAPI.NET" },
            { "https://vault.azure.cn", "https://vault.azure.cn:443" },
            { "https://vault.managedhsm.azure.net", "https://VAULT.MANAGEDHSM.AZURE.NET:443" },
            { "https://arbitrary.example:8443", "HTTPS://ARBITRARY.EXAMPLE:8443" },
            { "https://localhost", "https://LOCALHOST:443" },
            { "https://127.0.0.1:8443", "https://127.0.0.1:8443" },
            { "https://[2001:db8::a]", "https://[2001:DB8::A]:443" },
            { "https://xn--bcher-kva.example.", "https://XN--BCHER-KVA.EXAMPLE.:443" },
            { "http://localhost", "HTTP://LOCALHOST:80" },
            { "http://localhost:8080", "http://LOCALHOST:8080" } };
        for (String[] origin : origins) {
            String nextLink = origin[1] + "/path%2Fpart/../page?skip=a%2Bb%3D&value=%23&empty=";
            assertEquals(nextLink, KeyVaultEndpointValidation.validateNextLink(nextLink, origin[0] + "/base/"));
            assertEquals(KeyVaultEndpointValidation.getOrigin(origin[0]),
                KeyVaultEndpointValidation.getOrigin(origin[1]));
        }
    }

    @Test
    public void rejectsForeignOrMalformedOrigins() {
        assertThrows(IllegalArgumentException.class,
            () -> KeyVaultEndpointValidation.getOrigin("https://b\u00fccher.example"));
        String endpoint = "https://vault.example";
        String[] continuations = {
            "https://other.example/page",
            "http://vault.example/page",
            "https://vault.example:8443/page",
            "https://vault.example.other.example/page",
            "https://user@vault.example/page",
            "https://vault.example@other.example/page",
            "https://vault.example:invalid/page",
            "https://vault.example:65536/page",
            "https://vault.example:-2/page",
            "https://vault.example/bad path",
            "https://vault.example/page#fragment",
            "https://vault.example/page?bad=%xy",
            "/page",
            "//vault.example/page",
            "https:///page",
            "file://vault.example/page" };
        for (String continuation : continuations) {
            assertThrows(IllegalArgumentException.class,
                () -> KeyVaultEndpointValidation.validateNextLink(continuation, endpoint), continuation);
        }
    }
}
