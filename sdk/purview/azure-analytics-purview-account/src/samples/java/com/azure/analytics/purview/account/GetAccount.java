package com.azure.analytics.purview.account;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetAccount {
    public static void main(String[] args) {
        AccountsClient client = new PurviewAccountClientBuilder()
            .endpoint(System.getenv("ACCOUNT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAccountsClient();
        BinaryData response = client.getWithResponse(null, null).getValue();
        System.out.println(response);
    }
}
