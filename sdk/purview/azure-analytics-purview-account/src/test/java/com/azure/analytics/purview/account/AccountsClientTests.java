package com.azure.analytics.purview.account;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

public class AccountsClientTests extends PurviewAccountClientTestBase {
    private AccountsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewAccountClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildAccountsClient());
    }

    @Test
    public void testGetAccount() {
        BinaryData response = client.getWithResponse(null, null).getValue();
        System.out.println(response);
    }
}
