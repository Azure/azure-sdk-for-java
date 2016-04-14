package com.microsoft.azure;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AzureTests {
    private static final ServiceClientCredentials credentials = new ApplicationTokenCredentials(
            System.getenv("client-id"),
            System.getenv("domain"),
            System.getenv("secret"),
            AzureEnvironment.AZURE);
    private static final String subscriptionId = System.getenv("subscription-id");

    private Subscriptions subscriptions;
    private ResourceGroups resourceGroups;
    private StorageAccounts storageAccounts;

    @Before
    public void setup() throws Exception {
        Azure.Authenticated azure = Azure
                .authenticate(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .withUserAgent("AzureTests");
        subscriptions = azure.subscriptions();
        Azure.Subscription sub = azure.withSubscription(subscriptionId);
        resourceGroups = sub.resourceGroups();
        storageAccounts = sub.storageAccounts();
    }

    @Test
    public void listSubscriptions() throws Exception {
        Assert.assertTrue(0 < subscriptions.list().size());
    }

    @Test
    public void listResourceGroups() throws Exception {
        Assert.assertTrue(0 < resourceGroups.list().size());
    }

    @Test
    public void listStorageAccounts() throws Exception {
        Assert.assertTrue(0 < storageAccounts.list().size());
    }
}
