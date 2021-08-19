// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.account;

import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {
    public static void main(String[] args) {
        AccountsClient client = new PurviewAccountClientBuilder()
            .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAccountsClient();
    }
}
