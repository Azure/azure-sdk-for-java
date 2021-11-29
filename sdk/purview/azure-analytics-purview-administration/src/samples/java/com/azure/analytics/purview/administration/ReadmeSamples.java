// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.BinaryData;

/**
 * Samples to include in README.md
 */
public class ReadmeSamples {
    /**
     * Sample to demonstrate creating Purview Accounts client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createAccountsClient
        AccountsClient client = new PurviewAccountClientBuilder()
            .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAccountsClient();
        // END: readme-sample-createAccountsClient
    }

    /**
     * Sample to demonstrate getting the properties of an account.
     */
    public void getAccounts() {
        // BEGIN: readme-sample-getAccountProperties
        AccountsClient client = new PurviewAccountClientBuilder()
            .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAccountsClient();
        BinaryData response = client.getAccountPropertiesWithResponse(null).getValue();
        // END: readme-sample-getAccountProperties
    }
}
