// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import reactor.core.publisher.Mono;

public class BlobClientBaseTests extends BlobTestBase {
    private static final String FIRST_PAGE_ETAG = "\"0x8DFIRSTPAGE\"";
    private static final String SECOND_PAGE_ETAG = "\"0x8DSECONDPAGE\"";
    private static final String NEXT_MARKER = "page-two";
    private static final String LEASE_ID = "lease-id";
    private static final String IF_NONE_MATCH = "\"caller-none-match\"";
    private static final OffsetDateTime IF_UNMODIFIED_SINCE
        = OffsetDateTime.of(2026, 8, 25, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final String FIRST_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints>"
        + "<NextMarker>page-two</NextMarker></BlobLayout>";
    private static final String SINGLE_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints></BlobLayout>";
    private static final String SECOND_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"100\" End=\"199\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-b:443\" /></Endpoints></BlobLayout>";

    private BlobClient bc;

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
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        Iterator<BlobLayout> iterator = bc.getLayout(null, Context.NONE).iterator();

        assertTrue(iterator.hasNext());
        BlobLayout layout = iterator.next();
        assertNotNull(layout.getRanges());
        assertFalse(layout.getRanges().isEmpty());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyBlob() {
        BlobClient emptyBlob = cc.getBlobClient(generateBlobName());
        emptyBlob.getBlockBlobClient().commitBlockList(new ArrayList<>());

        assertDoesNotThrow(() -> emptyBlob.getLayout(null, Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);

        assertDoesNotThrow(
            () -> bc.getLayout(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB)), Context.NONE)
                .stream()
                .count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        Iterator<PagedResponse<BlobLayout>> iterator = bc.getLayout(null, Context.NONE).iterableByPage(1).iterator();
        int pageCount = 0;

        while (iterator.hasNext()) {
            PagedResponse<BlobLayout> page = iterator.next();
            assertTrue(page.getValue().size() <= 1);
            pageCount++;
        }

        assertTrue(pageCount > 0);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Iterator<PagedResponse<BlobLayout>> iterator = bc.getLayout(null, Context.NONE).iterableByPage(1).iterator();
        String token = iterator.next().getContinuationToken();

        assertDoesNotThrow(() -> bc.getLayout(null, Context.NONE).iterableByPage(token).iterator().hasNext());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertDoesNotThrow(
            () -> bc.getLayout(new BlobGetLayoutOptions().setRequestConditions(bac), Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getLayoutACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class,
            () -> bc.getLayout(new BlobGetLayoutOptions().setRequestConditions(bac), Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutError() {
        BlobClient blobClient = cc.getBlobClient(generateBlobName());

        assertThrows(BlobStorageException.class, () -> blobClient.getLayout(null, Context.NONE).stream().count());
    }

    @DoNotRecord
    @Test
    public void getLayoutContinuationUsesFirstPageETag() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobClient client = client(httpClient);

        assertEquals(2, client.getLayout(null).stream().count());

        assertEquals(2, httpClient.captured.size());
        CapturedRequest first = httpClient.captured.get(0);
        CapturedRequest second = httpClient.captured.get(1);
        assertNull(first.ifMatch);
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);
        assertTrue(second.url.contains("marker=" + NEXT_MARKER), "Expected continuation marker in: " + second.url);
    }

    @DoNotRecord
    @Test
    public void getLayoutContinuationPreservesOtherConditions() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobClient client = client(httpClient);
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(LEASE_ID)
            .setIfNoneMatch(IF_NONE_MATCH)
            .setIfUnmodifiedSince(IF_UNMODIFIED_SINCE);

        assertEquals(2,
            client.getLayout(new BlobGetLayoutOptions().setRequestConditions(requestConditions), Context.NONE)
                .stream()
                .count());

        CapturedRequest second = httpClient.captured.get(1);
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);
        assertEquals(LEASE_ID, second.leaseId);
        assertEquals(IF_NONE_MATCH, second.ifNoneMatch);
        assertEquals(DateTimeRfc1123.toRfc1123String(IF_UNMODIFIED_SINCE), second.ifUnmodifiedSince);
    }

    @DoNotRecord
    @Test
    public void getLayoutSinglePageDoesNotSendIfMatch() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(false);
        BlobClient client = client(httpClient);

        assertEquals(1, client.getLayout(null).stream().count());

        assertEquals(1, httpClient.captured.size());
        assertNull(httpClient.captured.get(0).ifMatch);
    }

    private static BlobClient client(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .buildClient();
    }

    private static final class CapturedRequest {
        private final String url;
        private final String ifMatch;
        private final String ifNoneMatch;
        private final String ifUnmodifiedSince;
        private final String leaseId;

        CapturedRequest(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.ifMatch = request.getHeaders().getValue(HttpHeaderName.IF_MATCH);
            this.ifNoneMatch = request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH);
            this.ifUnmodifiedSince = request.getHeaders().getValue(HttpHeaderName.IF_UNMODIFIED_SINCE);
            this.leaseId = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-lease-id"));
        }
    }

    private static final class LayoutPagesHttpClient implements HttpClient {
        private final boolean includeContinuation;
        private final List<CapturedRequest> captured = new ArrayList<>();

        LayoutPagesHttpClient(boolean includeContinuation) {
            this.includeContinuation = includeContinuation;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            captured.add(new CapturedRequest(request));

            boolean isFirstPage = captured.size() == 1;
            String body = isFirstPage ? includeContinuation ? FIRST_PAGE : SINGLE_PAGE : SECOND_PAGE;
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.ETAG, isFirstPage ? FIRST_PAGE_ETAG : SECOND_PAGE_ETAG)
                    .set(HttpHeaderName.CONTENT_TYPE, "application/xml");

            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
