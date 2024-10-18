// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Mono;

class LoadTestingClientTestBase extends TestBase {
    protected LoadTestAdministrationClientBuilder adminBuilder;
    protected LoadTestRunClientBuilder testRunBuilder;

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String defaultEndpoint = "REDACTED.eus.cnt-prod.loadtesting.azure.com";

    protected final String existingTestId = Configuration.getGlobalConfiguration().get("EXISTING_TEST_ID", "11111111-1234-1234-1234-123456789012");
    protected final String newTestId = Configuration.getGlobalConfiguration().get("NEW_TEST_ID", "22222222-1234-1234-1234-123456789012");
    protected final String newTestRunId = Configuration.getGlobalConfiguration().get("NEW_TEST_RUN_ID", "33333333-1234-1234-1234-123456789012");
    protected final String newTestRunId2 = Configuration.getGlobalConfiguration().get("NEW_TEST_RUN_ID_2", "44444444-1234-1234-1234-123456789012");
    protected final String uploadJmxFileName = Configuration.getGlobalConfiguration().get("UPLOAD_JMX_FILE_NAME", "sample-JMX-file.jmx");
    protected final String uploadCsvFileName = Configuration.getGlobalConfiguration().get("UPLOAD_CSV_FILE_NAME", "additional-data.csv");
    protected final String defaultAppComponentResourceId = Configuration.getGlobalConfiguration().get("APP_COMPONENT_RESOURCE_ID", "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource");
    protected final String defaultServerMetricId = Configuration.getGlobalConfiguration().get("SERVER_METRIC_ID", "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource/providers/microsoft.insights/metricdefinitions/requests/duration");

    private TokenCredential getTokenCredential() {
        DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
        String authorityHost = Configuration.getGlobalConfiguration().get("AUTHORITY_HOST");
        if (authorityHost != null) {
            credentialBuilder.authorityHost(authorityHost);
        }
        return credentialBuilder.build();
    }

    @Override
    protected void beforeTest() {
        LoadTestAdministrationClientBuilder loadTestAdministrationClientBuilder =
                new LoadTestAdministrationClientBuilder()
                        .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", defaultEndpoint))
                        .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        LoadTestRunClientBuilder loadTestRunClientBuilder =
                new LoadTestRunClientBuilder()
                        .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", defaultEndpoint))
                        .httpClient(HttpClient.createDefault())
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            loadTestAdministrationClientBuilder
                    .httpClient(interceptorManager.getPlaybackClient())
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
            loadTestRunClientBuilder
                    .httpClient(interceptorManager.getPlaybackClient())
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            loadTestAdministrationClientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getTokenCredential());
            loadTestRunClientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getTokenCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            loadTestAdministrationClientBuilder.credential(getTokenCredential());
            loadTestRunClientBuilder.credential(getTokenCredential());
        }

        adminBuilder = loadTestAdministrationClientBuilder;
        testRunBuilder = loadTestRunClientBuilder;
    }

    // Helpers

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
}
