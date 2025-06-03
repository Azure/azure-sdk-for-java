// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.SharedExecutorService;
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
import org.junit.jupiter.api.parallel.Isolated;

import java.io.File;
import java.io.IOException;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.core.test.utils.TestUtils.getFaultInjectingHttpClient;
import static com.azure.storage.blob.BlobTestBase.ENVIRONMENT;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set of tests that use <a href="">HTTP fault injecting</a> to simulate scenarios where the network has random errors.
 * Isolated is used to ensure that timeouts caused by resource related issues are not mistaken for fault injections.
 */
@EnabledIf("shouldRun")
@Isolated
public class HttpFaultInjectingTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpFaultInjectingTests.class);

    private BlobContainerClient containerClient;

    @BeforeEach
    public void setup() {
        String testName
            = ("httpFaultInjectingTests" + CoreUtils.randomUuid().toString().replace("-", "")).toLowerCase();
        containerClient = new BlobServiceClientBuilder().endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
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

        BlobClient downloadClient = new BlobClientBuilder().endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .containerName(containerClient.getBlobContainerName())
            .blobName(containerClient.getBlobContainerName())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(getFaultInjectingHttpClient(getFaultInjectingWrappedHttpClient(), false))
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 4, null, 10L, 10L, null))
            .buildClient();

        List<File> files = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            File file = File.createTempFile(CoreUtils.randomUuid().toString() + i, ".txt");
            file.deleteOnExit();
            files.add(file);
        }
        AtomicInteger successCount = new AtomicInteger();

        Set<OpenOption> overwriteOptions
            = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, // If the file already exists and it is opened for WRITE access, then its length is truncated to 0.
                StandardOpenOption.READ, StandardOpenOption.WRITE));

        CountDownLatch countDownLatch = new CountDownLatch(500);
        SharedExecutorService.getInstance().invokeAll(files.stream().map(it -> (Callable<Void>) () -> {
            try {
                downloadClient.downloadToFileWithResponse(
                    new BlobDownloadToFileOptions(it.getAbsolutePath()).setOpenOptions(overwriteOptions)
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
            } finally {
                countDownLatch.countDown();
            }

            return null;
        }).collect(Collectors.toList()));

        countDownLatch.await(10, TimeUnit.MINUTES);

        assertTrue(successCount.get() >= 450,
            () -> "Expected over 450 successes, actual success count was: " + successCount.get());
        // cleanup
        files.forEach(it -> {
            try {
                Files.deleteIfExists(it.toPath());
            } catch (IOException e) {
                LOGGER.atWarning().addKeyValue("file", it.getAbsolutePath()).log("Failed to delete file.", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private HttpClient getFaultInjectingWrappedHttpClient() {
        switch (ENVIRONMENT.getHttpClientType()) {
            case NETTY:
                return HttpClient.createDefault(new HttpClientOptions().readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(NettyAsyncHttpClientProvider.class));

            case OK_HTTP:
                return HttpClient.createDefault(new HttpClientOptions().readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(OkHttpAsyncClientProvider.class));

            case VERTX:
                return HttpClient.createDefault(new HttpClientOptions().readTimeout(Duration.ofSeconds(2))
                    .responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(getVertxClientProviderReflectivelyUntilNameChangeReleases()));

            case JDK_HTTP:
                try {
                    return HttpClient.createDefault(new HttpClientOptions().readTimeout(Duration.ofSeconds(2))
                        .responseTimeout(Duration.ofSeconds(2))
                        .setHttpClientProvider((Class<? extends HttpClientProvider>) Class
                            .forName("com.azure.core.http.jdk.httpclient.JdkHttpClientProvider")));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }

            default:
                throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HttpClientProvider> getVertxClientProviderReflectivelyUntilNameChangeReleases() {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.azure.core.http.vertx.VertxHttpClientProvider");
        } catch (ClassNotFoundException ex) {
            try {
                clazz = Class.forName("com.azure.core.http.vertx.VertxAsyncHttpClientProvider");
            } catch (ClassNotFoundException ex2) {
                ex2.addSuppressed(ex);
                throw new RuntimeException(ex2);
            }
        }

        return (Class<? extends HttpClientProvider>) clazz;
    }

    private static boolean shouldRun() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        // macOS has known issues running HTTP fault injector, change this once
        // https://github.com/Azure/azure-sdk-tools/pull/6216 is resolved
        return ENVIRONMENT.getTestMode() == TestMode.LIVE && !osName.contains("mac os") && !osName.contains("darwin");
    }
}
