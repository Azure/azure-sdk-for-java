package com.microsoft.azure.loganalytics;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.loganalytics.implementation.LogAnalyticsDataClientImpl;
import com.microsoft.azure.loganalytics.models.QueryBody;
import com.microsoft.azure.loganalytics.models.QueryResults;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class LogAnalyticsDataClientTests extends TestBase {
    protected static LogAnalyticsDataClientImpl logAnalyticsClient;

    @Override
    protected String baseUri() {
        return AzureEnvironment.AZURE.logAnalyticsEndpoint() + "v1/";
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        logAnalyticsClient = new LogAnalyticsDataClientImpl(restClient);
    }

    @Override
    protected void cleanUpResources() {
    }

    @Test
    public void firstTest() {
        String query = "Heartbeat | take 1";
        String workspaceId = "cab864ad-d0c1-496b-bc5e-4418315621bf";
        QueryResults queryResults = logAnalyticsClient.query(workspaceId, new QueryBody().withQuery(query));
        Assert.assertNotNull(queryResults);

        // Query should return a single table with one row
        Assert.assertEquals(queryResults.tables().size(), 1);
        Assert.assertEquals(queryResults.tables().get(0).rows().size(), 1);

        // Check type behavior on results
        Assert.assertTrue(queryResults.tables().get(0).rows().get(0).get(1) instanceof String);
        Assert.assertTrue(queryResults.tables().get(0).rows().get(0).get(16) instanceof Double);
        Assert.assertNull(queryResults.tables().get(0).rows().get(0).get(15));
    }
}
