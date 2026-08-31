// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.models.LayoutAwareRouting;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.FileReadOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLakeDataLocalityOptionsTests {
    private static final String ORIGINAL_DFS_ENDPOINT = "https://account.dfs.core.windows.net/filesystem/path";
    private static final String ORIGINAL_BLOB_HOST = "account.blob.core.windows.net";
    private static final String DATA_LOCALITY_HOST = "other-host.blob.core.windows.net";
    private static final String DATA_LOCALITY_ENDPOINT = "https://" + DATA_LOCALITY_HOST;
    private static final byte[] DOWNLOAD_BODY = "test".getBytes(StandardCharsets.UTF_8);
    private static final String LAYOUT_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"3\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints></BlobLayout>";
    // The host advertised by LAYOUT_XML, which chunk requests are expected to be routed to.
    private static final String LAYOUT_ENDPOINT_HOST = "host-a";

    @Test
    public void fileReadOptionsRoundTrip() {
        FileRange range = new FileRange(10, 20L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(3);
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId("leaseId");
        FileReadOptions options = new FileReadOptions().setRange(range)
            .setDownloadRetryOptions(downloadRetryOptions)
            .setRequestConditions(requestConditions)
            .setRetrieveContentRangeMd5(true)
            .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT)
            .setUserPrincipalName(true);

        assertSame(range, options.getRange());
        assertSame(downloadRetryOptions, options.getDownloadRetryOptions());
        assertSame(requestConditions, options.getRequestConditions());
        assertTrue(options.isRetrieveContentRangeMd5());
        assertEquals(DATA_LOCALITY_ENDPOINT, options.getDataLocalityEndpoint());
        assertEquals(Boolean.TRUE, options.isUserPrincipalName());
    }

    @Test
    public void fileInputStreamOptionsTransformToBlobInputStreamOptions() {
        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions()
            .setLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.ENABLED);

        BlobInputStreamOptions blobOptions = Transforms.toBlobInputStreamOptions(options);

        assertEquals(LayoutAwareRouting.ENABLED, blobOptions.getLayoutAwareRouting());
        assertNull(Transforms.toBlobInputStreamOptions(null));
    }

    @Test
    public void toBlobLayoutAwareRouting() {
        assertNull(Transforms.toBlobLayoutAwareRouting(null));
        assertEquals(LayoutAwareRouting.AUTO,
            Transforms.toBlobLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.AUTO));
        assertEquals(LayoutAwareRouting.DISABLED,
            Transforms.toBlobLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.DISABLED));
        assertEquals(LayoutAwareRouting.ENABLED,
            Transforms.toBlobLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.ENABLED));
    }

    @Test
    public void fileReadOptionsTransformToBlobDownloadStreamOptions() {
        FileRange range = new FileRange(10, 20L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(3);
        OffsetDateTime ifModifiedSince = OffsetDateTime.of(2026, 8, 25, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime ifUnmodifiedSince = OffsetDateTime.of(2026, 8, 26, 0, 0, 0, 0, ZoneOffset.UTC);
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId("leaseId")
            .setIfMatch("\"match\"")
            .setIfNoneMatch("\"none\"")
            .setIfModifiedSince(ifModifiedSince)
            .setIfUnmodifiedSince(ifUnmodifiedSince);
        FileReadOptions options = new FileReadOptions().setRange(range)
            .setDownloadRetryOptions(downloadRetryOptions)
            .setRequestConditions(requestConditions)
            .setRetrieveContentRangeMd5(true)
            .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT);

        BlobDownloadStreamOptions blobOptions = Transforms.toBlobDownloadStreamOptions(options);

        assertEquals(range.getOffset(), blobOptions.getRange().getOffset());
        assertEquals(range.getCount(), blobOptions.getRange().getCount());
        assertEquals(downloadRetryOptions.getMaxRetryRequests(),
            blobOptions.getDownloadRetryOptions().getMaxRetryRequests());
        assertEquals(requestConditions.getLeaseId(), blobOptions.getRequestConditions().getLeaseId());
        assertEquals(requestConditions.getIfMatch(), blobOptions.getRequestConditions().getIfMatch());
        assertEquals(requestConditions.getIfNoneMatch(), blobOptions.getRequestConditions().getIfNoneMatch());
        assertEquals(requestConditions.getIfModifiedSince(), blobOptions.getRequestConditions().getIfModifiedSince());
        assertEquals(requestConditions.getIfUnmodifiedSince(),
            blobOptions.getRequestConditions().getIfUnmodifiedSince());
        assertTrue(blobOptions.isRetrieveContentRangeMd5());
        assertEquals(DATA_LOCALITY_ENDPOINT, blobOptions.getDataLocalityEndpoint());
        assertNull(Transforms.toBlobDownloadStreamOptions(null));
    }

    @Test
    public void fileReadWithResponseUsesDataLocalityEndpoint() {
        ReadHttpClient httpClient = new ReadHttpClient();
        DataLakeFileClient client = client(httpClient);

        client.readWithResponse(new ByteArrayOutputStream(),
            new FileReadOptions().setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))
                .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT),
            null, Context.NONE);
        client.readWithResponse(new ByteArrayOutputStream(),
            new FileReadOptions().setRange(new FileRange(0, (long) DOWNLOAD_BODY.length)), null, Context.NONE);

        CapturedRequest rewritten = httpClient.captured.get(0);
        assertEquals(DATA_LOCALITY_HOST, rewritten.urlHost);
        assertEquals(ORIGINAL_BLOB_HOST, rewritten.hostHeader);

        CapturedRequest unmodified = httpClient.captured.get(1);
        assertEquals(ORIGINAL_BLOB_HOST, unmodified.urlHost);
        assertNull(unmodified.hostHeader);
    }

    @Test
    public void fileAsyncReadWithResponseUsesDataLocalityEndpoint() {
        ReadHttpClient httpClient = new ReadHttpClient();
        DataLakeFileAsyncClient client = asyncClient(httpClient);

        client.readWithResponse(new FileReadOptions().setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))
            .setDataLocalityEndpoint(DATA_LOCALITY_ENDPOINT)).block();
        client.readWithResponse(new FileReadOptions().setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))).block();

        CapturedRequest rewritten = httpClient.captured.get(0);
        assertEquals(DATA_LOCALITY_HOST, rewritten.urlHost);
        assertEquals(ORIGINAL_BLOB_HOST, rewritten.hostHeader);

        CapturedRequest unmodified = httpClient.captured.get(1);
        assertEquals(ORIGINAL_BLOB_HOST, unmodified.urlHost);
        assertNull(unmodified.hostHeader);
    }

    @Test
    public void fileOpenInputStreamWithDisabledLayoutAwareRoutingDoesNotFetchLayout() throws IOException {
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient();
        DataLakeFileClient client = client(httpClient);

        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))
            .setLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.DISABLED);

        byte[] bytes;
        try (InputStream stream = client.openInputStream(options, Context.NONE).getInputStream()) {
            bytes = readAll(stream);
        }

        assertArrayEquals(DOWNLOAD_BODY, bytes);
        assertEquals(0, httpClient.getLayoutRequestCount());
        for (CapturedRequest request : httpClient.getDataRequestRecords()) {
            assertEquals(ORIGINAL_BLOB_HOST, request.urlHost);
            assertNull(request.hostHeader);
        }
    }

    @Test
    public void fileOpenInputStreamWithDefaultLayoutAwareRoutingRoutesChunks() throws IOException {
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient();
        DataLakeFileClient client = client(httpClient);

        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setRange(new FileRange(0, (long) DOWNLOAD_BODY.length));

        byte[] bytes;
        try (InputStream stream = client.openInputStream(options, Context.NONE).getInputStream()) {
            bytes = readAll(stream);
        }

        assertArrayEquals(DOWNLOAD_BODY, bytes);
        assertEquals(1, httpClient.getLayoutRequestCount());

        boolean routedRequestSeen = false;
        for (CapturedRequest request : httpClient.getDataRequestRecords()) {
            if (LAYOUT_ENDPOINT_HOST.equals(request.urlHost)) {
                routedRequestSeen = true;
                assertEquals(ORIGINAL_BLOB_HOST, request.hostHeader);
            }
        }

        assertTrue(routedRequestSeen);
    }

    @Test
    public void fileAsyncReadToFileWithDefaultLayoutAwareRoutingRoutesChunks() throws IOException {
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient();
        DataLakeFileAsyncClient client = asyncClient(httpClient);
        Path tempFile = Files.createTempFile("layout-routing", ".dat");
        Files.deleteIfExists(tempFile);

        try {
            assertDoesNotThrow(() -> client.readToFileWithResponse(new ReadToFileOptions(tempFile.toString())
                .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions().setBlockSizeLong(1L))
                .setRangeGetContentMd5(false)
                .setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))).block());

            assertArrayEquals(DOWNLOAD_BODY, Files.readAllBytes(tempFile));
            assertEquals(1, httpClient.getLayoutRequestCount());

            boolean routedRequestSeen = false;
            for (CapturedRequest request : httpClient.getDataRequestRecords()) {
                if (LAYOUT_ENDPOINT_HOST.equals(request.urlHost)) {
                    routedRequestSeen = true;
                    assertEquals(ORIGINAL_BLOB_HOST, request.hostHeader);
                }
            }

            assertTrue(routedRequestSeen);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void fileReadToFileWithDefaultLayoutAwareRoutingRoutesChunks() throws IOException {
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient();
        DataLakeFileClient client = client(httpClient);
        Path tempFile = Files.createTempFile("layout-routing", ".dat");
        Files.deleteIfExists(tempFile);

        try {
            assertDoesNotThrow(() -> client.readToFileWithResponse(new ReadToFileOptions(tempFile.toString())
                .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions().setBlockSizeLong(1L))
                .setRangeGetContentMd5(false)
                .setRange(new FileRange(0, (long) DOWNLOAD_BODY.length)), null, Context.NONE));

            assertArrayEquals(DOWNLOAD_BODY, Files.readAllBytes(tempFile));
            assertEquals(1, httpClient.getLayoutRequestCount());

            boolean routedRequestSeen = false;
            for (CapturedRequest request : httpClient.getDataRequestRecords()) {
                if (LAYOUT_ENDPOINT_HOST.equals(request.urlHost)) {
                    routedRequestSeen = true;
                    assertEquals(ORIGINAL_BLOB_HOST, request.hostHeader);
                }
            }

            assertTrue(routedRequestSeen);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void fileAsyncReadToFileWithDisabledLayoutAwareRoutingDoesNotFetchLayout() throws IOException {
        LayoutRoutingHttpClient httpClient = new LayoutRoutingHttpClient();
        DataLakeFileAsyncClient client = asyncClient(httpClient);
        Path tempFile = Files.createTempFile("layout-routing", ".dat");
        Files.deleteIfExists(tempFile);

        try {
            assertDoesNotThrow(() -> client.readToFileWithResponse(new ReadToFileOptions(tempFile.toString())
                .setParallelTransferOptions(new com.azure.storage.common.ParallelTransferOptions().setBlockSizeLong(1L))
                .setLayoutAwareRouting(com.azure.storage.file.datalake.models.LayoutAwareRouting.DISABLED)
                .setRangeGetContentMd5(false)
                .setRange(new FileRange(0, (long) DOWNLOAD_BODY.length))).block());

            assertArrayEquals(DOWNLOAD_BODY, Files.readAllBytes(tempFile));
            assertEquals(0, httpClient.getLayoutRequestCount());
            for (CapturedRequest request : httpClient.getDataRequestRecords()) {
                assertEquals(ORIGINAL_BLOB_HOST, request.urlHost);
                assertNull(request.hostHeader);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static DataLakeFileClient client(HttpClient httpClient) {
        return new DataLakePathClientBuilder().endpoint(ORIGINAL_DFS_ENDPOINT)
            .setAnonymousAccess()
            .httpClient(httpClient)
            .buildFileClient();
    }

    private static DataLakeFileAsyncClient asyncClient(HttpClient httpClient) {
        return new DataLakePathClientBuilder().endpoint(ORIGINAL_DFS_ENDPOINT)
            .setAnonymousAccess()
            .httpClient(httpClient)
            .buildFileAsyncClient();
    }

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static final class CapturedRequest {
        private final String url;
        private final String urlHost;
        private final String hostHeader;

        CapturedRequest(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.urlHost = request.getUrl().getHost();
            this.hostHeader = request.getHeaders().getValue(HttpHeaderName.HOST);
        }
    }

    private static final class ReadHttpClient implements HttpClient {
        private final List<CapturedRequest> captured = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            captured.add(new CapturedRequest(request));

            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(DOWNLOAD_BODY.length))
                    .set(HttpHeaderName.CONTENT_RANGE,
                        "bytes 0-" + (DOWNLOAD_BODY.length - 1) + "/" + DOWNLOAD_BODY.length)
                    .set(HttpHeaderName.ETAG, "\"etag\"");
            return Mono.just(new MockHttpResponse(request, 206, headers, DOWNLOAD_BODY));
        }
    }

    private static final class LayoutRoutingHttpClient implements HttpClient {
        private final List<CapturedRequest> captured = new ArrayList<>();
        private int layoutRequestCount;
        private int dataRequestCount;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            CapturedRequest capturedRequest = new CapturedRequest(request);
            captured.add(capturedRequest);

            if (capturedRequest.url.contains("comp=layout")) {
                layoutRequestCount++;
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                    .set(HttpHeaderName.ETAG, "\"layout-etag\"");
                return Mono
                    .just(new MockHttpResponse(request, 200, headers, LAYOUT_XML.getBytes(StandardCharsets.UTF_8)));
            }

            dataRequestCount++;
            String rangeHeader = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
            int start = 0;
            int end = DOWNLOAD_BODY.length - 1;
            if (rangeHeader != null) {
                String[] parts = rangeHeader.replace("bytes=", "").split("-");
                start = Integer.parseInt(parts[0]);
                end = parts.length > 1 ? Integer.parseInt(parts[1]) : end;
            }

            int normalizedEnd = Math.min(end, DOWNLOAD_BODY.length - 1);
            byte[] body = new byte[normalizedEnd - start + 1];
            System.arraycopy(DOWNLOAD_BODY, start, body, 0, body.length);

            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(body.length))
                .set(HttpHeaderName.CONTENT_RANGE, "bytes " + start + "-" + normalizedEnd + "/" + DOWNLOAD_BODY.length)
                .set(HttpHeaderName.ETAG, "\"etag\"");
            if (dataRequestCount == 1) {
                headers.set(HttpHeaderName.fromString("x-ms-download-hint"), "layout");
            }

            return Mono.just(new MockHttpResponse(request, 206, headers, body));
        }

        int getLayoutRequestCount() {
            return layoutRequestCount;
        }

        List<CapturedRequest> getDataRequestRecords() {
            List<CapturedRequest> dataRequests = new ArrayList<>();
            for (CapturedRequest request : captured) {
                if (!request.url.contains("comp=layout")) {
                    dataRequests.add(request);
                }
            }
            return dataRequests;
        }

        List<CapturedRequest> getCapturedRequests() {
            return captured;
        }
    }
}
