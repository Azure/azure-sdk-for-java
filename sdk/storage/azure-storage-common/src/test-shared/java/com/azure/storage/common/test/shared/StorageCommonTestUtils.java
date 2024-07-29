// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.test.shared;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.vertx.VertxAsyncHttpClientProvider;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ServiceVersion;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.common.implementation.Constants;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class contains utility methods for Storage tests.
 */
public final class StorageCommonTestUtils {
    public static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientProvider().createInstance();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncClientProvider().createInstance();
    private static final HttpClient VERTX_HTTP_CLIENT = new VertxAsyncHttpClientProvider().createInstance();
    private static final HttpClient JDK_HTTP_HTTP_CLIENT;

    static {
        HttpClient jdkHttpHttpClient;
        try {
            jdkHttpHttpClient = createJdkHttpClient();
        } catch (LinkageError | ReflectiveOperationException e) {
            jdkHttpHttpClient = null;
        }

        JDK_HTTP_HTTP_CLIENT = jdkHttpHttpClient;
    }

    @SuppressWarnings("deprecation")
    private static HttpClient createJdkHttpClient() throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("com.azure.core.http.jdk.httpclient.JdkHttpClientProvider");
        return (HttpClient) clazz.getDeclaredMethod("createInstance").invoke(clazz.newInstance());
    }

    /**
     * Gets the CRC32 for the given string.
     *
     * @param input The string to get the CRC32 for.
     * @return The CRC32.
     */
    public static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }

    /**
     * Gets an HttpClient based on the test mode and the environment configuration.
     *
     * @param playbackClientSupplier Supplier for the playback client.
     * @return An HttpClient instance.
     */
    public static HttpClient getHttpClient(Supplier<HttpClient> playbackClientSupplier) {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            switch (ENVIRONMENT.getHttpClientType()) {
                case NETTY:
                    return NETTY_HTTP_CLIENT;
                case OK_HTTP:
                    return OK_HTTP_CLIENT;
                case VERTX:
                    return VERTX_HTTP_CLIENT;
                case JDK_HTTP:
                    return JDK_HTTP_HTTP_CLIENT;
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
            }
        } else {
            return playbackClientSupplier.get();
        }
    }

    /**
     * Converts an input stream to a byte array.
     *
     * @param inputStream The input stream to convert.
     * @return The byte array.
     */
    public static byte[] convertInputStreamToByteArray(InputStream inputStream) {
        return convertInputStreamToByteArray(inputStream, 4096);
    }

    /**
     * Converts an input stream to a byte array.
     *
     * @param inputStream The input stream to convert.
     * @param expectedSize The expected size of the byte array.
     * @return The byte array.
     */
    public static byte[] convertInputStreamToByteArray(InputStream inputStream, int expectedSize) {
        byte[] buffer = new byte[4096];
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(expectedSize);
        try {
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return outputStream.toByteArray();
    }

    /**
     * Compares two files for having equivalent content.
     *
     * @param file1 File used to upload data to the service
     * @param file2 File used to download data from the service
     * @param offset Write offset from the upload file
     * @param count Size of the download from the service
     * @return Whether the files have equivalent content based on offset and read count
     */
    public static boolean compareFiles(File file1, File file2, long offset, long count) throws IOException {
        long pos = 0L;
        int defaultBufferSize = 128 * Constants.KB;
        FileInputStream stream1 = new FileInputStream(file1);
        stream1.skip(offset);
        FileInputStream stream2 = new FileInputStream(file2);

        try {
            // If the amount we are going to read is smaller than the default buffer size use that instead.
            int bufferSize = (int) Math.min(defaultBufferSize, count);

            while (pos < count) {
                // Number of bytes we expect to read.
                int expectedReadCount = (int) Math.min(bufferSize, count - pos);
                byte[] buffer1 = new byte[expectedReadCount];
                byte[] buffer2 = new byte[expectedReadCount];

                int readCount1 = stream1.read(buffer1);
                int readCount2 = stream2.read(buffer2);

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                TestUtils.assertArraysEqual(buffer1, buffer2);
                assertEquals(readCount1, readCount2);

                pos += expectedReadCount;
            }

            int verificationRead = stream2.read();
            return pos == count && verificationRead == -1;
        } finally {
            stream1.close();
            stream2.close();
        }
    }

    /**
     * Instruments a builder with the test interceptor manager.
     *
     * @param builder The builder to instrument.
     * @param interceptorManager The interceptor manager to use.
     * @param <T> The type of the builder.
     * @param <E> The type of the service version.
     * @return The instrumented builder.
     */
    @SuppressWarnings("unchecked")
    public static <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder, HttpLogOptions logOptions,
        InterceptorManager interceptorManager) {
        // Groovy style reflection. All our builders follow this pattern.
        builder.httpClient(getHttpClient(interceptorManager));

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (ENVIRONMENT.getServiceVersion() != null) {
            try {
                Method serviceVersionMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                    .filter(method -> "serviceVersion".equals(method.getName()) && method.getParameterCount() == 1
                        && ServiceVersion.class.isAssignableFrom(method.getParameterTypes()[0]))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                        "Unable to find serviceVersion method for builder: " + builder.getClass()));
                Class<E> serviceVersionClass = (Class<E>) serviceVersionMethod.getParameterTypes()[0];
                ServiceVersion serviceVersion = (ServiceVersion) Enum.valueOf(serviceVersionClass,
                    ENVIRONMENT.getServiceVersion());
                serviceVersionMethod.invoke(builder, serviceVersion);
                builder.addPolicy(new ServiceVersionValidationPolicy(serviceVersion.getVersion()));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        return builder.httpLogOptions(logOptions);
    }

    /**
     * Gets an HttpClient based on the test mode and the environment configuration.
     *
     * @param interceptorManager The interceptor manager to use.
     * @return An HttpClient instance.
     */
    public static HttpClient getHttpClient(InterceptorManager interceptorManager) {
        return StorageCommonTestUtils.getHttpClient(interceptorManager::getPlaybackClient);
    }

    /**
     * Gets a random byte array of the given size.
     *
     * @param size The size of the byte array.
     * @param testResourceNamer The test resource namer to use.
     * @return The random byte array.
     */
    public static byte[] getRandomByteArray(int size, TestResourceNamer testResourceNamer) {
        long seed = UUID.fromString(testResourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    /**
     * Gets a random ByteBuffer of the given size.
     *
     * @param size The size of the ByteBuffer.
     * @param testResourceNamer The test resource namer to use.
     * @return The random ByteBuffer.
     */
    public static ByteBuffer getRandomData(int size, TestResourceNamer testResourceNamer) {
        return ByteBuffer.wrap(getRandomByteArray(size, testResourceNamer));
    }

    /**
     * Gets a random file of the given size.
     *
     * @param size The size of the file.
     * @param testResourceNamer The test resource namer to use.
     * @return The random file.
     * @throws IOException If an I/O error occurs.
     */
    public static File getRandomFile(int size, TestResourceNamer testResourceNamer) throws IOException {
        try {
            File file = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
            file.deleteOnExit();

            if (size > Constants.MB) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] data = getRandomByteArray(Constants.MB, testResourceNamer);
                    int mbChunks = size / Constants.MB;
                    int remaining = size % Constants.MB;
                    for (int i = 0; i < mbChunks; i++) {
                        fos.write(data);
                    }

                    if (remaining > 0) {
                        fos.write(data, 0, remaining);
                    }
                }
            } else {
                Files.write(file.toPath(), getRandomByteArray(size, testResourceNamer));
            }

            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets token credentials for a test.
     *
     * @param interceptorManager The interceptor manager to use.
     * @return The TokenCredential to use.
     */
    public static TokenCredential getTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isPlaybackMode()) {
            return new MockTokenCredential();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else { //live
            Configuration config = Configuration.getGlobalConfiguration();

            ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder().addLast(
                    new EnvironmentCredentialBuilder().build())
                .addLast(new AzureCliCredentialBuilder().build())
                .addLast(new AzureDeveloperCliCredentialBuilder().build());

            String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

            if (!CoreUtils.isNullOrEmpty(serviceConnectionId) && !CoreUtils.isNullOrEmpty(clientId)
                && !CoreUtils.isNullOrEmpty(tenantId) && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                AzurePipelinesCredential pipelinesCredential = new AzurePipelinesCredentialBuilder().systemAccessToken(
                        systemAccessToken)
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId)
                    .build();

                builder.addLast(
                    request -> pipelinesCredential.getToken(request).subscribeOn(Schedulers.boundedElastic()));
            }

            builder.addLast(new AzurePowerShellCredentialBuilder().build());

            return builder.build();
        }
    }
}
