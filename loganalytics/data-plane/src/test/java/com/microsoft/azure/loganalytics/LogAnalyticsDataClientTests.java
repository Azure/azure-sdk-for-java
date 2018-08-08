package com.microsoft.azure.loganalytics;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.loganalytics.implementation.LogAnalyticsDataClientImpl;
import com.microsoft.azure.loganalytics.models.QueryBody;
import com.microsoft.azure.loganalytics.models.QueryResults;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class LogAnalyticsDataClientTests {
    protected static LogAnalyticsDataClientImpl logAnalyticsClient;
    
    public void initialize() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials("997631f8-3a55-4bb2-81b2-c0972b222260", "microsoft.onmicrosoft.com", "Fmvuhf5imWGlA5zQ5jZNmelm5cpcVP85k8ja7BMPgzA=", null);
        logAnalyticsClient = new LogAnalyticsDataClientImpl(credentials);
    }

    @Test
    public void firstTest() {
        this.initialize();
        String query = "Heartbeat | take 1";
        String workspaceId = "cab864ad-d0c1-496b-bc5e-4418315621bf";
        QueryResults queryResults = logAnalyticsClient.query(workspaceId, new QueryBody().withQuery(query));
        Assert.assertNotNull(queryResults);
        
        // Query should return a single table with one row
        Assert.assertEquals(queryResults.tables().size(), 1);

        // Check type behavior on results
        Assert.assertTrue(queryResults.tables().get(0).rows().get(0).get(16) instanceof Double);
        Assert.assertNull(queryResults.tables().get(0).rows().get(0).get(15));
        
    }
}