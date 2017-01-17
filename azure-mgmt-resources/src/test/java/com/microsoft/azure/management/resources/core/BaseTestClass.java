/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.LogLevel;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public abstract class BaseTestClass extends MockIntegrationTestBase {

    @Before
    public void setup() throws Exception {
        addRegexRule("https://management.azure.com", MOCK_URI);
        setupTest(name.getMethodName());
        final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
        ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);
        RestClient restClient;
        String defaultSubscription;

        if (IS_MOCKED) {
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(MOCK_URI + "/")
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withNetworkInterceptor(interceptor), true);

            defaultSubscription = MOCK_SUBSCRIPTION;
        }
        else {
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withNetworkInterceptor(interceptor), false);

            defaultSubscription = credentials.defaultSubscriptionId();
            addRegexRule(defaultSubscription, MOCK_SUBSCRIPTION);
        }
        initializeClients(restClient, defaultSubscription);
    }

    @After
    public void cleanup() throws Exception {
        cleanUpResources();
        resetTest(name.getMethodName());
    }

    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        return builder.build();
    }

    protected abstract void initializeClients(RestClient restClient, String defaultSubscription);
    protected abstract void cleanUpResources();
}
