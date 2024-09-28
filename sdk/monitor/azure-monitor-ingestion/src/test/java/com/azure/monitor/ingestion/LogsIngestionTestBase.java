// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

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
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base test class for logs ingestion client tests.
 */
public abstract class LogsIngestionTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionTestBase.class);

    protected LogsIngestionClientBuilder clientBuilder;
    protected String dataCollectionEndpoint;
    protected String dataCollectionRuleId;
    protected String streamName;

    @Override
    public void beforeTest() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE", "https://dce.monitor.azure.com");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR_ID", "dcr-01584ffffeac4f7abbd4fbc24aa64130");
        streamName = "Custom-MyTableRawData";

        LogsIngestionClientBuilder clientBuilder = new LogsIngestionClientBuilder()
            .credential(getTestTokenCredential(interceptorManager))
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
            interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher()));
            clientBuilder
                .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy());
        }
        this.clientBuilder = clientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .endpoint(dataCollectionEndpoint);
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

    public static class LogsCountPolicy implements HttpPipelinePolicy {

        private AtomicLong totalLogsCount = new AtomicLong();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            BinaryData bodyAsBinaryData = httpPipelineCallContext.getHttpRequest().getBodyAsBinaryData();
            byte[] requestBytes = unzipRequestBody(bodyAsBinaryData);

            List<Object> logs = JsonSerializerProviders.createInstance(true)
                .deserializeFromBytes(requestBytes, new TypeReference<List<Object>>() { });
            totalLogsCount.addAndGet(logs.size());
            return httpPipelineNextPolicy.process();
        }

        public long getTotalLogsCount() {
            return this.totalLogsCount.get();
        }

    }

    public static class DataValidationPolicy implements HttpPipelinePolicy {

        private final String expectedJson;

        public DataValidationPolicy(List<Object> inputData) {
            this.expectedJson = new String(JsonSerializerProviders.createInstance(true).serializeToBytes(inputData));
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            BinaryData bodyAsBinaryData = httpPipelineCallContext.getHttpRequest().getBodyAsBinaryData();
            String actualJson = new String(unzipRequestBody(bodyAsBinaryData));
            assertEquals(expectedJson, actualJson);
            return httpPipelineNextPolicy.process();
        }
    }

    private static byte[] unzipRequestBody(BinaryData bodyAsBinaryData) {
        try {
            byte[] buffer = new byte[1024];
            GZIPInputStream gZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(bodyAsBinaryData.toBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = gZIPInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            gZIPInputStream.close();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to unzip data");
        }
        return null;
    }

    public static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            Configuration config = Configuration.getGlobalConfiguration();
            String serviceConnectionId  = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

            return new AzurePipelinesCredentialBuilder()
                .systemAccessToken(systemAccessToken)
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .build();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }
}
