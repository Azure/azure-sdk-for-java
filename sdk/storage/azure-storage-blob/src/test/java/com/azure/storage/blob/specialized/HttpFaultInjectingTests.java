// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.http.vertx.VertxAsyncHttpClientProvider;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.storage.blob.BlobTestBase.ENVIRONMENT;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set of tests that use <a href="">HTTP fault injecting</a> to simulate scenarios where the network has random errors.
 */
@EnabledIf("shouldRun")
public class HttpFaultInjectingTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpFaultInjectingTests.class);
    private static final HttpHeaderName UPSTREAM_URI_HEADER = HttpHeaderName.fromString("X-Upstream-Base-Uri");
    private static final HttpHeaderName HTTP_FAULT_INJECTOR_RESPONSE_HEADER
        = HttpHeaderName.fromString("x-ms-faultinjector-response-option");

    private BlobContainerClient containerClient;

    @BeforeEach
    public void setup() {
        String testName = ("httpFaultInjectingTests" + CoreUtils.randomUuid().toString().replace("-", ""))
            .toLowerCase();
        containerClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(BlobTestBase.getHttpClient(() -> {
                throw new RuntimeException("Test should not run during playback.");
            }))
            .buildClient()
            .createBlobContainer(testName);
    }

    @AfterEach
    public void teardown() {
        if (containerClient != null) {
            containerClient.delete();
        }
    }

    /**
     * Tests downloading to file with fault injection.
     * <p>
     * This test will upload a single blob of about 9MB and then download it in parallel 500 times. Each download will
     * have its file contents compared to the original blob data. The test only cares about files that were properly
     * downloaded, if a download fails with a network error it will be ignored. A requirement of 90% of files being
     * successfully downloaded is also a requirement to prevent a case where most files failed to download and passing,
     * hiding a true issue.
     */
    @Test
    public void downloadToFileWithFaultInjection() throws IOException, InterruptedException {
        byte[] realFileBytes = new byte[9 * Constants.MB - 1];
        ThreadLocalRandom.current().nextBytes(realFileBytes);

        containerClient.getBlobClient(containerClient.getBlobContainerName())
            .upload(BinaryData.fromBytes(realFileBytes), true);

        BlobClient downloadClient = new BlobClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .containerName(containerClient.getBlobContainerName())
            .blobName(containerClient.getBlobContainerName())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(new HttpFaultInjectingHttpClient(getFaultInjectingWrappedHttpClient()))
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 4, null, 10L, 10L, null))
            .buildClient();

        List<File> files = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            File file = File.createTempFile(CoreUtils.randomUuid().toString() + i, ".txt");
            file.deleteOnExit();
            files.add(file);
        }
        AtomicInteger successCount = new AtomicInteger();

        Set<OpenOption> overwriteOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, // If the file already exists and it is opened for WRITE access, then its length is truncated to 0.
            StandardOpenOption.READ, StandardOpenOption.WRITE));

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.invokeAll(files.stream().map(it -> (Callable<Void>) () -> {
            try {
                downloadClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(it.getAbsolutePath())
                        .setOpenOptions(overwriteOptions)
                        .setParallelTransferOptions(new ParallelTransferOptions().setMaxConcurrency(2)),
                    null, Context.NONE);
                byte[] actualFileBytes = Files.readAllBytes(it.toPath());
                TestUtils.assertArraysEqual(realFileBytes, actualFileBytes);
                LOGGER.atVerbose()
                    .addKeyValue("successCount", successCount.incrementAndGet())
                    .log("Download completed successfully.");
                Files.deleteIfExists(it.toPath());
            } catch (Exception ex) {
                // Don't let network exceptions fail the download
                LOGGER.atWarning()
                    .addKeyValue("downloadFile", it.getAbsolutePath())
                    .log("Failed to complete download.", ex);
            }

            return null;
        }).collect(Collectors.toList()));

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        assertTrue(successCount.get() >= 450);
        // cleanup
        files.forEach(it -> {
            try {
                Files.deleteIfExists(it.toPath());
            } catch (IOException e) {
                LOGGER.atWarning()
                    .addKeyValue("file", it.getAbsolutePath())
                    .log("Failed to delete file.", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private HttpClient getFaultInjectingWrappedHttpClient() {
        switch (ENVIRONMENT.getHttpClientType()) {
            case NETTY:
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(NettyAsyncHttpClientProvider.class));
            case OK_HTTP:
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(OkHttpAsyncClientProvider.class));
            case VERTX:
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(VertxAsyncHttpClientProvider.class));
            case JDK_HTTP:
                try {
                    return HttpClient.createDefault(new HttpClientOptions()
                        .readTimeout(Duration.ofSeconds(2))
                        .responseTimeout(Duration.ofSeconds(2))
                        .setHttpClientProvider((Class<? extends HttpClientProvider>) Class.forName(
                            "com.azure.core.http.jdk.httpclient.JdkHttpClientProvider")));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }

            default:
                throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
        }
    }

    // For now a local implementation is here in azure-storage-blob until this is released in azure-core-test.
    // Since this is a local definition with a clear set of configurations everything is simplified.
    private static final class HttpFaultInjectingHttpClient implements HttpClient {
        private final HttpClient wrappedHttpClient;

        HttpFaultInjectingHttpClient(HttpClient wrappedHttpClient) {
            this.wrappedHttpClient = wrappedHttpClient;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return send(request, Context.NONE);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
            String faultType = faultInjectorHandling();
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

            return wrappedHttpClient.send(request, context)
                .map(response -> {
                    HttpRequest request1 = response.getRequest();
                    request1.getHeaders().remove(UPSTREAM_URI_HEADER);
                    request1.setUrl(originalUrl);

                    return response;
                });
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
            String faultType = faultInjectorHandling();
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

            HttpResponse response = wrappedHttpClient.sendSync(request, context);
            response.getRequest().setUrl(originalUrl);
            response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER);

            return response;
        }

        private static URL rewriteUrl(URL originalUrl) {
            try {
                return UrlBuilder.parse(originalUrl)
                    .setScheme("http")
                    .setHost("localhost")
                    .setPort(7777)
                    .toUrl();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        private static String faultInjectorHandling() {
            // f: Full response
            // p: Partial Response (full headers, 50% of body), then wait indefinitely
            // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
            // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
            // pn: Partial Response (full headers, 50% of body), then finish normally
            // n: No response, then wait indefinitely
            // nc: No response, then close (TCP FIN)
            // na: No response, then abort (TCP RST)
            double random = ThreadLocalRandom.current().nextDouble();
            int choice = (int) (random * 100);

            if (choice >= 25) {
                // 75% of requests complete without error.
                return "f";
            } else if (choice >= 1) {
                if (random <= 0.34D) {
                    return "n";
                } else if (random <= 0.67D) {
                    return "nc";
                } else {
                    return "na";
                }
            } else {
                if (random <= 0.25D) {
                    return "p";
                } else if (random <= 0.50D) {
                    return "pc";
                } else if (random <= 0.75D) {
                    return "pa";
                } else {
                    return "pn";
                }
            }
        }
    }

    private static boolean shouldRun() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        // macOS has known issues running HTTP fault injector, change this once
        // https://github.com/Azure/azure-sdk-tools/pull/6216 is resolved
        return ENVIRONMENT.getTestMode() == TestMode.LIVE
            && !osName.contains("mac os")
            && !osName.contains("darwin");
    }
}
