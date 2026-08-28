// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.DataLakeFileLayoutInfo;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLakeFileLayoutPaginationTests {
    private static final String NEXT_MARKER = "page-two";
    private static final String FIRST_PAGE_ETAG = "\"0x8DFIRSTPAGE\"";
    private static final long FILE_SIZE = 200;
    private static final String FILE_CONTENT_TYPE = "application/octet-stream";
    private static final HttpHeaderName X_MS_BLOB_CONTENT_LENGTH
        = HttpHeaderName.fromString("x-ms-blob-content-length");
    private static final HttpHeaderName X_MS_BLOB_CONTENT_TYPE = HttpHeaderName.fromString("x-ms-blob-content-type");
    private static final HttpHeaderName X_MS_ACCESS_TIER = HttpHeaderName.fromString("x-ms-access-tier");
    private static final HttpHeaderName X_MS_ACCESS_TIER_INFERRED
        = HttpHeaderName.fromString("x-ms-access-tier-inferred");
    private static final HttpHeaderName X_MS_SMART_ACCESS_TIER = HttpHeaderName.fromString("x-ms-smart-access-tier");
    private static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-encryption-scope");
    private static final String ENCRYPTION_SCOPE = "layout-scope";
    private static final String FIRST_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints><NextMarker>" + NEXT_MARKER
        + "</NextMarker></BlobLayout>";
    private static final String SECOND_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"100\" End=\"199\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-b:443\" /></Endpoints></BlobLayout>";

    @Test
    @DoNotRecord
    public void asyncGetLayoutRetrievesOnePageAtATimeAndLocksContinuation() {
        LayoutHttpClient httpClient = new LayoutHttpClient();
        DataLakeFileAsyncClient client = asyncClient(httpClient);

        List<PagedResponse<DataLakeFileLayoutInfo>> pages = new ArrayList<>();
        client.getLayout(null).byPage().doOnNext(pages::add).blockLast();

        assertEquals(2, pages.size());
        assertEquals(2, httpClient.requests.size());
        assertNull(httpClient.requests.get(0).getHeaders().getValue(HttpHeaderName.IF_MATCH));
        assertEquals(FIRST_PAGE_ETAG, httpClient.requests.get(1).getHeaders().getValue(HttpHeaderName.IF_MATCH));
        assertTrue(httpClient.requests.get(1).getUrl().toString().contains("marker=" + NEXT_MARKER));
        assertFirstPageMetadataPreserved(pages);
    }

    @Test
    @DoNotRecord
    public void syncGetLayoutRetrievesOnePageAtATime() {
        LayoutHttpClient httpClient = new LayoutHttpClient();
        DataLakeFileClient client = syncClient(httpClient);

        List<PagedResponse<DataLakeFileLayoutInfo>> pages = new ArrayList<>();
        for (PagedResponse<DataLakeFileLayoutInfo> page : client.getLayout(null).iterableByPage()) {
            pages.add(page);
            assertEquals(pages.size(), httpClient.requests.size());
        }

        assertEquals(2, pages.size());
        assertFirstPageMetadataPreserved(pages);
    }

    private static void assertFirstPageMetadataPreserved(List<PagedResponse<DataLakeFileLayoutInfo>> pages) {
        DataLakeFileLayoutInfo firstLayout = pages.get(0).getValue().get(0);
        assertEquals(1, firstLayout.getRanges().size());
        assertNotNull(firstLayout.getPathProperties());
        assertNotNull(firstLayout.getPathProperties().getETag());
        assertEquals(FILE_SIZE, firstLayout.getPathProperties().getFileSize());
        assertEquals(FILE_CONTENT_TYPE, firstLayout.getPathProperties().getContentType());
        assertEquals(AccessTier.SMART, firstLayout.getPathProperties().getAccessTier());
        assertEquals(AccessTier.HOT, firstLayout.getPathProperties().getSmartAccessTier());
        assertEquals(true, firstLayout.getPathProperties().isAccessTierInferred());
        assertEquals(ENCRYPTION_SCOPE, firstLayout.getPathProperties().getEncryptionScope());
    }

    private static DataLakeFileAsyncClient asyncClient(HttpClient httpClient) {
        return new DataLakePathClientBuilder().endpoint("https://account.dfs.core.windows.net/filesystem/path")
            .setAnonymousAccess()
            .httpClient(httpClient)
            .buildFileAsyncClient();
    }

    private static DataLakeFileClient syncClient(HttpClient httpClient) {
        return new DataLakePathClientBuilder().endpoint("https://account.dfs.core.windows.net/filesystem/path")
            .setAnonymousAccess()
            .httpClient(httpClient)
            .buildFileClient();
    }

    private static final class LayoutHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            requests.add(request);
            boolean firstPage = requests.size() == 1;
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                .set(X_MS_BLOB_CONTENT_LENGTH, String.valueOf(FILE_SIZE))
                .set(X_MS_BLOB_CONTENT_TYPE, FILE_CONTENT_TYPE)
                .set(X_MS_ACCESS_TIER, "Smart")
                .set(X_MS_ACCESS_TIER_INFERRED, "true")
                .set(X_MS_SMART_ACCESS_TIER, "Hot")
                .set(X_MS_ENCRYPTION_SCOPE, ENCRYPTION_SCOPE);
            if (firstPage) {
                headers.set(HttpHeaderName.ETAG, FIRST_PAGE_ETAG);
            }
            String body = firstPage ? FIRST_PAGE : SECOND_PAGE;
            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
