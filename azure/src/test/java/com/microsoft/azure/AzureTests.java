package com.microsoft.azure;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
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
    private Azure.ResourceGroups resourceGroups;
    private StorageAccounts storageAccounts;

    @Before
    public void setup() throws Exception {
        Azure.Authenticated azure = Azure.configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .withUserAgent("AzureTests")
                .authenticate(credentials);

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

    @Test
    public void createStorageAccount() throws Exception {
        StorageAccount storageAccount = storageAccounts.define("my-stg1")
                .withRegion(Region.ASIA_EAST)
                .withNewGroup()
                .withAccountType(AccountType.PREMIUM_LRS)
                .provision();

        Assert.assertSame(storageAccount.name(), "my-stg1");
    }

    @Test
    public void createStorageAccountInResourceGroupContext() throws Exception {
        StorageAccount storageAccount = resourceGroups.get("my-grp")
                .storageAccounts()
                .define("my-stg2")
                .withAccountType(AccountType.PREMIUM_LRS)
                .provision();

        Assert.assertSame(storageAccount.name(), "my-stg2");
    }
}
