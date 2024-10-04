// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.util.binarydata.BinaryData;
import org.conscrypt.Conscrypt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static io.clientcore.http.jdk.httpclient.JdkHttpClientLocalTestServer.LONG_BODY;
import static io.clientcore.http.jdk.httpclient.JdkHttpClientLocalTestServer.SHORT_BODY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientTests {
    private static final String SERVER_HTTP_URI = JdkHttpClientLocalTestServer.getServer().getHttpUri();
    private static final String SERVER_HTTPS_URI = JdkHttpClientLocalTestServer.getServer().getHttpsUri();

    @Test
    public void testResponseShortBodyAsByteArray() throws IOException {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testResponseLongBodyAsByteArray() throws IOException {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void testBufferResponse() throws IOException {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        try (Response<?> response = doRequest(client, "/long")) {
            TestUtils.assertArraysEqual(LONG_BODY, response.getBody().toReplayableBinaryData().toBytes());
        }
    }

    @Test
    public void testBufferedResponse() throws IOException {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest(HttpMethod.GET, uri("/long"))
            .setRequestOptions(new RequestOptions().setResponseBodyMode(ResponseBodyMode.BUFFER));

        try (Response<?> response = client.send(request)) {
            TestUtils.assertArraysEqual(LONG_BODY, response.getBody().toBytes());
        }
    }

    @Test
    public void testMultipleGetBodyBytes() throws IOException {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        try (Response<?> response = doRequest(client, "/short")) {
            TestUtils.assertArraysEqual(SHORT_BODY, response.getBody().toBytes());

            // Second call should return the same body as BinaryData caches the result.
            TestUtils.assertArraysEqual(SHORT_BODY, response.getBody().toBytes());
        }
    }

    @Test
    @Timeout(20)
    public void testWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() throws IOException {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        try (Response<?> response = doRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            assertEquals("error", response.getBody().toString());
        }
    }

    // TODO (alzimmer): re-enable this test when progress reporting is added
    //    @Test
    //    public void testProgressReporter() throws IOException {
    //        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
    //
    //        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
    //        HttpRequest request = new HttpRequest(HttpMethod.POST, uri("/shortPost"));
    //        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(SHORT_BODY.length + LONG_BODY.length));
    //        request.setBody(
    //            BinaryData.fromListByteBuffer(Arrays.asList(ByteBuffer.wrap(LONG_BODY), ByteBuffer.wrap(SHORT_BODY))));
    //
    //        Contexts contexts = Contexts.with(Context.NONE)
    //            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::add));
    //
    //        try (Response<?> response = client.send(request)) {
    //            assertEquals(200, response.getStatusCode());
    //            List<Long> progressList = progress.stream().collect(Collectors.toList());
    //            assertEquals(LONG_BODY.length, progressList.get(0));
    //            assertEquals(SHORT_BODY.length + LONG_BODY.length, progressList.get(1));
    //        }
    //    }

    @Test
    public void testFileUpload() throws IOException {
        Path tempFile = writeToTempFile(LONG_BODY);
        tempFile.toFile().deleteOnExit();
        BinaryData body = BinaryData.fromFile(tempFile, 1L, 42L);

        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, uri("/shortPostWithBodyValidation")).setBody(body);

        try (Response<?> response = client.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, uri("/shortPost"));
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "132");
        request.setBody(BinaryData.fromStream(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new RuntimeException("boo");
            }
        }));

        IOException thrown = assertThrows(IOException.class, () -> client.send(request).close());
        assertEquals("boo", thrown.getCause().getMessage());
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, uri("/shortPost"));
        request.getHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)));
        request.setBody(BinaryData.fromStream(new InputStream() {
            int count = 0;

            @Override
            public int read() throws IOException {
                if (count++ < repetitions) {
                    return contentChunk.charAt(count % contentChunk.length());
                } else {
                    throw new RuntimeException("boo");
                }
            }
        }));

        IOException thrown = assertThrows(IOException.class, () -> client.send(request).close());
        assertEquals("boo", thrown.getCause().getMessage());
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContent() {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();

        HttpRequest request = new HttpRequest(HttpMethod.GET, uri("/connectionClose"));
        assertThrows(IOException.class, () -> client.send(request).close());
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();

        ForkJoinPool pool = new ForkJoinPool();
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (Response<?> response = doRequest(client, "/long")) {
                    byte[] body = response.getBody().toBytes();
                    TestUtils.assertArraysEqual(LONG_BODY, body);
                    return null;
                }
            });
        }

        pool.invokeAll(requests);
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @Test
    public void testIOExceptionInWriteBodyTo() {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();

        assertThrows(IOException.class, () -> {
            try (Response<?> response = doRequest(client, "/long")) {
                response.getBody().writeTo(new ThrowingWritableByteChannel());
            }
        });
    }

    @Test
    public void noResponseTimesOut() {
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1)).build();

        assertThrows(HttpTimeoutException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<?> response = doRequest(client, "/noResponse")) {
                assertNotNull(response);
            }
        }));
    }

    @Test
    public void slowStreamReadingTimesOut() {
        // Set both the response timeout and read timeout to make sure we aren't getting a response timeout when the
        // response body is slow to be sent.
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(1))
            .build();

        assertThrows(HttpTimeoutException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<?> response = doRequest(client, "/slowResponse")) {
                TestUtils.assertArraysEqual(SHORT_BODY, response.getBody().toBytes());
            }
        }));
    }

    @Test
    public void slowEagerReadingTimesOut() {
        // Set both the response timeout and read timeout to make sure we aren't getting a response timeout when the
        // response body is slow to be sent.
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(1))
            .build();

        assertThrows(HttpTimeoutException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<?> response = doRequest(client, "/slowResponse", ResponseBodyMode.BUFFER)) {
                TestUtils.assertArraysEqual(SHORT_BODY, response.getBody().toBytes());
            }
        }));
    }

    @Test
    public void testCustomSslContext() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2", Conscrypt.newProvider());

        // Initialize the SSL context with a trust manager that trusts all certificates.
        sslContext.init(null, new TrustManager[] { new InsecureTrustManager() }, null);

        HttpClient httpClient = new JdkHttpClientBuilder().sslContext(sslContext).build();

        try (Response<?> response = httpClient.send(new HttpRequest(HttpMethod.GET, httpsUri("/short")))) {
            TestUtils.assertArraysEqual(SHORT_BODY, response.getBody().toBytes());
        }
    }

    private static URI uri(String path) {
        try {
            return new URI(SERVER_HTTP_URI + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static URI httpsUri(String path) {
        try {
            return new URI(SERVER_HTTPS_URI + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) throws IOException {
        HttpClient client = new JdkHttpClientProvider().getSharedInstance();
        try (Response<?> response = doRequest(client, path)) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            WritableByteChannel body = Channels.newChannel(outStream);
            response.getBody().writeTo(body);
            TestUtils.assertArraysEqual(expectedBody, outStream.toByteArray());
        }
    }

    private static Response<?> doRequest(HttpClient client, String path) throws IOException {
        return doRequest(client, path, null);
    }

    private static Response<?> doRequest(HttpClient client, String path, ResponseBodyMode bodyMode) throws IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET, uri(path))
            .setRequestOptions(new RequestOptions().setResponseBodyMode(bodyMode));
        return client.send(request);
    }

    private static Path writeToTempFile(byte[] body) throws IOException {
        Path tempFile = Files.createTempFile("data", null);
        tempFile.toFile().deleteOnExit();
        String tempFilePath = tempFile.toString();
        FileOutputStream outputStream = new FileOutputStream(tempFilePath);
        outputStream.write(body);
        outputStream.close();
        return tempFile;
    }

    private static final class ThrowingWritableByteChannel implements WritableByteChannel {
        private boolean open = true;

        @Override
        public int write(ByteBuffer src) throws IOException {
            throw new IOException();
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            open = false;
        }
    }
}
