package com.microsoft.windowsazure.management;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.LocationsListResponse;

public class LocationOperationsTest  extends ManagementIntegrationTestBase { 
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void listLocationSuccess() throws Exception {
        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        Assert.assertEquals(200, locationsListResponse.getStatusCode());
        Assert.assertNotNull(locationsListResponse.getRequestId());
        Assert.assertTrue(locationsListResponse.getLocations().size() > 0);
    }
}