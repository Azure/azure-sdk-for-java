// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.models.BlobLayout;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpoints;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpointsEndpointItem;
import com.azure.storage.blob.implementation.models.BlobLayoutRanges;
import com.azure.storage.blob.implementation.models.BlobLayoutRangesRangeItem;
import com.azure.storage.blob.models.DownloadHint;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlobDataLocalityWiringTests {
    private static final String ORIGINAL_HOST = "fakeaccount.blob.core.windows.net";
    private static final int HTTPS_PORT = 443;
    private static final String ORIGINAL_AUTHORITY = ORIGINAL_HOST + ":" + HTTPS_PORT;
    private static final String ENDPOINT = "https://" + ORIGINAL_AUTHORITY;
    private static final long BLOCK_SIZE = 20;
    private static final int BLOB_LENGTH = 100;
    private static final String RFC_1123_DATE = "Wed, 21 Oct 2015 07:28:00 GMT";
    private static final String ETAG = "\"0x8DB3\"";
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final HttpHeaderName X_MS_BLOB_TYPE = HttpHeaderName.fromString("x-ms-blob-type");
    private static final HttpHeaderName X_MS_CREATION_TIME = HttpHeaderName.fromString("x-ms-creation-time");
    private static final HttpHeaderName X_MS_LEASE_STATE = HttpHeaderName.fromString("x-ms-lease-state");
    private static final HttpHeaderName X_MS_LEASE_STATUS = HttpHeaderName.fromString("x-ms-lease-status");
    private static final HttpHeaderName X_MS_SERVER_ENCRYPTED = HttpHeaderName.fromString("x-ms-server-encrypted");
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    private static final HttpHeaderName X_MS_DOWNLOAD_HINT = HttpHeaderName.fromString("x-ms-download-hint");

    private static final byte[] BLOB_CONTENT = createBlobContent();

    private Path downloadedFile;

    @AfterEach
    public void cleanup() throws IOException {
        if (downloadedFile != null) {
            Files.deleteIfExists(downloadedFile);
        }
    }

    @Test
    public void dataLocalityRoutesChunksToLayoutEndpoints() throws IOException {
        DataLocalityTestClient httpClient = new DataLocalityTestClient(true, false);

        downloadAndAssertContent(httpClient, true);

        assertEquals(1, httpClient.getLayoutRequestCount());
        Map<Long, DownloadRequestCapture> captures = capturesByStart(httpClient.getDownloadRequests());
        assertEquals(5, captures.size());

        assertOriginalAccount(captures.get(0L));
        assertRouted(captures.get(20L), "host-a");
        assertRouted(captures.get(40L), "host-a");
        assertRouted(captures.get(60L), "host-b");
        assertRouted(captures.get(80L), "host-b");
    }

    @Test
    public void dataLocalityDisabledRoutesNoChunks() throws IOException {
        DataLocalityTestClient httpClient = new DataLocalityTestClient(true, false);

        downloadAndAssertContent(httpClient, false);

        assertEquals(0, httpClient.getLayoutRequestCount());
        assertAllDownloadsUsedOriginalAccount(httpClient.getDownloadRequests());
    }

    @Test
    public void dataLocalityNoDownloadHintSkipsLayout() throws IOException {
        DataLocalityTestClient httpClient = new DataLocalityTestClient(false, false);

        downloadAndAssertContent(httpClient, true);

        assertEquals(0, httpClient.getLayoutRequestCount());
        assertAllDownloadsUsedOriginalAccount(httpClient.getDownloadRequests());
    }

    @Test
    public void dataLocalityGetLayoutFailureDownloadStillSucceeds() throws IOException {
        DataLocalityTestClient httpClient = new DataLocalityTestClient(true, true);

        downloadAndAssertContent(httpClient, true);

        assertEquals(1, httpClient.getLayoutRequestCount());
        assertAllDownloadsUsedOriginalAccount(httpClient.getDownloadRequests());
    }

    private void downloadAndAssertContent(DataLocalityTestClient httpClient, boolean enableDataLocality)
        throws IOException {
        BlobClient blobClient = new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("fakeaccount", Base64.getEncoder().encodeToString(new byte[32])))
            .httpClient(httpClient)
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 1, 10, 1L, 1L, null))
            .buildClient();

        Path targetDirectory = Paths.get("target");
        Files.createDirectories(targetDirectory);
        downloadedFile = Files.createTempFile(targetDirectory, "BlobDataLocalityWiringTests", ".bin");
        Files.delete(downloadedFile);

        BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadedFile.toString())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(BLOCK_SIZE));
        if (enableDataLocality) {
            options.setEnableDataLocality(true);
        }

        assertDoesNotThrow(() -> blobClient.downloadToFileWithResponse(options, Duration.ofSeconds(30), Context.NONE));
        assertArrayEquals(BLOB_CONTENT, Files.readAllBytes(downloadedFile));
    }

    private static Map<Long, DownloadRequestCapture> capturesByStart(List<DownloadRequestCapture> captures) {
        Map<Long, DownloadRequestCapture> capturesByStart = new HashMap<>();
        for (DownloadRequestCapture capture : captures) {
            capturesByStart.put(capture.start, capture);
        }
        return capturesByStart;
    }

    private static void assertAllDownloadsUsedOriginalAccount(List<DownloadRequestCapture> captures) {
        assertEquals(5, captures.size());
        for (DownloadRequestCapture capture : captures) {
            assertOriginalAccount(capture);
        }
    }

    private static void assertOriginalAccount(DownloadRequestCapture capture) {
        assertEquals(ORIGINAL_HOST, capture.url.getHost());
        assertEquals(HTTPS_PORT, capture.url.getPort());
        assertFalse("host-a".equals(capture.url.getHost()));
        assertFalse("host-b".equals(capture.url.getHost()));
    }

    private static void assertRouted(DownloadRequestCapture capture, String expectedHost) {
        assertEquals(expectedHost, capture.url.getHost());
        assertEquals(HTTPS_PORT, capture.url.getPort());
        assertEquals(ORIGINAL_AUTHORITY, capture.hostHeader);
    }

    private static byte[] createBlobContent() {
        byte[] content = new byte[BLOB_LENGTH];
        for (int i = 0; i < BLOB_LENGTH; i++) {
            content[i] = (byte) i;
        }
        return content;
    }

    private static byte[] serializeLayout() {
        BlobLayoutEndpointsEndpointItem endpointA
            = new BlobLayoutEndpointsEndpointItem().setIndex(0).setValue("https://host-a:443");
        BlobLayoutEndpointsEndpointItem endpointB
            = new BlobLayoutEndpointsEndpointItem().setIndex(1).setValue("https://host-b:443");
        BlobLayoutEndpoints endpoints = new BlobLayoutEndpoints().setEndpoint(Arrays.asList(endpointA, endpointB));
        BlobLayoutRangesRangeItem rangeA = new BlobLayoutRangesRangeItem().setStart(0).setEnd(49).setEndpointIndex(0);
        BlobLayoutRangesRangeItem rangeB = new BlobLayoutRangesRangeItem().setStart(50).setEnd(99).setEndpointIndex(1);
        BlobLayoutRanges ranges = new BlobLayoutRanges().setRange(Arrays.asList(rangeA, rangeB));
        BlobLayout layout = new BlobLayout().setRanges(ranges).setEndpoints(endpoints);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XmlWriter xmlWriter = XmlWriter.toStream(outputStream)) {
            layout.toXml(xmlWriter).flush();
            return outputStream.toByteArray();
        } catch (IOException | XMLStreamException ex) {
            throw new IllegalStateException("Failed to serialize blob layout.", ex);
        }
    }

    private static final class DataLocalityTestClient implements HttpClient {
        private final boolean includeDownloadHint;
        private final boolean failGetLayout;
        private final AtomicInteger layoutRequestCount = new AtomicInteger();
        private final List<DownloadRequestCapture> downloadRequests = new CopyOnWriteArrayList<>();
        private final byte[] layoutBody = serializeLayout();

        private DataLocalityTestClient(boolean includeDownloadHint, boolean failGetLayout) {
            this.includeDownloadHint = includeDownloadHint;
            this.failGetLayout = failGetLayout;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            try {
                if (request.getHttpMethod() != HttpMethod.GET) {
                    return Mono.error(new IllegalStateException("Unexpected HTTP method: " + request.getHttpMethod()));
                }

                String query = request.getUrl().getQuery();
                if (query != null && query.contains("comp=layout")) {
                    return Mono.just(handleGetLayout(request));
                }

                return Mono.just(handleDownload(request));
            } catch (RuntimeException ex) {
                return Mono.error(ex);
            }
        }

        private HttpResponse handleGetLayout(HttpRequest request) {
            layoutRequestCount.incrementAndGet();
            if (failGetLayout) {
                byte[] errorBody = "<Error><Code>InternalError</Code><Message>layout failed</Message></Error>"
                    .getBytes(StandardCharsets.UTF_8);
                return new MockHttpResponse(request, 500, createCommonHeaders(errorBody.length, "application/xml"),
                    errorBody);
            }

            return new MockHttpResponse(request, 200, createCommonHeaders(layoutBody.length, "application/xml"),
                layoutBody);
        }

        private HttpResponse handleDownload(HttpRequest request) {
            Range range = parseRange(request.getHeaders().getValue(X_MS_RANGE));
            downloadRequests.add(new DownloadRequestCapture(range.start, range.end, request.getUrl(),
                request.getHeaders().getValue(HttpHeaderName.HOST)));

            byte[] body = Arrays.copyOfRange(BLOB_CONTENT, (int) range.start, (int) range.end + 1);
            HttpHeaders headers = createCommonHeaders(body.length, "application/octet-stream")
                .set(HttpHeaderName.CONTENT_RANGE, String.format("bytes %d-%d/%d", range.start, range.end, BLOB_LENGTH))
                .set(X_MS_BLOB_TYPE, "BlockBlob")
                .set(X_MS_CREATION_TIME, RFC_1123_DATE)
                .set(HttpHeaderName.ACCEPT_RANGES, "bytes")
                .set(X_MS_LEASE_STATUS, "unlocked")
                .set(X_MS_LEASE_STATE, "available")
                .set(X_MS_SERVER_ENCRYPTED, "false");
            if (includeDownloadHint) {
                headers.set(X_MS_DOWNLOAD_HINT, DownloadHint.LAYOUT.toString());
            }

            return new MockHttpResponse(request, 206, headers, body);
        }

        private int getLayoutRequestCount() {
            return layoutRequestCount.get();
        }

        private List<DownloadRequestCapture> getDownloadRequests() {
            return Collections.unmodifiableList(downloadRequests);
        }
    }

    private static HttpHeaders createCommonHeaders(int contentLength, String contentType) {
        return new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength))
            .set(HttpHeaderName.CONTENT_TYPE, contentType)
            .set(HttpHeaderName.ETAG, ETAG)
            .set(HttpHeaderName.LAST_MODIFIED, RFC_1123_DATE)
            .set(HttpHeaderName.DATE, RFC_1123_DATE)
            .set(HttpHeaderName.X_MS_REQUEST_ID, "request-id")
            .set(X_MS_VERSION, "2027-03-07");
    }

    private static Range parseRange(String rangeHeader) {
        if (rangeHeader == null) {
            return new Range(0, BLOB_LENGTH - 1);
        }

        Matcher matcher = RANGE_PATTERN.matcher(rangeHeader);
        if (!matcher.matches()) {
            throw new IllegalStateException("Unexpected range header: " + rangeHeader);
        }

        return new Range(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)));
    }

    private static final class DownloadRequestCapture {
        private final long start;
        private final long end;
        private final URL url;
        private final String hostHeader;

        private DownloadRequestCapture(long start, long end, URL url, String hostHeader) {
            this.start = start;
            this.end = end;
            this.url = url;
            this.hostHeader = hostHeader;
        }
    }

    private static final class Range {
        private final long start;
        private final long end;

        private Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
