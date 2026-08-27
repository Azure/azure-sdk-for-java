// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.TestMode;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests that the default locality-aware chunk-download wiring works end-to-end (layout cache construction,
 * per-chunk endpoint resolution, and {@code Context} propagation to {@code DataLocalityPolicy}) without altering the
 * bytes returned.
 * <p>
 * The playback-safe tests assert integrity across SDK-managed chunked downloads against existing recordings. Live runs
 * use a large enough blob to receive {@code x-ms-download-hint: layout}; the live-only routing test then verifies that
 * subsequent range requests are sent to the service-provided layout endpoint while preserving the original account
 * authority in the {@code Host} header.
 */
public class BlobDataLocalityDownloadApiTests extends BlobTestBase {
    private static final int LIVE_TEST_CONTENT_LENGTH = 16 * Constants.MB;
    private static final int PLAYBACK_TEST_CONTENT_LENGTH = 16 * Constants.KB;
    private static final int LIVE_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int PLAYBACK_DOWNLOAD_BLOCK_SIZE = 2 * Constants.KB;
    private static final int CONTENT_PATTERN_LENGTH = 4 * Constants.KB;

    private BlobClient bc;
    private byte[] contentBytes;
    private Path testFile;

    @Override
    public void beforeTest() {
        if (testContextManager.doNotRecordTest()) {
            return;
        }

        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        if (testContextManager.doNotRecordTest()) {
            return;
        }

        super.afterTest();
    }

    @BeforeEach
    public void setup(TestInfo testInfo) {
        if (testInfo.getTestMethod().map(method -> method.isAnnotationPresent(DoNotRecord.class)).orElse(false)) {
            return;
        }

        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        contentBytes = createTestContent();
        bc.getBlockBlobClient().upload(new java.io.ByteArrayInputStream(contentBytes), contentBytes.length, true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (testFile != null) {
            Files.deleteIfExists(testFile);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void downloadToFileWithDefaultDataLocalitySingleChunk() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(
            () -> bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()), null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void downloadToFileWithDefaultDataLocalityMultipleChunks() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(() -> bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong((long) getDownloadBlockSize())),
            null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @LiveOnly
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void downloadToFileWithDefaultDataLocalityRoutesChunksToLayoutEndpoint() throws IOException {
        List<RequestHostRecord> records = new CopyOnWriteArrayList<>();
        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            recordRequestHostsPolicy(records));

        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(() -> downloadClient
            .downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()).setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong((long) LIVE_DOWNLOAD_BLOCK_SIZE)), null, null));

        URI accountUri = URI.create(bc.getBlobUrl());
        String accountHost = accountUri.getHost();
        String accountAuthority = accountUri.getAuthority();
        List<RequestHostRecord> rewrittenRecords = getRewrittenRecords(records, accountHost);

        assertFalse(rewrittenRecords.isEmpty(),
            "Expected at least one chunk request to be routed to a layout endpoint. Observed requests: " + records);
        for (RequestHostRecord record : rewrittenRecords) {
            assertEquals(accountAuthority, record.hostHeader,
                "Layout-routed request must preserve the original account authority in the Host header.");
        }
        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void openInputStreamWithDefaultDataLocality() throws IOException {
        BlobInputStreamOptions options = new BlobInputStreamOptions().setBlockSize(getDownloadBlockSize())
            .setRange(new BlobRange(0, (long) contentBytes.length));

        byte[] readBytes;
        try (InputStream is = bc.openInputStream(options, null)) {
            readBytes = readAll(is);
        }

        assertArrayEquals(contentBytes, readBytes);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void openInputStreamWithDefaultDataLocalityPartialRange() throws IOException {
        BlobInputStreamOptions options = new BlobInputStreamOptions().setBlockSize(getDownloadBlockSize())
            .setRange(new BlobRange(Constants.KB, (long) (4 * Constants.KB)));

        byte[] readBytes;
        try (InputStream is = bc.openInputStream(options, null)) {
            readBytes = readAll(is);
        }

        byte[] expected = new byte[4 * Constants.KB];
        System.arraycopy(contentBytes, Constants.KB, expected, 0, expected.length);
        assertArrayEquals(expected, readBytes);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void downloadToFileWithDefaultDataLocalityReturnsProperties() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        BlobProperties properties
            = bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()), null, null).getValue();

