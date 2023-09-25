// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

public class AccountsClientTests extends PurviewAccountClientTestBase {
    private AccountsClient client;

    @Override
    protected void beforeTest() {
        client = purviewAccountClientBuilderSetUp().buildClient();
    }

    @Test
    public void testGetAccount() {
        BinaryData response = client.getAccountPropertiesWithResponse(null).getValue();
        System.out.println(response);
    }
}
