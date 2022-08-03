// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsResult;
import com.azure.monitor.ingestion.models.UploadLogsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for {@link LogsIngestionClient}.
 */
public class LogsCollectionClientTest extends TestBase {
    private LogsIngestionClientBuilder clientBuilder;
    private String dataCollectionEndpoint;
    private String dataCollectionRuleId;
    private String streamName;

    @BeforeEach
    public void setup() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE", "https://dce.monitor.azure.com");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR_ID", "dcr-adec84661d05465f8532f32a04af6f98");
        streamName = "Custom-MyTableRawData";

        LogsIngestionClientBuilder clientBuilder = new LogsIngestionClientBuilder()
                .retryPolicy(new RetryPolicy(new RetryStrategy() {
                    @Override
                    public int getMaxRetries() {
                        return 0;
                    }

                    @Override
                    public Duration calculateRetryDelay(int i) {
                        return null;
                    }
                }));
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                    .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                    .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(getCredential());
        }
        this.clientBuilder = clientBuilder
                .endpoint(dataCollectionEndpoint);
    }

    private TokenCredential getCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                .clientSecret(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET))
                .tenantId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID))
                .build();
    }

    @Test
    public void testUploadLogs() {
        List<Object> logs = getObjects(10);
        LogsIngestionClient client = clientBuilder.buildClient();
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionClient client = clientBuilder
                .addPolicy(new HttpPipelinePolicy() {
                    @Override
                    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                        count.incrementAndGet();
                        return next.process();
                    }

                    @Override
                    public HttpPipelinePosition getPipelinePosition() {
                        return HttpPipelinePosition.PER_CALL;
                    }
                })
                .buildClient();
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> logs = getObjects(100000);
        AtomicBoolean changeDcrId = new AtomicBoolean();
        AtomicInteger count = new AtomicInteger();

        LogsIngestionClient client = clientBuilder
                .addPolicy(new HttpPipelinePolicy() {
                    @Override
                    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                        count.incrementAndGet();
                        if (changeDcrId.get()) {
                            String url = context.getHttpRequest().getUrl().toString()
                                    .replace(dataCollectionRuleId, "dcr-id");
                            context.getHttpRequest().setUrl(url);
                            changeDcrId.set(false);
                            return next.process();
                        }
                        changeDcrId.set(true);
                        return next.process();
                    }

                    @Override
                    public HttpPipelinePosition getPipelinePosition() {
                        return HttpPipelinePosition.PER_CALL;
                    }
                })
                .buildClient();

        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.PARTIAL_FAILURE, result.getStatus());
        assertEquals(11, count.get());
        assertEquals(5, result.getErrors().size());
        result.getErrors().stream().forEach(error -> assertEquals("NotFound", error.getResponseError().getCode()));
    }

    private List<Object> getObjects(int logsCount) {
        List<Object> logs = new ArrayList<>();

        for (int i = 0; i < logsCount; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.parse("2022-01-01T00:00:00+07:00"))
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            logs.add(logData);
        }
        return logs;
    }
}
