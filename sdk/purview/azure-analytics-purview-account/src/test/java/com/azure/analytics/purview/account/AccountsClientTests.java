package com.azure.analytics.purview.account;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;

public class AccountsClientTests extends PurviewAccountClientTestBase {
    private AccountsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewAccountClientBuilder()
            .host(getEndpoint())
            .pipeline(httpPipeline)
            .buildAccountsClient());
    }

    @Disabled
    public void testGetAccount() {
        BinaryData response = client.get(null);
    }
}
