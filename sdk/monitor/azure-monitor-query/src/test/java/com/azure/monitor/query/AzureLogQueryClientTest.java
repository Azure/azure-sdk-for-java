// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import org.junit.jupiter.api.Test;

public class AzureLogQueryClientTest {

    @Test
    public void testQueryLogs() {
        System.out.println("Test 2");
        AzureLogQueryClient azureLogQueryClient = new AzureLogQueryClientBuilder()
            .credential(new ClientSecretCredentialBuilder().clientSecret(".4k6P7UktCb_LXzED09Z~i-0-Z.5SGKaf3").tenantId("72f988bf-86f1-41af-91ab-2d7cd011db47").clientId("ed5f0b46-af44-485c-aea3-b80a558b91d4").build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        QueryResults queryResults = azureLogQueryClient.queryLogs("d2d0e126-fa1e-4b0a-b647-250cdd471e68",
            "AzureActivity | take 1");

        queryResults.getTables().stream()
            .map(table -> table.getRows())
            .forEach(row -> row.stream().forEach(val -> System.out.println(val)));
        System.out.println("Done " + queryResults);
    }
}
