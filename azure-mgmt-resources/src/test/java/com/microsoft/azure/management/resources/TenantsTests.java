package com.microsoft.azure.management.resources;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TenantsTests extends TestBase {
    protected static ResourceManager.Authenticated resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canListTenants() throws Exception {
        PagedList<Tenant> tenants = resourceManager.tenants().list();
        Assert.assertTrue(tenants.size() > 0);
    }
}