        assertEquals(contentBytes.length, properties.getBlobSize());
    }

    @DoNotRecord
    @Test
    public void downloadToFileWithLayoutAwareRoutingDisabledDoesNotFetchLayout() throws IOException {
        byte[] testContent = new byte[64];
        for (int i = 0; i < testContent.length; i++) {
            testContent[i] = (byte) i;
        }
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient(testContent);
        BlobClient downloadClient = new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .buildClient();

        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(
            () -> downloadClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString())
                .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(8L))
                .setLayoutAwareRouting(com.azure.storage.blob.models.LayoutAwareRouting.DISABLED), null, null));

        assertArrayEquals(testContent, Files.readAllBytes(testFile));
        assertEquals(0, httpClient.getLayoutRequestCount());
        for (RequestRecord record : httpClient.getDataRequestRecords()) {
            assertEquals("account.blob.core.windows.net", record.requestHost);
            assertEquals("account.blob.core.windows.net", record.urlHost);
            assertNull(record.hostHeader);
        }
    }

    private static byte[] readAll(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static byte[] createTestContent() {
        byte[] content = new byte[getTestContentLength()];
        byte[] pattern = new byte[CONTENT_PATTERN_LENGTH];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (byte) (i % 256);
        }

        System.arraycopy(pattern, 0, content, 0, pattern.length);
        int copied = pattern.length;
        while (copied < content.length) {
            int bytesToCopy = Math.min(copied, content.length - copied);
            System.arraycopy(content, 0, content, copied, bytesToCopy);
            copied += bytesToCopy;
        }

        return content;
    }

    private static final class LayoutRoutingHttpClient implements com.azure.core.http.HttpClient {
        private final byte[] contentBytes;
        private final List<RequestRecord> dataRequestRecords = new ArrayList<>();
        private int layoutRequestCount;

        private LayoutRoutingHttpClient(byte[] contentBytes) {
            this.contentBytes = contentBytes;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            RequestRecord record = new RequestRecord(request);
            if (record.url.contains("comp=layout")) {
                layoutRequestCount++;
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                    .set(HttpHeaderName.ETAG, "\"layout-etag\"");
                return Mono.just(new MockHttpResponse(request, 200, headers,
                    ("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<BlobLayout><Ranges><Range Start=\"0\" End=\""
                        + (contentBytes.length - 1)
                        + "\" EndpointIndex=\"0\" /></Ranges><Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" />"
                        + "</Endpoints></BlobLayout>").getBytes()));
            }

            dataRequestRecords.add(record);
            String rangeHeader = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
            int start = 0;
            int end = contentBytes.length - 1;
            if (rangeHeader != null) {
                String[] parts = rangeHeader.replace("bytes=", "").split("-");
                start = Integer.parseInt(parts[0]);
                end = parts.length > 1 ? Integer.parseInt(parts[1]) : end;
            }

            int normalizedEnd = Math.min(end, contentBytes.length - 1);
            byte[] body = new byte[normalizedEnd - start + 1];
            System.arraycopy(contentBytes, start, body, 0, body.length);
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(body.length))
                .set(HttpHeaderName.CONTENT_RANGE, "bytes " + start + "-" + normalizedEnd + "/" + contentBytes.length)
                .set(HttpHeaderName.ETAG, "\"etag\"");
            if (dataRequestRecords.size() == 1) {
                headers.set(HttpHeaderName.fromString("x-ms-download-hint"), "layout");
            }

            return Mono.just(new MockHttpResponse(request, 206, headers, body));
        }

        int getLayoutRequestCount() {
            return layoutRequestCount;
        }

        List<RequestRecord> getDataRequestRecords() {
            return dataRequestRecords;
        }
    }

    private static final class RequestRecord {
        private final String url;
        private final String urlHost;
        private final String requestHost;
        private final String hostHeader;

        private RequestRecord(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.urlHost = request.getUrl().getHost();
            this.requestHost = request.getUrl().getHost();
            this.hostHeader = request.getHeaders().getValue(HttpHeaderName.HOST);
        }
    }

    private static int getTestContentLength() {
        return ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? PLAYBACK_TEST_CONTENT_LENGTH : LIVE_TEST_CONTENT_LENGTH;
    }

    private static int getDownloadBlockSize() {
        return ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? PLAYBACK_DOWNLOAD_BLOCK_SIZE : LIVE_DOWNLOAD_BLOCK_SIZE;
    }

    private static List<RequestHostRecord> getRewrittenRecords(List<RequestHostRecord> records, String accountHost) {
        List<RequestHostRecord> rewrittenRecords = new ArrayList<>();
        for (RequestHostRecord record : records) {
            if (record.requestHost != null && !record.requestHost.equalsIgnoreCase(accountHost)) {
                rewrittenRecords.add(record);
            }
        }

        return rewrittenRecords;
    }

    private static HttpPipelinePolicy recordRequestHostsPolicy(List<RequestHostRecord> records) {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                recordRequest(context);
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                recordRequest(context);
                return next.processSync();
            }

            private void recordRequest(HttpPipelineCallContext context) {
                records.add(new RequestHostRecord(context.getHttpRequest().getUrl().getHost(),
                    context.getHttpRequest().getHeaders().getValue(HttpHeaderName.HOST)));
            }
        };
    }

    private static final class RequestHostRecord {
        private final String requestHost;
        private final String hostHeader;

        private RequestHostRecord(String requestHost, String hostHeader) {
            this.requestHost = requestHost;
            this.hostHeader = hostHeader;
        }

        @Override
        public String toString() {
            return "RequestHostRecord{requestHost='" + requestHost + "', hostHeader='" + hostHeader + "'}";
        }
    }
}
