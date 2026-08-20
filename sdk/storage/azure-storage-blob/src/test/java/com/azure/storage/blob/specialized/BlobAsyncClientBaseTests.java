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
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.implementation.util.BlobLayoutCacheValue;
import com.azure.storage.blob.models.BlobLayoutInfo;
import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        StepVerifier.create(bc.getLayoutWithResponse(null).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            BlobLayoutInfo info = r.get(0);
            assertNotNull(info.getETag());
            assertFalse(info.getETag().isEmpty());
            assertEquals(DATA.getDefaultDataSize(), info.getBlobContentLength());
            assertEquals(BlobType.BLOCK_BLOB, info.getBlobType());
            assertNotNull(info.getLastModified());
            assertNotNull(info.getCreatedOn());
            assertTrue(info.isServerEncrypted());
            assertEquals(LeaseStatusType.UNLOCKED, info.getLeaseStatus());
            assertEquals(LeaseStateType.AVAILABLE, info.getLeaseState());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyBlob() {
        BlobAsyncClient emptyBlob = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(emptyBlob.getBlockBlobAsyncClient()
            .commitBlockList(new ArrayList<>())
            .thenMany(emptyBlob.getLayoutWithResponse(null))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        StepVerifier.create(bc.getBlockBlobAsyncClient()
            .upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .thenMany(
                bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB))))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        StepVerifier.create(bc.getLayoutWithResponse(null).byPage(1).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            r.forEach(page -> assertTrue(page.getValue().size() <= 1));
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Flux<PagedResponse<BlobLayoutInfo>> response = bc.getLayoutWithResponse(null)
            .byPage(1)
            .next()
            .flatMapMany(r -> bc.getLayoutWithResponse(null).byPage(r.getContinuationToken()));

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Flux<BlobLayoutInfo> response = bc.setTags(t)
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
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
 * Verifies that paginated {@code getLayout} calls issued while setting up a locality-aware download follow the
 * service contract: subsequent pages are requested with {@code If-Match} set to the ETag returned by the first page
 * and with the same range as the initial layout call.
 */
class BlobAsyncClientBaseLayoutPaginationTests {
    private static final String FIRST_PAGE_ETAG = "\"0x8DFIRSTPAGE\"";
    private static final String SECOND_PAGE_ETAG = "\"0x8DSECONDPAGE\"";

    // ScrubEtagPolicy removes the quotes from response ETag headers, so this is the value the SDK reads from
    // the first page and sends back as If-Match.
    private static final String FIRST_PAGE_ETAG_SCRUBBED = "0x8DFIRSTPAGE";

    private static final String FIRST_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints>"
        + "<NextMarker>page-two</NextMarker></BlobLayout>";

    private static final String SECOND_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"100\" End=\"199\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-b:443\" /></Endpoints>" + "</BlobLayout>";

    @Test
    public void subsequentPagesReuseInitialETagAndRange() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient();
        BlobAsyncClientBase client = client(httpClient);

        BlobLayoutCacheValue value
            = client.fetchLayoutCacheValueAsync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE)
                .block();

        assertNotNull(value);
        List<BlobLayoutRange> ranges = value.getRanges();
        assertEquals(2, ranges.size());
        assertEquals("https://host-a:443", ranges.get(0).getEndpoint());
        assertEquals("https://host-b:443", ranges.get(1).getEndpoint());

        assertEquals(2, httpClient.captured.size());
        CapturedRequest first = httpClient.captured.get(0);
        CapturedRequest second = httpClient.captured.get(1);

        // The first call must not be conditioned on a layout ETag it has not seen yet.
        assertNull(first.ifMatch);
        assertEquals(FIRST_PAGE_ETAG_SCRUBBED, second.ifMatch);

        // The range must stay identical across pages so the service returns a consistent layout.
        assertEquals(first.range, second.range);
        assertNotNull(first.range);

        assertTrue(second.url.contains("marker=page-two"), "Expected the continuation marker, but was: " + second.url);
    }

    private static BlobAsyncClientBase client(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .buildAsyncClient();
    }

    private static final class CapturedRequest {
        private final String url;
        private final String ifMatch;
        private final String range;

        CapturedRequest(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.ifMatch = request.getHeaders().getValue(HttpHeaderName.IF_MATCH);
            this.range = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
        }
    }

    private static final class LayoutPagesHttpClient implements HttpClient {
        private final List<CapturedRequest> captured = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            captured.add(new CapturedRequest(request));

            boolean isFirstPage = captured.size() == 1;
            String body = isFirstPage ? FIRST_PAGE : SECOND_PAGE;
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.ETAG, isFirstPage ? FIRST_PAGE_ETAG : SECOND_PAGE_ETAG)
                    .set(HttpHeaderName.CONTENT_TYPE, "application/xml");

            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
