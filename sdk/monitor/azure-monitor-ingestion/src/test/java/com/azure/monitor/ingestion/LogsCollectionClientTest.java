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
    private String dataCollectionRuleId;
    private String streamName;
    private String dataCollectionEndpoint;

    @BeforeEach
    public void setup() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_ENDPOINT");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_RULE_ID");
        streamName = Configuration.getGlobalConfiguration().get("DATA_COLLECTION_STREAM_NAME");

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
        List<Object> dataList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            dataList.add(logData);
        }
        LogsIngestionClient client = clientBuilder.buildClient();
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, dataList);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            dataList.add(logData);
        }

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
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, dataList);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            dataList.add(logData);
        }

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

        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, dataList);
        assertEquals(UploadLogsStatus.PARTIAL_FAILURE, result.getStatus());
        assertEquals(11, count.get());
        assertEquals(5, result.getErrors().size());
        result.getErrors().stream().forEach(error -> assertEquals("NotFound", error.getResponseError().getCode()));
    }
}
