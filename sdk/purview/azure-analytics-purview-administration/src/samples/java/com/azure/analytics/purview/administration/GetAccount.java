// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetAccount {
    public static void main(String[] args) {
        AccountsClient client = new AccountsClientBuilder()
            .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        BinaryData response = client.getAccountPropertiesWithResponse(null).getValue();
        System.out.println(response);
    }
}
