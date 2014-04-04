package com.microsoft.windowsazure.management;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.RoleSizeListResponse;

public class RoleSizeOperationsTest  extends ManagementIntegrationTestBase { 
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void listRoleSizeSuccess() throws Exception {
        RoleSizeListResponse roleSizeListResponse = managementClient.getRoleSizesOperations().list();

        Assert.assertEquals(200, roleSizeListResponse.getStatusCode());
        Assert.assertNotNull(roleSizeListResponse.getRequestId());
        Assert.assertTrue(roleSizeListResponse.getRoleSizes().size() > 0);
    }
}