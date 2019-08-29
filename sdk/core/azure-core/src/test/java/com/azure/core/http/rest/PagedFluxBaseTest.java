package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link PagedFluxBase}
 */
public class PagedFluxBaseTest {

    private List<PagedResponse<Integer>> pagedResponses;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        System.out.println("-------------- Running " + testName.getMethodName() + " -----------------------------");
    }

    @Test
    public void testEmptyResultsPagedFluxBase() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(0);
        StepVerifier.create(pagedFluxBase.log()).verifyComplete();
        StepVerifier.create(pagedFluxBase.byPage().log()).verifyComplete();
        StepVerifier.create(pagedFluxBase.byPage(null).log()).verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToItems() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(5);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesFromStart() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(5);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1), pagedResponses.get(2),
                pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesFromContinuationToken() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(5);
        StepVerifier.create(pagedFluxBase.byPage("3").log())
            .expectNext(pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesWithSinglePageResult() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(1);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBase(1);
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBase(1);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesWithSinglePageResultWithoutNextPageRetriever() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBaseSinglePage();
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBaseSinglePage();
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBaseSinglePage();
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesWithTwoPages() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(2);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBase(2);
        StepVerifier.create(pagedFluxBase.byPage("1").log())
            .expectNext(pagedResponses.get(1))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxBase(2);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2, 3, 4, 5)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxBaseSubscribeToPagesFromNullContinuationToken() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxBase(5);
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();
    }

    private PagedFluxBase<Integer, PagedResponse<Integer>> getIntegerPagedFluxBase(int noOfPages) throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());

        return new PagedFluxBase<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private PagedFluxBase<Integer, PagedResponse<Integer>> getIntegerPagedFluxBaseSinglePage() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, 1)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, 1))
            .collect(Collectors.toList());

        return new PagedFluxBase<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)));
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest,
                                                                   HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, HttpResponseStatus.OK.code(),
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
                                                     List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        return Mono.just(pagedResponses.get(Integer.valueOf(continuationToken)));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

}
