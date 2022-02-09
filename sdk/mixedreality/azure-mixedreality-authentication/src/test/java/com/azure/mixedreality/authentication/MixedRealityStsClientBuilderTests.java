// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MixedRealityStsClientBuilderTests {
    private final String accountDomain = "mixedreality.azure.com";
    private final String accountId = "00000000-0000-0000-0000-000000000000";
    private final String accountKey = "00000000-0000-0000-0000-000000000000";

    @Test
    public void buildClient() {

        MixedRealityStsClient client = new MixedRealityStsClientBuilder()
            .accountDomain(this.accountDomain)
            .accountId(this.accountId)
            .credential(new AzureKeyCredential(accountKey))
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void buildClientMissingAccountDomain() {

        MixedRealityStsClientBuilder builder = new MixedRealityStsClientBuilder()
            .accountId(this.accountId)
            .credential(new AzureKeyCredential(accountKey));

        NullPointerException exception = assertThrows(NullPointerException.class, () -> builder.buildClient());

        assertEquals("The 'accountDomain' has not been set and is required.", exception.getMessage());
    }

    @Test
    public void buildClientMissingAccountId() {

        MixedRealityStsClientBuilder builder = new MixedRealityStsClientBuilder()
            .accountDomain(this.accountDomain)
            .credential(new AzureKeyCredential(accountKey));

        NullPointerException exception = assertThrows(NullPointerException.class, () -> builder.buildClient());

        assertEquals("The 'accountId' has not been set and is required.", exception.getMessage());
    }

    @Test
    public void buildClientMissingCredential() {

        MixedRealityStsClientBuilder builder = new MixedRealityStsClientBuilder()
            .accountId(this.accountId)
            .accountDomain(this.accountDomain);

        NullPointerException exception = assertThrows(NullPointerException.class, () -> builder.buildClient());

        assertEquals("The 'credential' has not been set and is required.", exception.getMessage());
    }
}
