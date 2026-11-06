// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ParallelDocumentQueryExecutionContextTest {
    private static final long TIMEOUT = 10_000;
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(5);
    private static final FeedRangeEpkImpl LEFT = new FeedRangeEpkImpl(new Range<>("", "80", true, false));
    private static final FeedRangeEpkImpl RIGHT = new FeedRangeEpkImpl(new Range<>("80", "FF", true, false));

    @SuppressWarnings("unchecked")
    private final DocumentProducer<Document> producer = mock(DocumentProducer.class);
    private final Map<String, ClientSideRequestStatistics> requestStatisticsByPage = new HashMap<>();

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void pageWithBackendContinuationIsEmittedWithoutFetchingTheNextPage() {
        AtomicInteger nextPageSubscriptions = new AtomicInteger();
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux
            .just(page("A", LEFT, "left-1", "a"))
            .concatWith(Flux.defer(() -> {
                nextPageSubscriptions.incrementAndGet();
                return Flux.never();
            }));

        StepVerifier.create(source.transformDeferred(newTransformer()).next())
            .assertNext(page -> assertThat(page.getContinuationToken()).isEqualTo(continuation("left-1", LEFT)))
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        assertThat(nextPageSubscriptions.get()).isZero();
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void pageThatExhaustsItsRangeResumesAtTheRangeOfTheNextNonEmptyPage() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("A", LEFT, "left-1", "a"),
            page("B", LEFT, null, "b"),
            page("C", RIGHT, "right-1"),
            page("D", RIGHT, "right-2", "c"),
            page("E", RIGHT, null, "d"));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(ids(pages)).containsExactly("a", "b", "c", "d");
        assertThat(pages.stream().map(FeedResponse::getContinuationToken).collect(Collectors.toList()))
            .containsExactly(
                continuation("left-1", LEFT),
                continuation(null, RIGHT),
                continuation("right-2", RIGHT),
                null);
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void lastNonEmptyPageHasNoContinuationWhenRemainingRangesAreEmpty() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("A", LEFT, null, "a"),
            page("B", RIGHT, null));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(ids(pages)).containsExactly("a");
        assertThat(pages.get(0).getContinuationToken()).isNull();
        assertDiagnosticsOf(pages.get(0), "A", "B");
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void emptyResultProducesSingleEmptyPageWithTheDiagnosticsOfEveryRequest() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("A", LEFT, null),
            page("B", RIGHT, null));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).getResults()).isEmpty();
        assertThat(pages.get(0).getContinuationToken()).isNull();
        assertDiagnosticsOf(pages.get(0), "A", "B");
    }

    /**
     * The query returns its last document before the backend finishes scanning: page A has documents and a
     * continuation token, and the pages that follow are empty. A is emitted without waiting for them, and their
     * diagnostics are attached to A once the query completes.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void diagnosticsOfTrailingEmptyPagesAreAttachedToTheLastEmittedPage() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("A", LEFT, "left-1", "a"),
            page("B", LEFT, "left-2"),
            page("C", LEFT, null));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(pages).hasSize(1);
        assertThat(ids(pages)).containsExactly("a");
        assertThat(pages.get(0).getContinuationToken()).isEqualTo(continuation("left-1", LEFT));
        assertDiagnosticsOf(pages.get(0), "A", "B", "C");
    }

    /**
     * A caller resuming from page A's continuation token receives the trailing empty pages as a single empty page
     * that carries their diagnostics.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void resumingAfterTheLastDocumentReturnsAnEmptyPageWithTheDiagnosticsOfTheTrailingPages() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("B", LEFT, "left-2"),
            page("C", LEFT, null));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).getResults()).isEmpty();
        assertThat(pages.get(0).getContinuationToken()).isNull();
        assertDiagnosticsOf(pages.get(0), "B", "C");
    }

    /**
     * Empty pages between two emitted pages are attributed to the next emitted page: the page before them has already
     * been emitted when they arrive.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void diagnosticsOfEmptyPagesBetweenEmittedPagesAreAttachedToTheNextEmittedPage() {
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(
            page("A", LEFT, "left-1", "a"),
            page("B", LEFT, null),
            page("C", RIGHT, null, "c"));

        List<FeedResponse<Document>> pages = source.transformDeferred(newTransformer()).collectList().block();

        assertThat(ids(pages)).containsExactly("a", "c");
        assertDiagnosticsOf(pages.get(0), "A");
        assertDiagnosticsOf(pages.get(1), "B", "C");
    }

    private static ParallelDocumentQueryExecutionContext.EmptyPagesFilterTransformer<Document> newTransformer() {
        return new ParallelDocumentQueryExecutionContext.EmptyPagesFilterTransformer<>(
            new RequestChargeTracker(),
            new CosmosQueryRequestOptions(),
            UUID.randomUUID(),
            () -> "test");
    }

    /**
     * Creates a backend page whose diagnostics hold one query metrics entry and one request statistics entry, both
     * identified by {@code label}.
     */
    private DocumentProducer<Document>.DocumentProducerFeedResponse page(
        String label,
        FeedRangeEpkImpl range,
        String backendContinuation,
        String... ids) {

        List<Document> documents = Arrays.stream(ids)
            .map(ParallelDocumentQueryExecutionContextTest::document)
            .collect(Collectors.toList());

        ConcurrentMap<String, QueryMetrics> queryMetrics = new ConcurrentHashMap<>();
        queryMetrics.put(label, QueryMetrics.createFromCollection(Collections.emptyList()));
        CosmosDiagnostics diagnostics = BridgeInternal.createCosmosDiagnostics(queryMetrics);
        ClientSideRequestStatistics requestStatistics = new ClientSideRequestStatistics(mock(DiagnosticsClientContext.class));
        diagnosticsAccessor().getFeedResponseDiagnostics(diagnostics)
            .addClientSideRequestStatistics(Collections.singletonList(requestStatistics));
        requestStatisticsByPage.put(label, requestStatistics);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONTINUATION, backendContinuation);
        FeedResponse<Document> feedResponse = BridgeInternal.createFeedResponseWithQueryMetrics(
            documents, headers, queryMetrics, null, false, false, diagnostics);
        return producer.new DocumentProducerFeedResponse(feedResponse, range);
    }

    private void assertDiagnosticsOf(FeedResponse<Document> page, String... labels) {
        assertThat(BridgeInternal.queryMetricsFromFeedResponse(page).keySet())
            .as("query metrics")
            .containsExactlyInAnyOrder(labels);

        Collection<ClientSideRequestStatistics> requestStatistics =
            diagnosticsAccessor().getClientSideRequestStatistics(page.getCosmosDiagnostics());
        assertThat(requestStatistics)
            .as("request statistics")
            .containsExactlyInAnyOrderElementsOf(
                Arrays.stream(labels).map(requestStatisticsByPage::get).collect(Collectors.toList()));
    }

    private static ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor() {
        return ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    }

    private static Document document(String id) {
        Document document = new Document();
        document.setId(id);
        return document;
    }

    private static String continuation(String backendContinuation, FeedRangeEpkImpl range) {
        return new CompositeContinuationToken(backendContinuation, range.getRange()).toJson();
    }

    private static List<String> ids(List<FeedResponse<Document>> pages) {
        return pages.stream()
            .flatMap(page -> page.getResults().stream())
            .map(Document::getId)
            .collect(Collectors.toList());
    }
}
