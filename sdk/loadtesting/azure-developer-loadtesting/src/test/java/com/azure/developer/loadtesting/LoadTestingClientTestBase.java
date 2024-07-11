// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher.TestProxyRequestMatcherType;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LoadTestingClientTestBase extends TestProxyTestBase {
    private static final String URL_REGEX = "(?<=http:\\/\\/|https:\\/\\/)([^\\/?]+)";
    private final String defaultEndpoint = "REDACTED.eastus.cnt-prod.loadtesting.azure.com";

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final String existingTestId = Configuration.getGlobalConfiguration().get("EXISTING_TEST_ID",
            "11111111-1234-1234-1234-123456789012");
    protected final String newTestId = Configuration.getGlobalConfiguration().get("NEW_TEST_ID",
            "22222222-1234-1234-1234-123456789012");
    protected final String newTestIdAsync = Configuration.getGlobalConfiguration().get("NEW_TEST_ID",
            "22223333-1234-1234-1234-123456789012");
    protected final String newTestRunId = Configuration.getGlobalConfiguration().get("NEW_TEST_RUN_ID",
            "33333333-1234-1234-1234-123456789012");
    protected final String newTestRunIdAsync = Configuration.getGlobalConfiguration().get("NEW_TEST_RUN_ID_2",
            "44444444-1234-1234-1234-123456789012");
    protected final String uploadJmxFileName = Configuration.getGlobalConfiguration().get("UPLOAD_JMX_FILE_NAME",
            "sample-JMX-file.jmx");
    protected final String uploadCsvFileName = Configuration.getGlobalConfiguration().get("UPLOAD_CSV_FILE_NAME",
            "additional-data.csv");
    protected final String defaultAppComponentResourceId = Configuration.getGlobalConfiguration().get(
            "APP_COMPONENT_RESOURCE_ID",
            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource");
    protected final String defaultServerMetricId = Configuration.getGlobalConfiguration().get("SERVER_METRIC_ID",
            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource/providers/microsoft.insights/metricdefinitions/requests/duration");

    @Override
    protected void beforeTest() {
        if (getTestMode() != TestMode.LIVE) {
            List<TestProxySanitizer> sanitizers = new ArrayList<>();
            sanitizers.add(new TestProxySanitizer("Location",
                    "https://[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                    "https://REDACTED", TestProxySanitizerType.HEADER));
            sanitizers.add(new TestProxySanitizer(URL_REGEX, "REDACTED", TestProxySanitizerType.BODY_REGEX));
            interceptorManager.addSanitizers(sanitizers);
            // Remove `operation-location`, `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK2030", "AZSDK3430", "AZSDK3493");
        }

        if (getTestMode() == TestMode.PLAYBACK) {
            List<TestProxyRequestMatcher> matchers = new ArrayList<>();
            matchers.add(new TestProxyRequestMatcher(TestProxyRequestMatcherType.BODILESS));
            interceptorManager.addMatchers(matchers);
        }
    }

    // Helpers

    protected LoadTestAdministrationClient getLoadTestAdministrationClient() {
        return getLoadTestAdministrationClientBuilder(false).buildClient();
    }

    protected LoadTestAdministrationAsyncClient getLoadTestAdministrationAsyncClient() {
        return getLoadTestAdministrationClientBuilder(true).buildAsyncClient();
    }

    protected LoadTestRunClient getLoadTestRunClient() {
        return getLoadTestRunClientBuilder(false).buildClient();
    }

    protected LoadTestRunAsyncClient getLoadTestRunAsyncClient() {
        return getLoadTestRunClientBuilder(true).buildAsyncClient();
    }

    protected Map<String, Object> getAppComponentBodyFromDict() {
        Map<String, Object> appCompMap = new HashMap<String, Object>();
        Map<String, Object> compsMap = new HashMap<String, Object>();
        Map<String, Object> compMap = new HashMap<String, Object>();
        compMap.put("resourceId", defaultAppComponentResourceId);
        compMap.put("resourceType", "microsoft.insights/components");
        compMap.put("resourceName", "appcomponentresource");
        compMap.put("displayName", "Performance_LoadTest_Insights");
        compMap.put("kind", "web");

        compsMap.put(defaultAppComponentResourceId, compMap);
        appCompMap.put("components", compsMap);

        return appCompMap;
    }

    protected Map<String, Object> getServerMetricsBodyFromDict() {
        Map<String, Object> serverMetricsMap = new HashMap<String, Object>();
        Map<String, Object> metricsMap = new HashMap<String, Object>();
        Map<String, Object> metricMap = new HashMap<String, Object>();
        metricMap.put("resourceId", defaultAppComponentResourceId);
        metricMap.put("metricNamespace", "microsoft.insights/components");
        metricMap.put("name", "requests/duration");
        metricMap.put("aggregation", "Average");
        metricMap.put("resourceType", "microsoft.insights/components");

        metricsMap.put(defaultServerMetricId, metricMap);
        serverMetricsMap.put("metrics", metricsMap);

        return serverMetricsMap;
    }

    private TokenCredential getTokenCredential() {
        String authorityHost = Configuration.getGlobalConfiguration().get("AUTHORITY_HOST");

        switch (getTestMode()) {
            case RECORD:
                DefaultAzureCredentialBuilder defaultAzureCredentialBuilder = new DefaultAzureCredentialBuilder();

                if (authorityHost != null && !authorityHost.isEmpty()) {
                    defaultAzureCredentialBuilder.authorityHost(authorityHost);
                }

                return defaultAzureCredentialBuilder.build();
            case LIVE:
                Configuration config = Configuration.getGlobalConfiguration();
                EnvironmentCredentialBuilder environmentCredentialBuilder = new EnvironmentCredentialBuilder();

                if (authorityHost != null && !authorityHost.isEmpty()) {
                    environmentCredentialBuilder.authorityHost(authorityHost);
                }

                ChainedTokenCredentialBuilder chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder()
                    .addLast(environmentCredentialBuilder.build())
                    .addLast(new AzureCliCredentialBuilder().build())
                    .addLast(new AzureDeveloperCliCredentialBuilder().build())
                    .addLast(new AzurePowerShellCredentialBuilder().build());

                String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
                String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
                String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
                String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

                if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                    && !CoreUtils.isNullOrEmpty(clientId)
                    && !CoreUtils.isNullOrEmpty(tenantId)
                    && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                    chainedTokenCredentialBuilder.addLast(new AzurePipelinesCredentialBuilder()
                        .systemAccessToken(systemAccessToken)
                        .clientId(clientId)
                        .tenantId(tenantId)
                        .serviceConnectionId(serviceConnectionId)
                        .build());
                }

                return chainedTokenCredentialBuilder.build();
            default:
                // On PLAYBACK mode
                return new MockTokenCredential();
        }
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
                .assertAsync()
                .build();
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
                .assertSync()
                .build();
    }

    private HttpClient getTestModeHttpClient() {
        HttpClient httpClient;
        if (getTestMode() == TestMode.PLAYBACK) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = HttpClient.createDefault();
        }
        return httpClient;
    }

    private LoadTestAdministrationClientBuilder getLoadTestAdministrationClientBuilder(boolean async) {
        HttpClient httpClient = getTestModeHttpClient();

        if (async) {
            httpClient = buildAsyncAssertingClient(httpClient);
        } else {
            httpClient = buildSyncAssertingClient(httpClient);
        }

        LoadTestAdministrationClientBuilder loadTestAdministrationClientBuilder = new LoadTestAdministrationClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", defaultEndpoint))
                .httpClient(httpClient)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            loadTestAdministrationClientBuilder
                    .credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            loadTestAdministrationClientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getTokenCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            loadTestAdministrationClientBuilder.credential(getTokenCredential());
        }

        return loadTestAdministrationClientBuilder;
    }

    private LoadTestRunClientBuilder getLoadTestRunClientBuilder(boolean async) {
        HttpClient httpClient = getTestModeHttpClient();

        if (async) {
            httpClient = buildAsyncAssertingClient(httpClient);
        } else {
            httpClient = buildSyncAssertingClient(httpClient);
        }

        LoadTestRunClientBuilder loadTestRunClientBuilder = new LoadTestRunClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", defaultEndpoint))
                .httpClient(httpClient)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            loadTestRunClientBuilder
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            loadTestRunClientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getTokenCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            loadTestRunClientBuilder.credential(getTokenCredential());
        }

        return loadTestRunClientBuilder;
    }
}
