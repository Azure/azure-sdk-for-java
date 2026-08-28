// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
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
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the default locality-aware chunk-download wiring works end-to-end (layout cache construction,
 * per-chunk endpoint resolution, and {@code Context} propagation to {@code DataLocalityPolicy}) without altering the
 * bytes returned.
 * <p>
 * The playback-safe tests assert integrity across SDK-managed chunked downloads against existing recordings. Live runs
 * use a large enough blob to receive {@code x-ms-download-hint: layout}; the live-only routing test then verifies that
 * subsequent range requests are sent to the service-provided layout endpoint while preserving the original account
 * authority in the {@code Host} header.
 * <p>
 * The mock-backed tests additionally cover the caller-supplied one-shot endpoint on
 * {@link BlobDownloadStreamOptions} and {@link BlobDownloadContentOptions}, which lets a caller who already holds a
 * layout route a single download without the SDK fetching a layout itself.
 */
public class BlobDataLocalityDownloadApiTests extends BlobTestBase {
    private static final int LIVE_TEST_CONTENT_LENGTH = 16 * Constants.MB;
    private static final int PLAYBACK_TEST_CONTENT_LENGTH = 16 * Constants.KB;
    private static final int LIVE_DOWNLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int NO_HINT_LIVE_CONTENT_LENGTH = 4 * Constants.MB;
    private static final int NO_HINT_LIVE_DOWNLOAD_BLOCK_SIZE = Constants.MB;
    private static final int PLAYBACK_DOWNLOAD_BLOCK_SIZE = 2 * Constants.KB;
    private static final int CONTENT_PATTERN_LENGTH = 4 * Constants.KB;
    private static final String ORIGINAL_HOST = "account.blob.core.windows.net";
    private static final String DATA_LOCALITY_ENDPOINT = "https://host-a:443";
    private static final String DATA_LOCALITY_HOST = "host-a";
    private static final byte[] MOCK_BODY
        = "the quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);
    private static final int MOCK_FAILURE_OFFSET = 16;

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

