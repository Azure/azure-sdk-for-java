package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TenantsTests {
    protected static ResourceManager.Authenticated resourceManager;

    @BeforeClass
    public static void setup() throws Exception {
        resourceManager = ResourceManager
                .configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .authenticate(
                        new ApplicationTokenCredentials(
                                System.getenv("client-id"),
                                System.getenv("domain"),
                                System.getenv("secret"),
                                null)
                );
    }

    @Test
    public void canListTenants() throws Exception {
        PagedList<Tenant> tenants = resourceManager.tenants().list();
        Assert.assertTrue(tenants.size() > 0);
    }
}
