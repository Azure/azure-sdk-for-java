// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.azure.core.util.Configuration;
import com.azure.security.attestation.AttestationClient;

import org.junit.jupiter.api.Test;

public class AttestationTest extends TestBase {
    private final String isolatedEndpoint = Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL");
    private final String aadEndpoint = Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL");

    @Test
    public void testClassCreation() {
        var client = new AttestationClient()
        assertEquals("hello", (new Hello()).getMessage());
    }
}

