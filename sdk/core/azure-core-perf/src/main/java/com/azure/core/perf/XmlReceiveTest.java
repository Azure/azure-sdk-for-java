// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.core.MockHttpClient;
import com.azure.core.perf.core.MyRestProxyService;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.perf.core.TestDataFactory;
import com.azure.perf.test.core.PerfStressOptions;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class XmlReceiveTest extends RestProxyTestBase<PerfStressOptions> {
    private final MockHttpClient mockHTTPClient;
    private final MyRestProxyService service;
    private final byte[] bodyBytes;

    public XmlReceiveTest(PerfStressOptions options) throws IOException {
        super(options);
        this.bodyBytes = generateBodyBytes(options.getSize());
        mockHTTPClient = new MockHttpClient((httpRequest) -> createMockResponse(httpRequest,
            "application/xml", bodyBytes));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(mockHTTPClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, pipeline);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getUserDatabaseAsync()
            .map(userdatabase -> {
                userdatabase.getValue();
                return 1;
            }).then();
    }

    private byte[] generateBodyBytes(long size) throws IOException {
        return serializeData(TestDataFactory.generateUserDatabase(size), new XmlMapper());
    }
}
