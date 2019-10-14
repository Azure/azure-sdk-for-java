// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.test.environment.setup.AzureSearchResources;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class SearchServiceTestBase extends TestBase {

    protected final ClientLogger logger = new ClientLogger(SearchIndexClientTestBase.class);

    protected String searchServiceName;
    protected ApiKeyCredentials apiKeyCredentials;
    protected SearchIndexService searchServiceHotelsIndex;

    private static AzureSearchResources azureSearchResources;

    @Override
    public String getTestName() {
        return testName.getMethodName();
    }

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() {
        initializeAzureResources();
    }

    @AfterClass
    public static void afterClass() {
        azureSearchResources.deleteResourceGroup();
    }

    @Override
    protected void beforeTest() {
        if (!interceptorManager.isPlaybackMode()) {
            azureSearchResources.initialize();
            azureSearchResources.createResourceGroup();
            azureSearchResources.createService();

            searchServiceName = azureSearchResources.getSearchServiceName();
            apiKeyCredentials = new ApiKeyCredentials(azureSearchResources.getSearchAdminKey());
        }
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        azureSearchResources.deleteService();
    }

    private static void initializeAzureResources() {
        String appId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String azureDomainId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String secret = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            appId,
            azureDomainId,
            secret,
            AzureEnvironment.AZURE);

        azureSearchResources = new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_EAST);
    }
}
