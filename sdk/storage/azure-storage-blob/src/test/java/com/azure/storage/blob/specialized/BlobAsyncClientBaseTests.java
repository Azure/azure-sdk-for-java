// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.implementation.util.BlobLayoutCacheValue;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobAsyncClientBaseTests extends BlobTestBase {
    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayout() {
        StepVerifier.create(bc.getLayoutWithResponse(null).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            BlobLayout layout = r.get(0);
            assertNotNull(layout.getBlobProperties());
            assertFalse(layout.getRanges().isEmpty());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutEmptyBlob() {
        BlobAsyncClient emptyBlob = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(emptyBlob.getBlockBlobAsyncClient()
            .commitBlockList(new ArrayList<>())
            .thenMany(emptyBlob.getLayoutWithResponse(null))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutRange() {
        StepVerifier.create(bc.getBlockBlobAsyncClient()
            .upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .thenMany(
                bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB))))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutPageSize() {
        StepVerifier.create(bc.getLayoutWithResponse(null).byPage(1).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            r.forEach(page -> assertTrue(page.getValue().size() <= 1));
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutContinuationToken() {
        Flux<PagedResponse<BlobLayout>> response = bc.getLayoutWithResponse(null)
            .byPage(1)
            .next()
            .flatMapMany(r -> bc.getLayoutWithResponse(null).byPage(r.getContinuationToken()));

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Flux<BlobLayout> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match),
                BlobTestBase::convertNulls))
            .flatMapMany(conditions -> {
                BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(conditions.get(0))
                    .setIfMatch(conditions.get(1))
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRequestConditions(bac));
            });

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getLayoutACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Mono<Long> response
            = Mono
                .zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, noneMatch),
                    BlobTestBase::convertNulls)
                .flatMap(conditions -> {
                    BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(conditions.get(0))
                        .setIfMatch(match)
                        .setIfNoneMatch(conditions.get(1))
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified)
                        .setTagsConditions(tags);

                    return bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRequestConditions(bac)).count();
                });

        StepVerifier.create(response).verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutError() {
        BlobAsyncClient blobClient = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(blobClient.getLayoutWithResponse(null)).verifyError(BlobStorageException.class);
    }
}

class BlobAsyncClientBaseLayoutFailureTests {

    @ParameterizedTest
    @ValueSource(ints = { 400, 500, 503, 599, 401, 429 })
    public void nonFatalStatusesFallBackToTheOriginalEndpoint(int statusCode) {
        BlobStorageException exception = exception(statusCode);

        assertNull(Objects.requireNonNull(BlobAsyncClientBase.handleLayoutFetchError(exception).block()).getRanges());
    }

    @ParameterizedTest
    @ValueSource(ints = { 403, 404, 409, 412 })
    public void fatalStatusesFailTheDownload(int statusCode) {
        BlobStorageException exception = exception(statusCode);

        assertThrows(BlobStorageException.class, () -> BlobAsyncClientBase.handleLayoutFetchError(exception).block());
    }

    private static BlobStorageException exception(int statusCode) {
        return new BlobStorageException("layout failure", new MockHttpResponse(null, statusCode), null);
    }
}

/**
 * Verifies layout pagination for both the public {@code getLayoutWithResponse} paged API and the internal
 * {@code fetchLayoutCacheValueAsync} path used when setting up a locality-aware download. Continuation pages must
 * carry {@code If-Match} with the first page's ETag, preserve the caller's other request conditions, reuse the
 * initial range, and a single-page layout must not send {@code If-Match}.
 */
