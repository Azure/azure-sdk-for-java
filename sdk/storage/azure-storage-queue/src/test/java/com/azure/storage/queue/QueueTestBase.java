// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Context;
import com.azure.core.util.ServiceVersion;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.common.test.shared.policy.NoOpHttpPipelinePolicy;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import okhttp3.ConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.CRC32;

/**
 * Base class for Azure Storage Queue tests.
 */
public class QueueTestBase extends TestProxyTestBase {
    private static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    private String prefix;

    protected TestDataFactory getData() {
        return TestDataFactory.getInstance();
    }

    // Clients for API tests
    protected QueueServiceClient primaryQueueServiceClient;
    protected QueueServiceAsyncClient primaryQueueServiceAsyncClient;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        prefix = getCrc32(testInfo.getDisplayName());
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    @AfterEach
    public void cleanup() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }


        QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
            .connectionString(getPrimaryConnectionString())
            .buildClient();

        cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(prefix), null, Context.NONE)
            .forEach(queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName()));
    }

    protected String getRandomName(int length) {
        return testResourceNamer.randomName(prefix, length);
    }

    protected QueueServiceClientBuilder queueServiceBuilderHelper() {
        return instrument(new QueueServiceClientBuilder()).connectionString(getPrimaryConnectionString());
    }

    protected QueueClientBuilder queueBuilderHelper() {
        return instrument(new QueueClientBuilder())
            .connectionString(getPrimaryConnectionString())
            .queueName(getRandomName(60));
    }

    protected QueueServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected QueueClientBuilder getQueueClientBuilder(String endpoint) {
        return instrument(new QueueClientBuilder()).endpoint(endpoint);
    }

     protected Duration getMessageUpdateDelay(long liveMillis) {
        return (getTestMode() == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveMillis);
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader("x-ms-version", "2017-11-09");
                return next.process();
            }

            @Override
            public HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL;
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        // Groovy style reflection. All our builders follow this pattern.
        builder.httpClient(getHttpClient());

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (ENVIRONMENT.getServiceVersion() != null) {
            try {
                Method serviceVersionMethod = builder.getClass()
                    .getDeclaredMethod("serviceVersion", ServiceVersion.class);
                Class<E> serviceVersionClass = (Class<E>) serviceVersionMethod.getParameterTypes()[0];
                ServiceVersion serviceVersion = (ServiceVersion) Enum.valueOf(serviceVersionClass,
                    ENVIRONMENT.getServiceVersion());
                serviceVersionMethod.invoke(builder, serviceVersion);
                builder.addPolicy(new ServiceVersionValidationPolicy(serviceVersion.getVersion()));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        builder.httpLogOptions(QueueServiceClientBuilder.getDefaultHttpLogOptions());

        return builder;
    }

    protected HttpPipelinePolicy getRecordPolicy() {
        if (interceptorManager.isRecordMode()) {
            return interceptorManager.getRecordPolicy();
        } else {
            return NoOpHttpPipelinePolicy.INSTANCE;
        }
    }

    protected HttpClient getHttpClient() {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            switch (ENVIRONMENT.getHttpClientType()) {
                case NETTY:
                    return NETTY_HTTP_CLIENT;
                case OK_HTTP:
                    return OK_HTTP_CLIENT;
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
            }
        } else {
            return interceptorManager.getPlaybackClient();
        }
    }

    protected String getPrimaryConnectionString() {
        return ENVIRONMENT.getPrimaryAccount().getConnectionString();
    }

    protected <T> T retry(Supplier<T> action, Predicate<Exception> retryPredicate, Integer times,
        Duration delay) throws Exception {
        int actualTimes = (times == null) ? 6 : times;
        long actualDelayMillis = (delay == null) ? Duration.ofSeconds(10).toMillis() : delay.toMillis();

        for (int i = 0; i < actualTimes; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                if (!retryPredicate.test(e)) {
                    throw e;
                } else {
                    sleepIfRunningAgainstService(actualDelayMillis);
                }
            }
        }

        throw new Exception("Exhausted all retry attempts.");
    }
}
