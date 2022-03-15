// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.core.MockHttpReceiveClient;
import com.azure.core.perf.core.MyRestProxyService;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.perf.core.TestDataFactory;
import com.azure.core.perf.models.UserDatabase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

public class JsonSendTest extends RestProxyTestBase<PerfStressOptions> {
    private final MockHttpReceiveClient mockHttpReceiveClient;
    private final MyRestProxyService service;
    private final UserDatabase userDatabase;

    public JsonSendTest(PerfStressOptions options) {
        super(options);
        userDatabase = TestDataFactory.generateUserDatabase(options.getSize());
        mockHttpReceiveClient = new MockHttpReceiveClient();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(mockHttpReceiveClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, pipeline);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.setUserDatabaseJson(userDatabase).then();
    }
}
