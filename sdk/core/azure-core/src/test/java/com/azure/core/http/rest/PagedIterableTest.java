// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link PagedIterable}.
 */
public class PagedIterableTest {

    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    @Test
    public void testEmptyResults() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(0);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        assertEquals(0, pagedIterable.streamByPage().count());
    }

    @Test
    public void testPageStream() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        assertEquals(5, pagedIterable.streamByPage().count());
        assertEquals(pagedResponses, pagedIterable.streamByPage().collect(Collectors.toList()));
    }

    @Test
    public void testPageIterable() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        Iterator<PagedResponse<Integer>> iter = pagedIterable.iterableByPage().iterator();

        int index = 0;
        while (iter.hasNext()) {
            PagedResponse<Integer> pagedResponse = iter.next();
            assertEquals(pagedResponses.get(index++), pagedResponse);
        }
    }

    @Test
    public void testStream() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        assertEquals(15, pagedIterable.stream().count());
        List<Integer> ints = Stream.iterate(0, i -> i + 1).limit(15).collect(Collectors.toList());
        assertEquals(ints, pagedIterable.stream().collect(Collectors.toList()));
    }

    @Test
    public void testIterable() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        Iterator<Integer> iter = pagedIterable.iterator();

        int index = 0;
        while (iter.hasNext()) {
            int val = iter.next();
            assertEquals(index++, val);
        }
    }

    @Test
    public void testMap() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<String> intStrs =
            Stream.iterate(0, i -> i + 1).map(String::valueOf).limit(15).collect(Collectors.toList());
        assertEquals(intStrs, pagedIterable.mapPage(String::valueOf).stream().collect(Collectors.toList()));
    }

    @Test
    public void testPageMap() {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        int[] index = new int[1];
        assertTrue(pagedIterable.mapPage(String::valueOf).streamByPage().allMatch(pagedResponse ->
            pagedStringResponses.get(index[0]++).getValue().equals(pagedResponse.getValue())));
    }

    private PagedFlux<Integer> getIntegerPagedFlux(int noOfPages) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
                .put("header2", "value2");

            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

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
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private PagedFlux<Integer> getIntegerPagedFluxSinglePage() {
        try {
            HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
                .put("header2", "value2");
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

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
        } catch (MalformedURLException e) {
            return null;
        }
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

        return Mono.just(pagedResponses.get(Integer.valueOf(continuationToken)));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

    private List<String> getStringItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().map(val -> String.valueOf(val)).collect(Collectors.toList());
    }

}
