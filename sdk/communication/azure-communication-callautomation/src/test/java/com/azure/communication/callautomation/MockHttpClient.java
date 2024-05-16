// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import com.azure.core.test.http.NoOpHttpClient;
import reactor.core.publisher.Mono;

public class MockHttpClient extends NoOpHttpClient {
    public static final String THROW_TEST_EXCEPTION = "exception";
    private ArrayList<SimpleEntry<String, Integer>> responses = new ArrayList<SimpleEntry<String, Integer>>();
    private final ArrayList<HttpRequest> requests  = new ArrayList<HttpRequest>();

    public MockHttpClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        this.responses = responses;
    }

    public List<HttpRequest> getRequests() {
        return requests;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        requests.add(request);

        if (responses.size() > 0) {
            SimpleEntry<String, Integer> entry = responses.get(0);
            responses.remove(entry);
            HttpResponse response = CallAutomationUnitTestBase.generateMockResponse(entry.getKey(), request, entry.getValue());
            if (entry.getKey().startsWith(THROW_TEST_EXCEPTION)) {
                return Mono.error(new HttpResponseException("Mock error", response));
            }

            return Mono.just(CallAutomationUnitTestBase.generateMockResponse(entry.getKey(), request, entry.getValue()));
        }

        return Mono.just(CallAutomationUnitTestBase.generateMockResponse("", request, 500));
    }
}
