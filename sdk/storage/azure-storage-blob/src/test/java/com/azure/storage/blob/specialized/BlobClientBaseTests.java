// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.implementation.util.BlobLayoutCacheValue;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.DataLocalityPolicy;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobClientBaseTests extends BlobTestBase {
    private static final String FIRST_PAGE_ETAG = "\"0x8DFIRSTPAGE\"";
    private static final String SECOND_PAGE_ETAG = "\"0x8DSECONDPAGE\"";
    private static final String NEXT_MARKER = "page-two";
    private static final String LEASE_ID = "lease-id";
    private static final String IF_NONE_MATCH = "\"caller-none-match\"";
    private static final OffsetDateTime IF_UNMODIFIED_SINCE
        = OffsetDateTime.of(2026, 8, 25, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final String CALLER_CONTEXT_KEY = "caller-context-key";
    private static final String CALLER_CONTEXT_VALUE = "caller-context-value";
    private static final String ORIGINAL_HOST = "account.blob.core.windows.net";
    private static final String SEEKABLE_LAYOUT_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" />"
        + "<Range Start=\"100\" End=\"199\" EndpointIndex=\"1\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" />"
        + "<Endpoint Index=\"1\" Value=\"https://host-b:443\" /></Endpoints></BlobLayout>";
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayout() {
        Iterator<BlobLayout> iterator = bc.getLayout(null, Context.NONE).iterator();

        assertTrue(iterator.hasNext());
        BlobLayout layout = iterator.next();
        assertNotNull(layout.getRanges());
        assertFalse(layout.getRanges().isEmpty());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutEmptyBlob() {
        BlobClient emptyBlob = cc.getBlobClient(generateBlobName());
        emptyBlob.getBlockBlobClient().commitBlockList(new ArrayList<>());

        assertDoesNotThrow(() -> emptyBlob.getLayout(null, Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutRange() {
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);

        assertDoesNotThrow(
            () -> bc.getLayout(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB)), Context.NONE)
                .stream()
                .count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
    @Test
    public void getLayoutContinuationToken() {
        Iterator<PagedResponse<BlobLayout>> iterator = bc.getLayout(null, Context.NONE).iterableByPage(1).iterator();
        String token = iterator.next().getContinuationToken();

        assertDoesNotThrow(() -> bc.getLayout(null, Context.NONE).iterableByPage(token).iterator().hasNext());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2026-10-06")
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

    @DoNotRecord
    @Test
    public void getLayoutIndependentEnumerationsUseTheirOwnFirstPageETag() {
        LayoutPagesHttpClient httpClient = new LayoutPagesHttpClient(true, true);
        BlobClient client = client(httpClient);
        PagedIterable<BlobLayout> layouts = client.getLayout(null);
        Iterator<PagedResponse<BlobLayout>> firstEnumeration = layouts.iterableByPage().iterator();
        Iterator<PagedResponse<BlobLayout>> secondEnumeration = layouts.iterableByPage().iterator();

        firstEnumeration.next();
        secondEnumeration.next();
        firstEnumeration.next();
        secondEnumeration.next();

        assertEquals(4, httpClient.captured.size());
        assertNull(httpClient.captured.get(0).ifMatch);
        assertNull(httpClient.captured.get(1).ifMatch);
        assertEquals(FIRST_PAGE_ETAG, httpClient.captured.get(2).ifMatch);
        assertEquals(SECOND_PAGE_ETAG, httpClient.captured.get(3).ifMatch);
    }

    @DoNotRecord
    @Test
    public void fetchLayoutCacheValueSyncSinglePageAccumulatesRanges() {
        BlobLayoutCacheValue value = layoutClient(new LayoutPagesHttpClient(false))
            .fetchLayoutCacheValueSync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE);

        assertNotNull(value.getRanges());
        assertEquals(1, value.getRanges().size());
        assertEquals("https://host-a:443", value.getRanges().get(0).getEndpoint());
    }

    @DoNotRecord
    @Test
    public void fetchLayoutCacheValueSyncMultiplePagesAccumulatesRanges() {
        BlobLayoutCacheValue value = layoutClient(new LayoutPagesHttpClient(true))
            .fetchLayoutCacheValueSync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE);

        assertNotNull(value.getRanges());
        assertEquals(2, value.getRanges().size());
        assertEquals("https://host-a:443", value.getRanges().get(0).getEndpoint());
        assertEquals("https://host-b:443", value.getRanges().get(1).getEndpoint());
    }

    @DoNotRecord
    @Test
    public void fetchLayoutCacheValueSyncEmptyLayoutReturnsEmptyRanges() {
        BlobLayoutCacheValue value = layoutClient(new EmptyLayoutHttpClient())
            .fetchLayoutCacheValueSync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE);

        assertNotNull(value.getRanges());
        assertTrue(value.getRanges().isEmpty());
    }

    @DoNotRecord
    @ParameterizedTest
    @MethodSource("fatalLayoutStatusSupplier")
    public void fetchLayoutCacheValueSyncFatalStatusesPropagate(int statusCode) {
        assertThrows(BlobStorageException.class, () -> layoutClient(new ErrorLayoutHttpClient(statusCode))
            .fetchLayoutCacheValueSync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE));
    }

    @DoNotRecord
    @ParameterizedTest
    @MethodSource("nonFatalLayoutStatusSupplier")
    public void fetchLayoutCacheValueSyncNonFatalStatusesFallback(int statusCode) {
        BlobLayoutCacheValue value = layoutClient(new ErrorLayoutHttpClient(statusCode))
            .fetchLayoutCacheValueSync(new BlobRange(0, 200L), new BlobRequestConditions(), Context.NONE);

        assertNull(value.getRanges());
    }

    @DoNotRecord
    @Test
    public void openInputStreamWithDefaultDataLocalityUsesSyncLayoutProvider() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 200, SEEKABLE_LAYOUT_XML);
        BlobClient downloadClient = seekableClient(httpClient, records);

        BlobInputStreamOptions options
            = new BlobInputStreamOptions().setBlockSize(8).setRange(new BlobRange(0, (long) contentBytes.length));

        byte[] readBytes;
        try (InputStream is
            = downloadClient.openInputStream(options, new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE))) {
            readBytes = readAll(is);
        }

        LayoutRequestRecord layoutRequest = records.stream()
            .filter(record -> record.requestUrl.contains("comp=layout"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a layout request."));
        assertTrue(layoutRequest.syncCall, "Expected the layout request to use the synchronous pipeline.");
        assertEquals(CALLER_CONTEXT_VALUE, layoutRequest.callerContextValue);
        assertArrayEquals(contentBytes, readBytes);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadWithDefaultDataLocalityCachesLayoutForRepeatedReads() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 200, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);

            channel.position(10);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[10], buffer.get());

            buffer.clear();
            channel.position(18);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[18], buffer.get());
        }

        assertEquals(1, httpClient.getLayoutRequestCount());
        assertEquals("host-a", httpClient.getDataRequestRecords().get(1).requestHost);
        assertEquals("host-a", httpClient.getDataRequestRecords().get(2).requestHost);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(1).hostHeader);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(2).hostHeader);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadWithDefaultDataLocalityRoutesDifferentRangesToDifferentEndpoints()
        throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 200, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);

            channel.position(10);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[10], buffer.get());

            buffer.clear();
            channel.position(150);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[150], buffer.get());
        }

        assertEquals(1, httpClient.getLayoutRequestCount());
        assertEquals("host-a", httpClient.getDataRequestRecords().get(1).requestHost);
        assertEquals("host-b", httpClient.getDataRequestRecords().get(2).requestHost);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(1).hostHeader);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(2).hostHeader);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadWithDefaultDataLocalityRoutesBackwardSeekCorrectly() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 200, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);

            channel.position(150);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[150], buffer.get());

            buffer.clear();
            channel.position(10);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[10], buffer.get());
        }

        assertEquals(1, httpClient.getLayoutRequestCount());
        assertEquals("host-b", httpClient.getDataRequestRecords().get(1).requestHost);
        assertEquals("host-a", httpClient.getDataRequestRecords().get(2).requestHost);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(1).hostHeader);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(2).hostHeader);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadWithoutLayoutHintDoesNotFetchLayout() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, false, 200, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);

            channel.position(10);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[10], buffer.get());

            buffer.clear();
            channel.position(150);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[150], buffer.get());
        }

        assertEquals(0, httpClient.getLayoutRequestCount());
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(1).requestHost);
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(2).requestHost);
        assertNull(httpClient.getDataRequestRecords().get(1).hostHeader);
        assertNull(httpClient.getDataRequestRecords().get(2).hostHeader);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadWithSoftFailedLayoutFallsBackToOriginalEndpoint() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 500, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);

            channel.position(10);
            assertEquals(1, channel.read(buffer));
            buffer.flip();
            assertEquals(contentBytes[10], buffer.get());
        }

        assertEquals(1, httpClient.getLayoutRequestCount());
        assertEquals(ORIGINAL_HOST, httpClient.getDataRequestRecords().get(1).requestHost);
        assertNull(httpClient.getDataRequestRecords().get(1).hostHeader);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadPreservesCallerContextAfterEndpointAugmentation() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 200, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            channel.position(10);
            assertEquals(1, channel.read(buffer));
        }

        LayoutRequestRecord dataRequest = records.stream()
            .filter(record -> !record.requestUrl.contains("comp=layout"))
            .skip(1)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a data request after layout lookup."));
        assertEquals(CALLER_CONTEXT_VALUE, dataRequest.callerContextValue);
        assertEquals("https://host-a:443", dataRequest.layoutEndpointValue);
    }

    @DoNotRecord
    @Test
    public void openSeekableByteChannelReadPropagatesFatalLayoutFetchErrors() throws IOException {
        byte[] contentBytes = createTestContent();
        List<LayoutRequestRecord> records = new ArrayList<>();
        SeekableReadHttpClient httpClient = new SeekableReadHttpClient(contentBytes, true, 403, SEEKABLE_LAYOUT_XML);
        BlobClient client = seekableClient(httpClient, records);

        BlobSeekableByteChannelReadResult result
            = client.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(8),
                new Context(CALLER_CONTEXT_KEY, CALLER_CONTEXT_VALUE));

        try (SeekableByteChannel channel = result.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            channel.position(10);
            assertThrows(BlobStorageException.class, () -> channel.read(buffer));
        }
    }

    private static BlobClient client(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .buildClient();
    }

    private static BlobClientBase layoutClient(HttpClient httpClient) {
        return client(httpClient);
    }

    private static BlobClient seekableClient(SeekableReadHttpClient httpClient, List<LayoutRequestRecord> records) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient)
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(recordLayoutRequestThreadsPolicy(records))
            .buildClient();
    }

    private static Stream<Arguments> fatalLayoutStatusSupplier() {
        return Stream.of(Arguments.of(403), Arguments.of(404), Arguments.of(409), Arguments.of(412));
    }

    private static Stream<Arguments> nonFatalLayoutStatusSupplier() {
        return Stream.of(Arguments.of(500), Arguments.of(429));
    }

    private static HttpPipelinePolicy recordLayoutRequestThreadsPolicy(List<LayoutRequestRecord> records) {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                recordRequest(context, false);
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                recordRequest(context, true);
                return next.processSync();
            }

            private void recordRequest(HttpPipelineCallContext context, boolean syncCall) {
                String requestUrl = context.getHttpRequest().getUrl().toString();
                records.add(new LayoutRequestRecord(requestUrl, syncCall,
                    context.getData(CALLER_CONTEXT_KEY).map(Object::toString).orElse(null),
                    context.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).map(Object::toString).orElse(null)));
            }
        };
    }

    private byte[] createTestContent() {
        byte[] content = getRandomByteArray(2 * Constants.KB);
        return content;
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

    private static final class LayoutRequestRecord {
        private final String requestUrl;
        private final boolean syncCall;
        private final String callerContextValue;
        private final String layoutEndpointValue;

        private LayoutRequestRecord(String requestUrl, boolean syncCall, String callerContextValue,
            String layoutEndpointValue) {
            this.requestUrl = requestUrl;
            this.syncCall = syncCall;
            this.callerContextValue = callerContextValue;
            this.layoutEndpointValue = layoutEndpointValue;
        }
    }

    private static final class SeekableRequestRecord {
        private final String url;
        private final String requestHost;
        private final String hostHeader;
        private final String rangeHeader;

        private SeekableRequestRecord(HttpRequest request) {
            this.url = request.getUrl().toString();
            this.requestHost = request.getUrl().getHost();
            this.hostHeader = request.getHeaders().getValue(HttpHeaderName.HOST);
            this.rangeHeader = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
        }
    }

    private static final class EmptyLayoutHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.just(new MockHttpResponse(request, 204));
        }
    }

    private static final class ErrorLayoutHttpClient implements HttpClient {
        private final int statusCode;

        private ErrorLayoutHttpClient(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.just(new MockHttpResponse(request, statusCode));
        }
    }

    private static final class LayoutPagesHttpClient implements HttpClient {
        private final boolean includeContinuation;
        private final boolean distinctFirstPageETags;
        private final List<CapturedRequest> captured = new ArrayList<>();
        private int firstPageRequests;

        LayoutPagesHttpClient(boolean includeContinuation) {
            this(includeContinuation, false);
        }

        LayoutPagesHttpClient(boolean includeContinuation, boolean distinctFirstPageETags) {
            this.includeContinuation = includeContinuation;
            this.distinctFirstPageETags = distinctFirstPageETags;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            CapturedRequest capturedRequest = new CapturedRequest(request);
            captured.add(capturedRequest);

            boolean isFirstPage = !capturedRequest.url.contains("marker=");
            String body = isFirstPage ? includeContinuation ? FIRST_PAGE : SINGLE_PAGE : SECOND_PAGE;
            String eTag = isFirstPage ? getFirstPageETag() : SECOND_PAGE_ETAG;
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.ETAG, eTag).set(HttpHeaderName.CONTENT_TYPE, "application/xml");

            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        }

        private String getFirstPageETag() {
            if (!distinctFirstPageETags) {
                return FIRST_PAGE_ETAG;
            }

            firstPageRequests++;
            return firstPageRequests == 1 ? FIRST_PAGE_ETAG : SECOND_PAGE_ETAG;
        }
    }

    private static final class SeekableReadHttpClient implements HttpClient {
        private final byte[] contentBytes;
        private final boolean includeLayoutHint;
        private final int layoutStatusCode;
        private final String layoutXml;
        private final List<SeekableRequestRecord> dataRequestRecords = new ArrayList<>();
        private final List<SeekableRequestRecord> layoutRequestRecords = new ArrayList<>();

        private SeekableReadHttpClient(byte[] contentBytes, boolean includeLayoutHint, int layoutStatusCode,
            String layoutXml) {
            this.contentBytes = contentBytes;
            this.includeLayoutHint = includeLayoutHint;
            this.layoutStatusCode = layoutStatusCode;
            this.layoutXml = layoutXml;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            SeekableRequestRecord record = new SeekableRequestRecord(request);
            if (record.url.contains("comp=layout")) {
                layoutRequestRecords.add(record);
                return Mono.just(createLayoutResponse(request));
            }

            dataRequestRecords.add(record);
            return Mono.just(createDataResponse(request));
        }

        int getLayoutRequestCount() {
            return layoutRequestRecords.size();
        }

        List<SeekableRequestRecord> getDataRequestRecords() {
            return dataRequestRecords;
        }

        private HttpResponse createLayoutResponse(HttpRequest request) {
            if (layoutStatusCode != 200) {
                return new MockHttpResponse(request, layoutStatusCode);
            }

            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                .set(HttpHeaderName.ETAG, "\"layout-etag\"");
            return new MockHttpResponse(request, layoutStatusCode, headers, layoutXml.getBytes(StandardCharsets.UTF_8));
        }

        private HttpResponse createDataResponse(HttpRequest request) {
            String range = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
            long start = 0L;
            long end = contentBytes.length - 1L;
            if (range != null) {
                String[] parts = range.replace("bytes=", "").split("-");
                start = Long.parseLong(parts[0]);
                end = parts.length > 1 ? Long.parseLong(parts[1]) : end;
            }

            int startIndex = (int) start;
            int endIndex = (int) Math.min(end, contentBytes.length - 1L);
            byte[] body = Arrays.copyOfRange(contentBytes, startIndex, endIndex + 1);
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_RANGE,
                "bytes " + startIndex + "-" + endIndex + "/" + contentBytes.length);
            if (includeLayoutHint && startIndex == 0) {
                headers.set(HttpHeaderName.fromString("x-ms-download-hint"), "layout");
            }

            return new MockHttpResponse(request, 206, headers, body);
        }
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
}
