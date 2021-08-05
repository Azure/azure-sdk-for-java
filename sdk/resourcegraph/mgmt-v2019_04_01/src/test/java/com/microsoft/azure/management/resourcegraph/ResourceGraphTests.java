/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resourcegraph;

import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.QueryRequest;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.QueryRequestOptions;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.QueryResponse;
import com.microsoft.azure.management.resourcegraph.v2019_04_01.ResultFormat;
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
        // assume have some resource in the subscription
        queryRequest.withSubscriptions(Arrays.asList(subscription));
        queryRequest.withQuery("Resources | project name, type | order by name asc | limit 5");
        // table format
        queryRequest.withOptions(new QueryRequestOptions().withResultFormat(ResultFormat.TABLE));
        QueryResponse queryResponse = resourceGraphManager.resourceProviders().resources(queryRequest);

        Assert.assertTrue(queryResponse.count() > 0);
        Assert.assertNotNull(queryResponse.data());
        Assert.assertTrue(queryResponse.data() instanceof Map);
        Map<String, Object> dataAsDict = (Map<String, Object>) queryResponse.data();
        Assert.assertTrue(dataAsDict.containsKey("columns"));
        Assert.assertTrue(dataAsDict.containsKey("rows"));
        List<String> columns = (List<String>) dataAsDict.get("columns");
        List<String> rows = (List<String>) dataAsDict.get("rows");
        Assert.assertEquals(2, columns.size()); // name and type
        Assert.assertTrue(rows.size() > 0);

        // object array format
        queryRequest.withOptions(new QueryRequestOptions().withResultFormat(ResultFormat.OBJECT_ARRAY));

        queryResponse = resourceGraphManager.resourceProviders().resources(queryRequest);
        Assert.assertTrue(queryResponse.count() > 0);
        Assert.assertTrue(queryResponse.data() instanceof List);
        List<Object> dataAsList = (List<Object>) queryResponse.data();
        Map<String, String> itemAsDict = (Map<String, String>) dataAsList.iterator().next();
        Assert.assertTrue(itemAsDict.containsKey("name"));
        Assert.assertTrue(itemAsDict.containsKey("type"));
    }
}
