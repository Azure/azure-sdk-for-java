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
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import com.azure.core.util.ServiceVersion;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import okhttp3.ConnectionPool;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

/**
 * Base class for Azure Storage Queue tests.
 */
public class QueueTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    protected String prefix;

    // Clients for API tests
    protected QueueServiceClient primaryQueueServiceClient;
    protected QueueServiceAsyncClient primaryQueueServiceAsyncClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(
                Collections.singletonList(new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL)));
        }

        // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
        // in SAS tokens.
        // TODO (alzimmer): Once all Storage libraries are migrated to test proxy move this into the common parent.
        interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Arrays.asList("sv"))));
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }


    /**
     * Clean up the test queues and messages for the account.
     */
    @Override
    protected void afterTest() {
        super.afterTest();
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
        return getRandomName(prefix, length);
    }

    protected String getRandomName(String prefix, int length) {
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

    protected QueueServiceClientBuilder getOAuthServiceClientBuilder(String endpoint) {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder();
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }
        return instrument(builder).endpoint(endpoint);
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

    protected QueueClientBuilder getOAuthQueueClientBuilder(String endpoint) {
        QueueClientBuilder builder = new QueueClientBuilder();
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }
        return instrument(builder).endpoint(endpoint);
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
                Method serviceVersionMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                    .filter(method -> "serviceVersion".equals(method.getName())
                        && method.getParameterCount() == 1
                        && ServiceVersion.class.isAssignableFrom(method.getParameterTypes()[0]))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unable to find serviceVersion method for builder: "
                        + builder.getClass()));
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
}
