// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.monitor.ingestion.LogsIngestionTestBase.getObjects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link LogsIngestionClient}.
 */
public class LogsIngestionClientConcurrencyTest {
    private static final String ENDPOINT = "https://dce.monitor.azure.com";
    private static final String RULE_ID = "dcr-a64851bc17714f0483d1e96b5d84953b";
    private static final String STREAM = "Custom-MyTableRawData";
    private static final int LOGS_IN_BATCH = 9800; // approx

    private LogsIngestionClientBuilder clientBuilder;

    @BeforeEach
    void beforeEach() {
        clientBuilder = new LogsIngestionClientBuilder()
            .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .endpoint(ENDPOINT);
    }

    @SyncAsyncTest
    public void testUploadLogsInBatchesConcurrent() {
        int concurrency = 10;
        int batchCount = 20;
        List<Object> logs = getObjects(LOGS_IN_BATCH * batchCount);

        TestHttpClient http = new TestHttpClient(false);
        clientBuilder.httpClient(http);
        LogsUploadOptions uploadOptions = new LogsUploadOptions().setMaxConcurrency(concurrency);

        SyncAsyncExtension.execute(() -> clientBuilder.buildClient().upload(RULE_ID, STREAM, logs, uploadOptions),
            () -> clientBuilder.buildAsyncClient().upload(RULE_ID, STREAM, logs, uploadOptions));
        assertEquals(batchCount, http.getCallsCount());
        assertTrue(http.getMaxConcurrentCalls() <= concurrency + 1,
            String.format("http.getMaxConcurrentCalls() = %s", http.getMaxConcurrentCalls()));
    }

    @Test
    public void testUploadLogsPartialFailureConcurrent() {
        int concurrency = 4;
        int batchCount = 7;
        List<Object> logs = getObjects(LOGS_IN_BATCH * batchCount);

        TestHttpClient http = new TestHttpClient(true);
        clientBuilder.httpClient(http);
        LogsUploadOptions uploadOptions = new LogsUploadOptions().setMaxConcurrency(concurrency);

        LogsIngestionClient client = clientBuilder.httpClient(http).buildClient();

        LogsUploadException uploadLogsException
            = assertThrows(LogsUploadException.class, () -> client.upload(RULE_ID, STREAM, logs, uploadOptions));

        asserError(uploadLogsException);
        assertEquals(batchCount, http.getCallsCount());
    }

    @Test
    public void testUploadLogsPartialFailureConcurrentAsync() {
        int concurrency = 3;
        int batchCount = 12;
        List<Object> logs = getObjects(LOGS_IN_BATCH * batchCount);

        TestHttpClient http = new TestHttpClient(true);
        LogsUploadOptions uploadOptions = new LogsUploadOptions().setMaxConcurrency(concurrency);

        StepVerifier
            .create(clientBuilder.httpClient(http).buildAsyncClient().upload(RULE_ID, STREAM, logs, uploadOptions))
            .consumeErrorWith(ex -> {
                assertTrue(ex instanceof LogsUploadException);
                asserError((LogsUploadException) ex);
            })
            .verify();
        assertEquals(batchCount, http.getCallsCount());
    }

    private static void asserError(LogsUploadException uploadException) {
        assertEquals(LOGS_IN_BATCH, uploadException.getFailedLogsCount(), 200);
        assertEquals(1, uploadException.getLogsUploadErrors().size());
    }

    public class TestHttpClient extends NoOpHttpClient {
        private final AtomicInteger concurrentCalls = new AtomicInteger(0);
        private final AtomicInteger maxConcurrency;
        private final AtomicBoolean failSecondRequest;
        private final AtomicInteger counter;

        public TestHttpClient(boolean failSecondRequest) {
            this.maxConcurrency = new AtomicInteger();
            this.failSecondRequest = new AtomicBoolean(failSecondRequest);
            this.counter = new AtomicInteger();
        }

        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.delay(Duration.ofMillis(1)).map(l -> process(request));
        }

        public HttpResponse sendSync(HttpRequest request, Context context) {
            return process(request);
        }

        public int getCallsCount() {
            return counter.get();
        }

        private HttpResponse process(HttpRequest request) {
            int c = concurrentCalls.incrementAndGet();
            if (c > maxConcurrency.get()) {
                maxConcurrency.set(c);
            }

            try {
                Thread.sleep(1000);
                if (counter.getAndIncrement() == 1 && failSecondRequest.compareAndSet(true, false)) {
                    return new MockHttpResponse(request, 404);
                }
                return new MockHttpResponse(request, 204);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                concurrentCalls.decrementAndGet();
            }
        }

        public int getMaxConcurrentCalls() {
            return maxConcurrency.get();
        }
    }
}
