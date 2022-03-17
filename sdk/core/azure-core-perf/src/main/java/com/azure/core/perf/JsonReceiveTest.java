// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpClient;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.MockHttpClient;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.perf.core.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

public class JsonReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public JsonReceiveTest(CorePerfStressOptions options) {
        super(options, createMockHttpClient(options));
    }

    private static HttpClient createMockHttpClient(CorePerfStressOptions options) {
        byte[] bodyBytes = generateBodyBytes(options.getSize());
        return new MockHttpClient((httpRequest) -> createMockResponse(httpRequest,
            "application/json",  bodyBytes));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return new JsonSendTest(options).runAsync();
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getUserDatabaseJsonAsync(endpoint)
            .map(userdatabase -> {
                userdatabase.getValue().getUserList().forEach(sampleUserData -> {
                    sampleUserData.getId();
                });
                return 1;
            }).then();
    }

    private static byte[] generateBodyBytes(long size) {
        return serializeData(TestDataFactory.generateUserDatabase(size), new ObjectMapper());
    }
}