        BlobProperties properties
            = bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()), null, null).getValue();

        assertEquals(contentBytes.length, properties.getBlobSize());
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

        // The layout is fetched once and cached for the duration of the download; re-fetching it per chunk would be
        // a regression. Continuation pages are part of that one enumeration, so only marker-less requests are counted.
        assertEquals(1, countLayoutEnumerations(records),
            "Expected exactly one comp=layout enumeration. Observed requests: " + records);

        // Verify each routed chunk's host matches an endpoint from the public getLayout API.
        // Use bc (plain client, no recording policy) so this call does not pollute records.
        Set<String> layoutEndpointHosts = new HashSet<>();
        for (BlobLayout layout : bc.getLayout(null, Context.NONE)) {
            for (BlobLayoutRange range : layout.getBlobLayoutInfo().getRanges()) {
                String host = UrlBuilder.parse(range.getEndpoint()).getHost();
                if (host != null) {
                    layoutEndpointHosts.add(host.toLowerCase(Locale.ROOT));
                }
            }
        }
        for (RequestHostRecord record : rewrittenRecords) {
            assertTrue(layoutEndpointHosts.contains(record.requestHost.toLowerCase(Locale.ROOT)),
                "Chunk host '" + record.requestHost + "' not in layout endpoints " + layoutEndpointHosts
                    + ". Observed requests: " + records);
        }
    }

    /**
     * A blob below the size at which the service emits {@code x-ms-download-hint: layout} must not cause the SDK to
     * fetch a layout at all. The block size is deliberately smaller than the blob so the download is genuinely
     * chunked -- otherwise the layout would be skipped because no bytes remain after the initial chunk, and the test
     * would pass without ever exercising the download-hint check.
     */
    @LiveOnly
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void downloadToFileSmallBlobDoesNotFetchLayout() throws IOException {
        byte[] data = getRandomByteArray(NO_HINT_LIVE_CONTENT_LENGTH);
        BlobClient smallBlobClient = cc.getBlobClient(generateBlobName());
        smallBlobClient.getBlockBlobClient().upload(new java.io.ByteArrayInputStream(data), data.length, true);

        List<RequestHostRecord> records = new CopyOnWriteArrayList<>();
        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            smallBlobClient.getBlobUrl(), recordRequestHostsPolicy(records));

        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(
            () -> downloadClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(testFile.toString()).setParallelTransferOptions(
                    new ParallelTransferOptions().setBlockSizeLong((long) NO_HINT_LIVE_DOWNLOAD_BLOCK_SIZE)),
                null, null));

        URI accountUri = URI.create(smallBlobClient.getBlobUrl());
        String accountHost = accountUri.getHost();

        assertEquals(0, countLayoutRequests(records),
            "Small blob must not trigger comp=layout. Observed requests: " + records);
        assertTrue(getRewrittenRecords(records, accountHost).isEmpty(),
            "All requests must stay on the account host. Observed requests: " + records);
        for (RequestHostRecord record : records) {
            assertNull(record.hostHeader, "No Host header rewriting expected. Observed: " + record);
        }
        assertArrayEquals(data, Files.readAllBytes(testFile));
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

    @DoNotRecord
    @Test
    public void downloadStreamWithResponseUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobClient client = mockClient(httpClient);

        client.downloadStreamWithResponse(new ByteArrayOutputStream(),
            new BlobDownloadStreamOptions().setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT), null, Context.NONE);
        client.downloadStreamWithResponse(new ByteArrayOutputStream(), new BlobDownloadStreamOptions(), null,
            Context.NONE);

        assertRoutedThenUnrouted(httpClient);
    }

    @DoNotRecord
    @Test
    public void downloadStreamWithResponseWithRangeUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobClient client = mockClient(httpClient);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        client.downloadStreamWithResponse(stream, new BlobDownloadStreamOptions().setRange(new BlobRange(4, 10L))
            .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT), null, Context.NONE);

        assertRoutedRange(httpClient.getCaptured().get(0));
        assertArrayEquals(Arrays.copyOfRange(MOCK_BODY, 4, 14), stream.toByteArray());
    }

    @DoNotRecord
    @Test
    public void downloadContentWithResponseUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobClient client = mockClient(httpClient);

        client.downloadContentWithResponse(
            new BlobDownloadContentOptions().setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT), null, Context.NONE);
        client.downloadContentWithResponse(new BlobDownloadContentOptions(), null, Context.NONE);

        assertRoutedThenUnrouted(httpClient);
    }

    @DoNotRecord
    @Test
    public void downloadContentWithResponseWithRangeUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobClient client = mockClient(httpClient);

        byte[] content
            = client.downloadContentWithResponse(new BlobDownloadContentOptions().setRange(new BlobRange(4, 10L))
                .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT), null, Context.NONE).getValue().toBytes();

        assertRoutedRange(httpClient.getCaptured().get(0));
        assertArrayEquals(Arrays.copyOfRange(MOCK_BODY, 4, 14), content);
    }

    @DoNotRecord
    @Test
    public void asyncDownloadStreamWithResponseUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobAsyncClient client = mockAsyncClient(httpClient);

        drain(client.downloadStreamWithResponse(
            new BlobDownloadStreamOptions().setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT)));
        drain(client.downloadStreamWithResponse(new BlobDownloadStreamOptions()));

        assertRoutedThenUnrouted(httpClient);
    }

    @DoNotRecord
    @Test
    public void asyncDownloadContentWithResponseUsesDataLocalityEndpoint() {
        OneShotHttpClient httpClient = new OneShotHttpClient();
        BlobAsyncClient client = mockAsyncClient(httpClient);

        client
            .downloadContentWithResponse(
                new BlobDownloadContentOptions().setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT))
            .block();
        client.downloadContentWithResponse(new BlobDownloadContentOptions()).block();

        assertRoutedThenUnrouted(httpClient);
    }

    /**
     * A mid-stream failure is resumed by re-requesting the remaining bytes. That resumed request must stay on the
     * endpoint the caller selected, otherwise the tail of the download silently leaves the locality-optimal path.
     */
    @DoNotRecord
    @Test
    public void inStreamRetryStaysOnTheDataLocalityEndpoint() {
        TruncatedOneShotHttpClient httpClient = new TruncatedOneShotHttpClient();
        BlobAsyncClient client = mockAsyncClient(httpClient);

        byte[] content = FluxUtil.collectBytesInByteBufferStream(client
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT))
            .flatMapMany(BlobDownloadAsyncResponse::getValue)).block();

        assertArrayEquals(MOCK_BODY, content);

        assertEquals(2, httpClient.getCaptured().size(),
            "Expected the mid-stream failure to trigger exactly one resume.");
        for (OneShotRequestRecord request : httpClient.getCaptured()) {
            assertEquals(DATA_LOCALITY_HOST, request.urlHost);
            assertEquals(ORIGINAL_HOST, request.hostHeader);
        }

        // The resumed request asks only for the bytes that were never delivered.
        assertEquals("bytes=" + MOCK_FAILURE_OFFSET + "-" + (MOCK_BODY.length - 1),
            httpClient.getCaptured().get(1).rangeHeader);
    }

    private static void assertRoutedThenUnrouted(OneShotHttpClient httpClient) {
        assertEquals(2, httpClient.getCaptured().size());

        OneShotRequestRecord routed = httpClient.getCaptured().get(0);
        assertEquals(DATA_LOCALITY_HOST, routed.urlHost);
        assertEquals(ORIGINAL_HOST, routed.hostHeader);

        OneShotRequestRecord unrouted = httpClient.getCaptured().get(1);
        assertEquals(ORIGINAL_HOST, unrouted.urlHost);
        assertNull(unrouted.hostHeader);
    }

    private static void assertRoutedRange(OneShotRequestRecord routed) {
        assertEquals(DATA_LOCALITY_HOST, routed.urlHost);
        assertEquals(ORIGINAL_HOST, routed.hostHeader);
        assertEquals("bytes=4-13", routed.rangeHeader);
    }

    private static void drain(Mono<BlobDownloadAsyncResponse> response) {
        assertNotNull(
            FluxUtil.collectBytesInByteBufferStream(response.flatMapMany(BlobDownloadAsyncResponse::getValue)).block());
    }

    private static BlobClient mockClient(HttpClient httpClient) {
        return mockBuilder(httpClient).buildClient();
    }

    private static BlobAsyncClient mockAsyncClient(HttpClient httpClient) {
        return mockBuilder(httpClient).buildAsyncClient();
    }

    private static BlobClientBuilder mockBuilder(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://" + ORIGINAL_HOST)
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient);
    }

    private static HttpHeaders mockDownloadHeaders(int start, int end) {
        return new HttpHeaders().set(HttpHeaderName.ETAG, "\"etag\"")
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(end - start + 1))
            .set(HttpHeaderName.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + MOCK_BODY.length);
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

    private static final class LayoutRoutingHttpClient implements HttpClient {
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

    private static long countLayoutRequests(List<RequestHostRecord> records) {
        long count = 0;
        for (RequestHostRecord record : records) {
            if (isLayoutRequest(record)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts layout enumerations rather than layout requests. The service is permitted to paginate a layout, and every
     * continuation page is also a {@code comp=layout} request, so only the marker-less request that starts an
     * enumeration is counted.
     */
    private static long countLayoutEnumerations(List<RequestHostRecord> records) {
        long count = 0;
        for (RequestHostRecord record : records) {
            if (isLayoutRequest(record) && !record.requestUrl.contains("marker=")) {
                count++;
            }
        }
        return count;
    }

    private static boolean isLayoutRequest(RequestHostRecord record) {
        return record.requestUrl != null && record.requestUrl.contains("comp=layout");
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
                    context.getHttpRequest().getHeaders().getValue(HttpHeaderName.HOST),
                    context.getHttpRequest().getUrl().toString()));
            }
        };
    }

    private static final class RequestHostRecord {
        private final String requestHost;
        private final String hostHeader;
        private final String requestUrl;

        private RequestHostRecord(String requestHost, String hostHeader, String requestUrl) {
            this.requestHost = requestHost;
            this.hostHeader = hostHeader;
            this.requestUrl = requestUrl;
        }

        @Override
        public String toString() {
            return "RequestHostRecord{requestHost='" + requestHost + "', hostHeader='" + hostHeader + "', requestUrl='"
                + requestUrl + "'}";
        }
    }

    private static final class OneShotRequestRecord {
        private final String urlHost;
        private final String hostHeader;
        private final String rangeHeader;

        private OneShotRequestRecord(HttpRequest request) {
            this.urlHost = request.getUrl().getHost();
            this.hostHeader = request.getHeaders().getValue(HttpHeaderName.HOST);
            this.rangeHeader = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
        }
    }

    /**
     * Serves {@link #MOCK_BODY}, honoring the requested range, and records where each request was sent.
     */
    private static class OneShotHttpClient implements HttpClient {
        private final List<OneShotRequestRecord> captured = new ArrayList<>();

        List<OneShotRequestRecord> getCaptured() {
            return captured;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            OneShotRequestRecord record = new OneShotRequestRecord(request);
            captured.add(record);

            int start = 0;
            int end = MOCK_BODY.length - 1;
            if (record.rangeHeader != null) {
                String[] parts = record.rangeHeader.replace("bytes=", "").split("-");
                start = Integer.parseInt(parts[0]);
                end = parts.length > 1 ? Math.min(Integer.parseInt(parts[1]), end) : end;
            }

            byte[] body = Arrays.copyOfRange(MOCK_BODY, start, end + 1);
            return Mono.just(new MockHttpResponse(request, 206, mockDownloadHeaders(start, end), body));
        }
    }

    /**
     * Fails the first response body partway through with an {@link IOException}, which is the failure shape the
     * reliable download path resumes from. Subsequent requests are served normally.
     */
    private static final class TruncatedOneShotHttpClient extends OneShotHttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (getCaptured().isEmpty()) {
                getCaptured().add(new OneShotRequestRecord(request));
                return Mono.just(new TruncatedBodyResponse(request));
            }

            return super.send(request);
        }
    }

    private static final class TruncatedBodyResponse extends HttpResponse {
        private TruncatedBodyResponse(HttpRequest request) {
            super(request);
        }

        @Override
        public int getStatusCode() {
            return 206;
        }

        @Override
        public String getHeaderValue(String name) {
            return getHeaders().getValue(HttpHeaderName.fromString(name));
        }

        @Override
        public HttpHeaders getHeaders() {
            return mockDownloadHeaders(0, MOCK_BODY.length - 1);
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.concat(Flux.just(ByteBuffer.wrap(Arrays.copyOfRange(MOCK_BODY, 0, MOCK_FAILURE_OFFSET))),
                Flux.error(new IOException("Connection reset by peer")));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(getBody());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }
    }
}
