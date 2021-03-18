/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resourcegraph;

import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.QueryRequest;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.QueryResponse;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.implementation.ResourceGraphManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResourceGraphTests extends TestBase {
    private ResourceGraphManager resourceGraphManager;
    private String subscription;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException {
        resourceGraphManager = ResourceGraphManager
            .authenticate(restClient);
        subscription = defaultSubscription;
    }

    @Override
    protected void cleanUpResources() {
    }

    @Test
    @Ignore
    public void testResourceGraph() {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.withSubscriptions(Arrays.asList(subscription));
        queryRequest.withQuery("Resources | project name, type | order by name asc | limit 5");

        QueryResponse queryResponse = resourceGraphManager.resourceProviders().resources(queryRequest);

        // assume have some resource
        Assert.assertTrue(queryResponse.count() > 0);
        Assert.assertNotNull(queryResponse.data());
        Assert.assertTrue(queryResponse.data() instanceof Map);
        Map<String, Object> dataAsDict = (Map<String, Object>) queryResponse.data();
        Assert.assertTrue(dataAsDict.containsKey("columns"));
        Assert.assertTrue(dataAsDict.containsKey("rows"));
        List<String> columns = (List<String>) dataAsDict.get("columns");
        List<String> rows = (List<String>) dataAsDict.get("columns");
        Assert.assertEquals(2, columns.size()); // name and type
        Assert.assertTrue(rows.size() > 0);
    }
}
