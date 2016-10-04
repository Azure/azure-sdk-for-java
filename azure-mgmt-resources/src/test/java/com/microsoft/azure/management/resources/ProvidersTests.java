package com.microsoft.azure.management.resources;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class ProvidersTests {
    protected static ResourceManager resourceManager;

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
                                AzureEnvironment.AZURE)
                ).withSubscription(System.getenv("subscription-id"));
    }

    @Test
    public void canUnregisterAndRegisterProvider() throws Exception {
        List<Provider> providers = resourceManager.providers().list();
        int size = providers.size();
        Assert.assertTrue(size > 0);
        Provider provider = providers.get(0);
        resourceManager.providers().unregister(provider.namespace());
        provider = resourceManager.providers().getByName(provider.namespace());
        while (provider.registrationState().equals("Unregistering")) {
            Thread.sleep(5000);
            provider = resourceManager.providers().getByName(provider.namespace());
        }
        resourceManager.providers().register(provider.namespace());
        while (provider.registrationState().equals("Unregistered")) {
            Thread.sleep(5000);
            provider = resourceManager.providers().getByName(provider.namespace());
        }
        Assert.assertEquals("Registered", provider.registrationState());
        List<ProviderResourceType> resourceTypes = provider.resourceTypes();
        Assert.assertTrue(resourceTypes.size() > 0);
    }
}
