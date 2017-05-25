/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public abstract class TestBase extends MockIntegrationTestBase {
    private PrintStream out;

    public static String generateRandomResourceName(String prefix, int maxLen) {
        return SdkContext.randomResourceName(prefix, maxLen);
    }

    @Before
    public void setup() throws Exception {
        addTextReplacementRule("https://management.azure.com/", this.mockUri() + "/");
        setupTest(name.getMethodName());
        ApplicationTokenCredentials credentials;
        RestClient restClient;
        String defaultSubscription;

        if (IS_MOCKED) {
            credentials = new AzureTestCredentials();
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(this.mockUri() + "/")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withNetworkInterceptor(this.interceptor()), true);

            defaultSubscription = MOCK_SUBSCRIPTION;
            System.out.println(this.mockUri());
            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            }));
        }
        else {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            credentials = ApplicationTokenCredentials.fromFile(credFile);
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withReadTimeout(3, TimeUnit.MINUTES)
                    .withNetworkInterceptor(this.interceptor()), false);

            defaultSubscription = credentials.defaultSubscriptionId();
            addTextReplacementRule(defaultSubscription, MOCK_SUBSCRIPTION);
        }
        initializeClients(restClient, defaultSubscription, credentials.domain());
    }

    @After
    public void cleanup() throws Exception {
        cleanUpResources();
        if (IS_MOCKED) {
            if (testRecord.networkCallRecords.size() > 0) {
                System.out.println("Remaining records " + testRecord.networkCallRecords.size() + " :");
                for (int index = 0; index < testRecord.networkCallRecords.size(); index++) {
                    NetworkCallRecord record = testRecord.networkCallRecords.get(index);
                    System.out.println(record.Method + " - " + record.Uri);
                }
                Assert.assertEquals(0, testRecord.networkCallRecords.size());
            }
            System.setOut(out);
        }
        resetTest(name.getMethodName());
    }

    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        return builder.build();
    }

    protected abstract void initializeClients(RestClient restClient, String defaultSubscription, String domain);
    protected abstract void cleanUpResources();
}
