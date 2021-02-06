// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;

public final class GetToken {
    public static void main(String[] args) {
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
