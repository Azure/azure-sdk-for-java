// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base test class for logs ingestion client tests.
 */
public abstract class LogsIngestionTestBase extends TestBase {
    protected LogsIngestionClientBuilder clientBuilder;
    protected String dataCollectionEndpoint;
    protected String dataCollectionRuleId;
    protected String streamName;

    @BeforeEach
    public void setup() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE", "https://dce.monitor.azure.com");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR_ID", "dcr-a64851bc17714f0483d1e96b5d84953b");
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
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .endpoint(dataCollectionEndpoint);
    }

    private TokenCredential getCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                .clientSecret(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET))
                .tenantId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID))
                .build();
    }

    public class BatchCountPolicy implements HttpPipelinePolicy {
        private final AtomicInteger counter;

        BatchCountPolicy(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            counter.incrementAndGet();
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            counter.incrementAndGet();
            return next.processSync();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    public class PartialFailurePolicy implements HttpPipelinePolicy {
        private final AtomicInteger counter;
        private final AtomicBoolean changeDcrId = new AtomicBoolean();

        PartialFailurePolicy(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            process(context);
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            process(context);
            return next.processSync();
        }

        private void process(HttpPipelineCallContext context) {
            counter.incrementAndGet();
            if (changeDcrId.get()) {
                String url = context.getHttpRequest().getUrl().toString()
                        .replace(dataCollectionRuleId, "dcr-id");
                context.getHttpRequest().setUrl(url);
                changeDcrId.set(false);
            } else {
                changeDcrId.set(true);
            }
        }
        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    public static List<Object> getObjects(int logsCount) {
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
