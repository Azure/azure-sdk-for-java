package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NetworkUsageOperationsTests extends NetworkManagementTest {
    @Test
    public void canListNetworkUsages() throws Exception {
        List<NetworkUsage> usages = networkManager.usages().listByRegion(Region.US_EAST);
        Assert.assertTrue(usages.size() > 0);
    }

    @Override
    protected void cleanUpResources() {
    }
}
