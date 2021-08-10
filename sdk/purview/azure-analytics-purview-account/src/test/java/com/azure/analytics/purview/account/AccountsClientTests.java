package com.azure.analytics.purview.account;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

public class AccountsClientTests extends PurviewAccountClientTestBase {
    private AccountsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewAccountClientBuilder()
            .host(getEndpoint())
            .pipeline(httpPipeline)
            .buildAccountsClient());
    }

    @Test
    public void testGetAccount() {
        BinaryData response = client.get(null);
        System.out.println(response);
    }
}
