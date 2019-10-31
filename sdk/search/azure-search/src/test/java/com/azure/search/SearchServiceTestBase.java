// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.implementation.serializer.jsonwrapper.JsonWrapper;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.search.models.AccessCondition;
import com.azure.search.test.environment.setup.AzureSearchResources;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.Locale;

public abstract class SearchServiceTestBase extends TestBase {

    private static final String DEFAULT_DNS_SUFFIX = "search.windows.net";
    private static final String DOGFOOD_DNS_SUFFIX = "search-dogfood.windows-int.net";

    private String searchServiceName;
    private String searchDnsSuffix;
    protected String endpoint;
    protected ApiKeyCredentials apiKeyCredentials;
    protected SearchIndexService searchServiceHotelsIndex;

    private static String testEnvironment;
    private static AzureSearchResources azureSearchResources;

    private JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

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
        searchDnsSuffix = testEnvironment.equals("DOGFOOD") ? DOGFOOD_DNS_SUFFIX : DEFAULT_DNS_SUFFIX;
        if (!interceptorManager.isPlaybackMode()) {
            azureSearchResources.initialize();
            azureSearchResources.createResourceGroup();
            azureSearchResources.createService();

            searchServiceName = azureSearchResources.getSearchServiceName();
            apiKeyCredentials = new ApiKeyCredentials(azureSearchResources.getSearchAdminKey());
        }
        endpoint = String.format("https://%s.%s", searchServiceName, searchDnsSuffix);
        jsonApi.configureTimezone();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        azureSearchResources.deleteService();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    private static void initializeAzureResources() {
        String appId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String azureDomainId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String secret = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        testEnvironment = Configuration.getGlobalConfiguration().get("AZURE_TEST_ENVIRONMENT");
        if (testEnvironment == null) {
            testEnvironment = "AZURE";
        } else {
            testEnvironment = testEnvironment.toUpperCase(Locale.US);
        }

        AzureEnvironment environment = testEnvironment.equals("DOGFOOD") ? getDogfoodEnvironment() : AzureEnvironment.AZURE;

        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            appId,
            azureDomainId,
            secret,
            environment);

        azureSearchResources = new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_EAST);
    }

    private static AzureEnvironment getDogfoodEnvironment() {
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("portalUrl", "http://df.onecloud.azure-test.net");
        configuration.put("managementEndpointUrl", "https://management.core.windows.net/");
        configuration.put("resourceManagerEndpointUrl", "https://api-dogfood.resources.windows-int.net/");
        configuration.put("activeDirectoryEndpointUrl", "https://login.windows-ppe.net/");
        configuration.put("activeDirectoryResourceId", "https://management.core.windows.net/");
        configuration.put("activeDirectoryGraphResourceId", "https://graph.ppe.windows.net/");
        configuration.put("activeDirectoryGraphApiVersion", "2013-04-05");
        return new AzureEnvironment(configuration);
    }

    /**
     * If the document schema is known, user can convert the properties to a specific object type
     * @param cls Class type of the document object to convert to
     * @param <T> type
     * @return an object of the request type
     */
    protected  <T> T convertToType(Object document, Class<T> cls) {
        return jsonApi.convertObjectToType(document, cls);
    }


    /**
     * Constructs an access condition such that an operation will be performed only if the resource's current ETag
     * value matches the specified ETag value.
     * @param eTag the ETag value to check against the resource's ETag
     * @return An AccessCondition object that represents the If-Match condition
     */
    protected AccessCondition generateIfMatchAccessCondition(String eTag) {
        return new AccessCondition().setIfMatch(eTag);
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource does not exist.
     * @return an AccessCondition object that represents a condition where a resource does not exist
     */
    protected AccessCondition generateIfNotExistsAccessCondition() {
        // Setting this access condition modifies the request to include the HTTP If-None-Match conditional header set to "*"
        return new AccessCondition().setIfNoneMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource exists.
     * @return an AccessCondition object that represents a condition where a resource exists
     */
    protected AccessCondition generateIfExistsAccessCondition() {
        // Setting this access condition modifies the request to include the HTTP If-Match conditional header set to "*"
        return new AccessCondition().setIfMatch("*");
    }
}
