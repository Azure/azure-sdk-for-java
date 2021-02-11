// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to get an access token from the Mixed Reality security
 * token service (STS).
 */
public final class GetToken {
    /**
     * Runs the sample and demonstrates to get an access token from the Mixed
     * Reality security token service (STS).
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // You can get your account domain, Id, and key by viewing your Mixed
        // Reality resource in the Azure portal.
        final String accountDomain = "";
        final String accountId = "00000000-0000-0000-0000-000000000000";
        final String accountKey = "";

        AzureKeyCredential keyCredential = new AzureKeyCredential(accountKey);
        MixedRealityStsClient client = new MixedRealityStsClientBuilder()
            .accountDomain(accountDomain)
            .accountId(accountId)
            .credential(keyCredential)
            .buildClient();

        AccessToken token = client.getToken();
    }
}
