// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.PageRetriever;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.util.FluxUtil.withContext;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link PagedFlux}
 */
public class PagedFluxTest {
    private static final int DEFAULT_PAGE_COUNT = 4;

    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    @Test
    public void testEmptyResults() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(0);
        StepVerifier.create(pagedFlux).verifyComplete();
        StepVerifier.create(pagedFlux.byPage()).verifyComplete();
        StepVerifier.create(pagedFlux.byPage(null)).verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToItems() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux)
            .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPagedFluxConverter() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);

        StepVerifier.create(pagedFlux.mapPage(String::valueOf))
            .expectNext("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromStart() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1), pagedResponses.get(2),
                pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPagedFluxSubscribeToPagesFromStartWithConvertedType() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage())
            .expectNextCount(5)
            .verifyComplete();

        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage())
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(0).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(1).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(2).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(3).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(4).getValue().equals(pagedResponse.getValue()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPagedFluxSinglePageConvertedType() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage())
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage())
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(0).getValue().equals(pagedResponse.getValue()))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromContinuationToken() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage("3"))
            .expectNext(pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResult() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.byPage())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.byPage(null))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux)
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResultWithoutNextPageRetriever() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux.byPage())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux.byPage(null))
            .verifyComplete();

        pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux)
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithTwoPages() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux.byPage())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux.byPage("1"))
            .expectNext(pagedResponses.get(1))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux)
            .expectNext(0, 1, 2, 3, 4, 5)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromNullContinuationToken() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage(null))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxWithContext() {
        final String expectedContextKey = "hello";
        final String expectedContextValue = "context";

        final Consumer<com.azure.core.util.Context> contextVerifier = context -> {
            assertNotNull(context);
            assertEquals(1, context.size());
            assertEquals(expectedContextValue, context.getData(expectedContextKey).orElse("").toString());
        };

        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Integer>> pagedResponseSupplier = continuationToken ->
            new PagedResponseBase<>(request, 200, headers, Collections.emptyList(), continuationToken, null);

        PagedFlux<Integer> singlePageFlux = new PagedFlux<>(() -> withContext(context -> {
            contextVerifier.accept(context);
            return Mono.just(pagedResponseSupplier.apply(null));
        }));

        StepVerifier.create(singlePageFlux.byPage()
            .contextWrite(Context.of(expectedContextKey, expectedContextValue)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        final String expectedContinuationToken = "0";
        PagedFlux<Integer> multiPageFlux = new PagedFlux<>(() -> withContext(context -> {
            contextVerifier.accept(context);
            return Mono.just(pagedResponseSupplier.apply(expectedContinuationToken));
        }), continuationToken -> withContext(context -> {
            contextVerifier.accept(context);
            assertEquals(expectedContinuationToken, continuationToken);
            return Mono.just(pagedResponseSupplier.apply(null));
        }));

        StepVerifier.create(multiPageFlux.byPage()
            .contextWrite(Context.of(expectedContextKey, expectedContextValue)))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void pagedFluxWithPageSize() {
        final int expectedPageSize = 5;

        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Integer>> pagedResponseSupplier = continuationToken ->
            new PagedResponseBase<>(request, 200, headers, Collections.emptyList(), continuationToken, null);

        PagedFlux<Integer> singlePageFlux = new PagedFlux<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return Mono.just(pagedResponseSupplier.apply(null));
        });

        StepVerifier.create(singlePageFlux.byPage(expectedPageSize))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        final String expectedContinuationToken = "0";
        PagedFlux<Integer> multiPageFlux = new PagedFlux<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return Mono.just(pagedResponseSupplier.apply(expectedContinuationToken));
        }, (continuationToken, pageSize) -> {
            assertEquals(expectedPageSize, pageSize);
            assertEquals(expectedContinuationToken, continuationToken);
            return Mono.just(pagedResponseSupplier.apply(null));
        });

        StepVerifier.create(multiPageFlux.byPage(expectedPageSize))
            .expectNextCount(2)
            .verifyComplete();
    }

    private PagedFlux<Integer> getIntegerPagedFlux(int noOfPages) {
        HttpHeaders httpHeaders = new HttpHeaders().set("header1", "value1")
            .set("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());

        pagedStringResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponseWithString(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());

        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private PagedFlux<Integer> getIntegerPagedFluxSinglePage() {
        HttpHeaders httpHeaders = new HttpHeaders().set("header1", "value1")
            .set("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, 1)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, 1))
            .collect(Collectors.toList());

        pagedStringResponses = IntStream.range(0, 1)
            .boxed()
            .map(i -> createPagedResponseWithString(httpRequest, httpHeaders, deserializedHeaders, i, 1))
            .collect(Collectors.toList());
        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)));
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200,
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private PagedResponseBase<String, String> createPagedResponseWithString(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200,
            httpHeaders,
            getStringItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        int parsedToken = Integer.parseInt(continuationToken);
        if (parsedToken >= pagedResponses.size()) {
            return Mono.empty();
        }

        return Mono.just(pagedResponses.get(parsedToken));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

    private List<String> getStringItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().map(String::valueOf).collect(Collectors.toList());
    }

    @Test
    public void fluxByItemOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedFlux pagedFlux = new OnlyOnePagedFlux(() -> pageRetriever);

        pagedFlux.ignoreElements().block();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        StepVerifier.create(pagedFlux.take(1)
            .then(Mono.delay(Duration.ofMillis(500)).then()))
            .verifyComplete();

        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @Test
    public void fluxByPageOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedFlux pagedFlux = new OnlyOnePagedFlux(() -> pageRetriever);

        pagedFlux.byPage().ignoreElements().block();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        StepVerifier.create(pagedFlux.byPage().take(1)
            .then(Mono.delay(Duration.ofMillis(500)).then()))
            .verifyComplete();

        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @ParameterizedTest
    @MethodSource("pagingTerminatesOnSupplier")
    public <C, T, P extends ContinuablePage<C, T>> void pagingTerminatesOn(ContinuablePagedFlux<C, T, P> pagedFlux,
        List<T> expectedItems) {
        StepVerifier.create(pagedFlux.collectList())
            .assertNext(actualItems -> {
                assertEquals(expectedItems.size(), actualItems.size());
                for (int i = 0; i < expectedItems.size(); i++) {
                    assertEquals(expectedItems.get(i), actualItems.get(i));
                }
            })
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> pagingTerminatesOnSupplier() {
        PageRetriever<String, PagedResponse<String>> pfEndsWithNullPageRetriever = new GetPagesUntil(null);
        PagedFlux<String> pfEndsWithNull = PagedFlux.create(() -> pfEndsWithNullPageRetriever);

        PageRetriever<String, PagedResponse<String>> pfEndsWithEmptyStringPageRetriever = new GetPagesUntil("");
        PagedFlux<String> pfEndsWithEmptyString = PagedFlux.create(() -> pfEndsWithEmptyStringPageRetriever);

        PageRetriever<String, PagedResponse<String>> pfbEndsWithNullPageRetriever = new GetPagesUntil(null);
        PagedFluxBase<String, PagedResponse<String>> pfbEndsWithNull = new PagedFluxBase<>(
            () -> pfbEndsWithNullPageRetriever, false);

        PageRetriever<String, PagedResponse<String>> pfbEndsWithEmptyStringPageRetriever = new GetPagesUntil("");
        PagedFluxBase<String, PagedResponse<String>> pfbEndsWithEmptyString = new PagedFluxBase<>(
            () -> pfbEndsWithEmptyStringPageRetriever, false);


        PageRetriever<String, ContinuablePage<String, String>> cpfcStringTokenEndsWithNullPageRetriever =
            new GetContinuablePagesUntil<>("1", null, token -> String.valueOf(Integer.parseInt(token) + 1),
                token -> token.equals("4"));
        ContinuablePagedFluxCore<String, String, ContinuablePage<String, String>> cpfcStringTokenEndsWithNull =
            createCpfc(() -> cpfcStringTokenEndsWithNullPageRetriever, null);

        PageRetriever<String, ContinuablePage<String, String>> cpfcStringTokenEndsWithPredicatePageRetriever =
            new GetContinuablePagesUntil<>("1", "finalToken", token -> String.valueOf(Integer.parseInt(token) + 1),
                token -> token.equals("4"));
        ContinuablePagedFluxCore<String, String, ContinuablePage<String, String>> cpfcStringTokenEndsWithPredicate =
            createCpfc(() -> cpfcStringTokenEndsWithPredicatePageRetriever, token -> !token.equals("finalToken"));

        PageRetriever<byte[], ContinuablePage<byte[], String>> cpfcByteArrayTokenEndsWithNullPageRetriever =
            new GetContinuablePagesUntil<>("1".getBytes(UTF_8), null,
                token -> String.valueOf(Integer.parseInt(new String(token, UTF_8)) + 1).getBytes(UTF_8),
                token -> new String(token, UTF_8).equals("4"));
        ContinuablePagedFluxCore<byte[], String, ContinuablePage<byte[], String>> cpfcByteArrayTokenEndsWithNull =
            createCpfc(() -> cpfcByteArrayTokenEndsWithNullPageRetriever, null);

        PageRetriever<byte[], ContinuablePage<byte[], String>> cpfcByteArrayTokenEndsWithPredicatePageRetriever =
            new GetContinuablePagesUntil<>("1".getBytes(UTF_8), "finalToken".getBytes(UTF_8),
                token -> String.valueOf(Integer.parseInt(new String(token, UTF_8)) + 1).getBytes(UTF_8),
                token -> new String(token, UTF_8).equals("4"));
        ContinuablePagedFluxCore<byte[], String, ContinuablePage<byte[], String>> cpfcByteArrayTokenEndsWithPredicate =
            createCpfc(() -> cpfcByteArrayTokenEndsWithPredicatePageRetriever,
                token -> !new String(token, UTF_8).equals("finalToken"));

        List<String> pagedFluxExpectedItems = Arrays.asList("1", "2", "3", "4", "5");

        return Stream.of(
            Arguments.arguments(pfEndsWithNull, pagedFluxExpectedItems),
            Arguments.arguments(pfEndsWithEmptyString, pagedFluxExpectedItems),
            Arguments.arguments(pfbEndsWithNull, pagedFluxExpectedItems),
            Arguments.arguments(pfbEndsWithEmptyString, pagedFluxExpectedItems),
            Arguments.arguments(cpfcStringTokenEndsWithNull, pagedFluxExpectedItems),
            Arguments.arguments(cpfcStringTokenEndsWithPredicate, pagedFluxExpectedItems),
            Arguments.arguments(cpfcByteArrayTokenEndsWithNull, pagedFluxExpectedItems),
            Arguments.arguments(cpfcByteArrayTokenEndsWithPredicate, pagedFluxExpectedItems)
        );
    }

    private static <C, T, P extends ContinuablePage<C, T>> ContinuablePagedFluxCore<C, T, P> createCpfc(
        Supplier<PageRetriever<C, P>> pageRetrieverSupplier, Predicate<C> continuationPredicate) {
        if (continuationPredicate == null) {
            return new ContinuablePagedFluxCore<C, T, P>(pageRetrieverSupplier) { };
        }

        return new ContinuablePagedFluxCore<C, T, P>(pageRetrieverSupplier, null, continuationPredicate) { };
    }

    private static final class GetPagesUntil implements PageRetriever<String, PagedResponse<String>> {
        private static final Function<String, String> INCREMENT_STRING_AS_INT = str ->
            String.valueOf(Integer.parseInt(str) + 1);

        private static final Function<String, String> NEXT_PAGE_VALUE = INCREMENT_STRING_AS_INT;
        private static final BiFunction<String, String, PagedResponse<String>> PAGE_CREATOR = (token, item) ->
            createPagedResponse(token, Collections.singletonList(item));

        private final String[] pageValue = new String[] { "1" };

        private final Predicate<String> isFinalPage;
        private final String finalToken;

        private GetPagesUntil(String finalToken) {
            this.finalToken = finalToken;
            this.isFinalPage = token -> token.equals("4");
        }

        @Override
        public Flux<PagedResponse<String>> get(String token, Integer pageSize) {
            return Flux.defer(() -> {
                String nextContinuationToken;
                if (token == null) {
                    nextContinuationToken = "1";
                } else if (isFinalPage.test(token)) {
                    nextContinuationToken = finalToken;
                } else {
                    nextContinuationToken = INCREMENT_STRING_AS_INT.apply(token);
                }

                PagedResponse<String> page = PAGE_CREATOR.apply(nextContinuationToken, pageValue[0]);
                pageValue[0] = NEXT_PAGE_VALUE.apply(pageValue[0]);
                return Flux.just(page);
            });
        }
    }

    private static <T> PagedResponse<T> createPagedResponse(String continuationToken, List<T> items) {
        return new PagedResponseBase<Void, T>(null, 200, null, new Page<T>() {
            @Override
            public IterableStream<T> getElements() {
                return IterableStream.of(items);
            }

            @Override
            public String getContinuationToken() {
                return continuationToken;
            }
        }, null);
    }

    private static final class GetContinuablePagesUntil<C> implements PageRetriever<C, ContinuablePage<C, String>> {
        private static final Function<String, String> INCREMENT_STRING_AS_INT = str ->
            String.valueOf(Integer.parseInt(str) + 1);

        private static final Function<String, String> NEXT_PAGE_VALUE = INCREMENT_STRING_AS_INT;
        private final BiFunction<C, String, ContinuablePage<C, String>> pageCreator = (token, item) ->
            createPage(token, Collections.singletonList(item));

        private final String[] pageValue = new String[] { "1" };

        private final C initialToken;
        private final C finalToken;
        private final Function<C, C> nextToken;
        private final Predicate<C> isFinalPage;

        private GetContinuablePagesUntil(C initialToken, C finalToken, Function<C, C> nextToken,
            Predicate<C> isFinalPage) {
            this.initialToken = initialToken;
            this.finalToken = finalToken;
            this.nextToken = nextToken;
            this.isFinalPage = isFinalPage;
        }

        @Override
        public Flux<ContinuablePage<C, String>> get(C token, Integer pageSize) {
            return Flux.defer(() -> {
                C nextContinuationToken;
                if (token == null) {
                    nextContinuationToken = initialToken;
                } else if (isFinalPage.test(token)) {
                    nextContinuationToken = finalToken;
                } else {
                    nextContinuationToken = nextToken.apply(token);
                }

                ContinuablePage<C, String> page = pageCreator.apply(nextContinuationToken, pageValue[0]);
                pageValue[0] = NEXT_PAGE_VALUE.apply(pageValue[0]);
                return Flux.just(page);
            });
        }
    }

    private static <C, T> ContinuablePage<C, T> createPage(C token, List<T> items) {
        return new ContinuablePage<C, T>() {
            @Override
            public IterableStream<T> getElements() {
                return IterableStream.of(items);
            }

            @Override
            public C getContinuationToken() {
                return token;
            }
        };
    }
}
