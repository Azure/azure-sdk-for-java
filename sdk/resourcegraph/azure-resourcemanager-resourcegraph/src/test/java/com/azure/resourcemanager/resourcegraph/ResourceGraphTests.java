// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resourcegraph;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.azure.resourcemanager.resourcegraph.models.ResultFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResourceGraphTests extends TestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void queryTest() {
        // requires a Azure Subscription
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        ResourceGraphManager manager = ResourceGraphManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        // @embedmeStart
        QueryRequest queryRequest = new QueryRequest()
            .withSubscriptions(Collections.singletonList(subscriptionId))
            .withQuery("Resources | project name, type | limit 5 | order by name asc");
        // table format
        queryRequest.withOptions(new QueryRequestOptions().withResultFormat(ResultFormat.TABLE));
        QueryResponse response = manager.resourceProviders().resources(queryRequest);

        Assertions.assertNotNull(response.data());
        Assertions.assertTrue(response.data() instanceof Map);

        // object array format
        queryRequest.withOptions(new QueryRequestOptions().withResultFormat(ResultFormat.OBJECT_ARRAY));
        response = manager.resourceProviders().resources(queryRequest);

        Assertions.assertNotNull(response.data());
        Assertions.assertTrue(response.data() instanceof List);
        // @embedmeEnd
    }
}