class BlobAsyncClientBaseLayoutPaginationTests {
    private static final String FIRST_PAGE_ETAG = "\"0x8DFIRSTPAGE\"";
    private static final String SECOND_PAGE_ETAG = "\"0x8DSECONDPAGE\"";
    private static final String THIRD_PAGE_ETAG = "\"0x8DTHIRDPAGE\"";
    private static final long LAYOUT_BLOB_SIZE = 300;
    private static final String LAYOUT_BLOB_CONTENT_TYPE = "application/octet-stream";
    private static final HttpHeaderName X_MS_BLOB_CONTENT_LENGTH
        = HttpHeaderName.fromString("x-ms-blob-content-length");
    private static final HttpHeaderName X_MS_BLOB_CONTENT_TYPE = HttpHeaderName.fromString("x-ms-blob-content-type");
    private static final String FIRST_PAGE_MARKER = "service-page-one";
    private static final String NEXT_MARKER = "page-two";
    private static final String FINAL_MARKER = "page-three";
    private static final int REQUESTED_PAGE_SIZE = 3;
    private static final int SERVICE_MAX_RESULTS = 7;
    private static final String LEASE_ID = "lease-id";
    private static final String IF_NONE_MATCH = "\"caller-none-match\"";
    private static final OffsetDateTime IF_UNMODIFIED_SINCE
        = OffsetDateTime.of(2026, 8, 25, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String FIRST_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints><Marker>" + FIRST_PAGE_MARKER
        + "</Marker><NextMarker>" + NEXT_MARKER + "</NextMarker><MaxResults>" + SERVICE_MAX_RESULTS
        + "</MaxResults></BlobLayout>";

    private static final String SINGLE_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints></BlobLayout>";

    private static final String SECOND_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"100\" End=\"199\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-b:443\" /></Endpoints><Marker>" + NEXT_MARKER
        + "</Marker><MaxResults>" + SERVICE_MAX_RESULTS + "</MaxResults></BlobLayout>";

    private static final String RESUMED_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"100\" End=\"199\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-b:443\" /></Endpoints>" + "<NextMarker>" + FINAL_MARKER
        + "</NextMarker></BlobLayout>";

    private static final String FINAL_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"200\" End=\"299\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-c:443\" /></Endpoints>" + "</BlobLayout>";

    private static BlobAsyncClient client(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .buildAsyncClient();
    }

    @Test
    public void getLayoutContinuationUsesFirstPageETag() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobAsyncClient client = client(httpClient);

        StepVerifier.create(client.getLayoutWithResponse(null).collectList())
            .assertNext(layouts -> assertEquals(2, layouts.size()))
            .verifyComplete();

        assertEquals(2, httpClient.captured.size());
        CapturedRequest first = httpClient.captured.get(0);
        CapturedRequest second = httpClient.captured.get(1);
        assertNull(first.ifMatch);
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);
        assertTrue(second.url.contains("marker=" + NEXT_MARKER), "Expected continuation marker in: " + second.url);
    }

    @Test
    public void getLayoutPopulatesPagingFieldsFromResponse() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobAsyncClient client = client(httpClient);

        StepVerifier.create(client.getLayoutWithResponse(null).byPage(REQUESTED_PAGE_SIZE).next())
            .assertNext(firstPage -> {
                BlobLayout layout = firstPage.getValue().get(0);

                assertTrue(httpClient.captured.get(0).url.contains("maxresults=" + REQUESTED_PAGE_SIZE));
                assertEquals(FIRST_PAGE_MARKER, layout.getMarker());
                assertEquals(NEXT_MARKER, layout.getNextMarker());
                assertEquals(SERVICE_MAX_RESULTS, layout.getMaxResults());
                assertEquals(NEXT_MARKER, firstPage.getContinuationToken());
                assertNotNull(layout.getBlobProperties());
                assertNotNull(layout.getBlobProperties().getETag());
                assertEquals(LAYOUT_BLOB_SIZE, layout.getBlobProperties().getBlobSize());
                assertEquals(LAYOUT_BLOB_CONTENT_TYPE, layout.getBlobProperties().getContentType());
            })
            .verifyComplete();
    }

    @Test
    public void getLayoutLeavesOmittedPagingFieldsNull() {
        BlobAsyncClient client = client(new LayoutPagesHttpClient(false));

        StepVerifier.create(client.getLayoutWithResponse(null).byPage(REQUESTED_PAGE_SIZE).next())
            .assertNext(firstPage -> {
                BlobLayout layout = firstPage.getValue().get(0);

                assertNull(layout.getMarker());
                assertNull(layout.getNextMarker());
                assertNull(layout.getMaxResults());
                assertNotNull(layout.getBlobProperties());
            })
            .verifyComplete();
    }

    @Test
    public void getLayoutContinuationPreservesOtherConditions() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobAsyncClient client = client(httpClient);
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(LEASE_ID)
            .setIfNoneMatch(IF_NONE_MATCH)
            .setIfUnmodifiedSince(IF_UNMODIFIED_SINCE);

        StepVerifier.create(client
            .getLayoutWithResponse(new BlobGetLayoutOptions().setRequestConditions(requestConditions), Context.NONE)
            .then()).verifyComplete();

        CapturedRequest second = httpClient.captured.get(1);
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);
        assertEquals(LEASE_ID, second.leaseId);
        assertEquals(IF_NONE_MATCH, second.ifNoneMatch);
        assertEquals(DateTimeRfc1123.toRfc1123String(IF_UNMODIFIED_SINCE), second.ifUnmodifiedSince);
    }

    @Test
    public void getLayoutSinglePageDoesNotSendIfMatch() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(false);
        BlobAsyncClient client = client(httpClient);

        StepVerifier.create(client.getLayoutWithResponse(null).collectList())
            .assertNext(layouts -> assertEquals(1, layouts.size()))
            .verifyComplete();

        assertEquals(1, httpClient.captured.size());
        assertNull(httpClient.captured.get(0).ifMatch);
    }

    @Test
    public void getLayoutResumedEnumerationLocksContinuationPagesToItsFirstPageETag() {
        ResumedLayoutPagesHttpClient httpClient = new ResumedLayoutPagesHttpClient();
        BlobAsyncClient client = client(httpClient);

        StepVerifier.create(client.getLayoutWithResponse(null).byPage(NEXT_MARKER).collectList())
            .assertNext(pages -> assertEquals(2, pages.size()))
            .verifyComplete();

        assertEquals(2, httpClient.captured.size());
        CapturedRequest resumed = httpClient.captured.get(0);
        CapturedRequest continuation = httpClient.captured.get(1);

        // There is nothing to lock to on the resumed page: the caller's marker came from an earlier enumeration
        // whose ETag this client never observed.
        assertTrue(resumed.url.contains("marker=" + NEXT_MARKER), "Expected the resumed marker in: " + resumed.url);
        assertNull(resumed.ifMatch);

        // Every later page of the resumed enumeration is pinned to the first page that enumeration did observe.
        assertTrue(continuation.url.contains("marker=" + FINAL_MARKER),
            "Expected the continuation marker in: " + continuation.url);
        assertEquals(SECOND_PAGE_ETAG, continuation.ifMatch);
    }

    @Test
    public void subsequentPagesReuseInitialETagAndRange() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobAsyncClientBase client = client(httpClient);

        BlobLayoutCacheValue value
            = client.fetchLayoutCacheValueAsync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE)
                .block();

        assertNotNull(value);
        List<BlobLayoutRange> ranges = Objects.requireNonNull(value.getRanges());
        assertEquals(2, ranges.size());
        assertEquals("https://host-a:443", ranges.get(0).getEndpoint());
        assertEquals("https://host-b:443", ranges.get(1).getEndpoint());

        assertEquals(2, httpClient.captured.size());
        CapturedRequest first = httpClient.captured.get(0);
        CapturedRequest second = httpClient.captured.get(1);

        // The first call must not be conditioned on a layout ETag it has not seen yet.
        assertNull(first.ifMatch);
        // ScrubEtagPolicy strips quotes from the response ETag, and the SDK re-adds them per RFC 9110 before
        // sending If-Match on continuation pages.
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);

        // The range must stay identical across pages so the service returns a consistent layout.
        assertEquals(first.range, second.range);
        assertNotNull(first.range);

        assertTrue(second.url.contains("marker=" + NEXT_MARKER),
            "Expected the continuation marker, but was: " + second.url);
    }

    @Test
    public void getLayoutWithoutRangeSendsNoRangeHeaderOnAnyPage() {
        // Contract: when the caller sets no range, continuation pages must also omit x-ms-range.
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true);
        BlobAsyncClient client = client(httpClient);

        StepVerifier.create(client.getLayoutWithResponse(null).collectList())
            .assertNext(layouts -> assertEquals(2, layouts.size()))
            .verifyComplete();

        assertEquals(2, httpClient.captured.size());
        CapturedRequest first = httpClient.captured.get(0);
        CapturedRequest second = httpClient.captured.get(1);
        assertNull(first.range, "Initial page must not send x-ms-range when no range was set");
        assertNull(second.range, "Continuation page must not send x-ms-range when no range was set");
        assertNull(first.ifMatch);
        assertEquals(FIRST_PAGE_ETAG, second.ifMatch);
    }

    private static final class CapturedRequest {
        private final String url;
        private final String ifMatch;
        private final String ifNoneMatch;
        private final String ifUnmodifiedSince;
        private final String leaseId;
        private final String range;

        CapturedRequest(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.ifMatch = request.getHeaders().getValue(HttpHeaderName.IF_MATCH);
            this.ifNoneMatch = request.getHeaders().getValue(HttpHeaderName.IF_NONE_MATCH);
            this.ifUnmodifiedSince = request.getHeaders().getValue(HttpHeaderName.IF_UNMODIFIED_SINCE);
            this.leaseId = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-lease-id"));
            this.range = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
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
            String body = isFirstPage ? (includeContinuation ? FIRST_PAGE : SINGLE_PAGE) : SECOND_PAGE;
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.ETAG, isFirstPage ? FIRST_PAGE_ETAG : SECOND_PAGE_ETAG)
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(bodyBytes.length))
                    .set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                    .set(X_MS_BLOB_CONTENT_LENGTH, String.valueOf(LAYOUT_BLOB_SIZE))
                    .set(X_MS_BLOB_CONTENT_TYPE, LAYOUT_BLOB_CONTENT_TYPE);

            return Mono.just(new MockHttpResponse(request, 200, headers, bodyBytes));
        }
    }

    /**
     * Serves an enumeration that a caller resumed from {@link #NEXT_MARKER}. The resumed page carries its own ETag
     * and a further continuation marker, so the enumeration has both a first page and a continuation page even
     * though it never requested the layout's first page.
     */
    private static final class ResumedLayoutPagesHttpClient implements HttpClient {
        private final List<CapturedRequest> captured = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            CapturedRequest capturedRequest = new CapturedRequest(request);
            captured.add(capturedRequest);

            boolean isResumedPage = capturedRequest.url.contains("marker=" + NEXT_MARKER);
            String body = isResumedPage ? RESUMED_PAGE : FINAL_PAGE;
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.ETAG, isResumedPage ? SECOND_PAGE_ETAG : THIRD_PAGE_ETAG)
                    .set(HttpHeaderName.CONTENT_TYPE, "application/xml");

            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
