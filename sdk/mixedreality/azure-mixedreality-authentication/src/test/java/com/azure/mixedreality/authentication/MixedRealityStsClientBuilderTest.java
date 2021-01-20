// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class MixedRealityStsClientBuilderTest {
    private final String accountDomain = "mixedreality.azure.com";
    private final UUID accountId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final String accountKey = "00000000-0000-0000-0000-000000000000";

    @Test
    public void testClient() {

        MixedRealityStsClient client = new MixedRealityStsClientBuilder()
            .accountDomain(this.accountDomain)
            .accountId(this.accountId)
            .credential(new AzureKeyCredential(accountKey))
            .buildClient();

        assertNotNull(client);
    }
}
