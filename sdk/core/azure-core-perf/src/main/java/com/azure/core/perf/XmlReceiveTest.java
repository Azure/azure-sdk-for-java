// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.perf.core.TestDataFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class XmlReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public XmlReceiveTest(CorePerfStressOptions options) {
        super(options, createMockResponseSupplier(options));
    }

    private static Function<HttpRequest, HttpResponse> createMockResponseSupplier(CorePerfStressOptions options) {
        byte[] bodyBytes = generateBodyBytes(options.getSize());
        return httpRequest -> createMockResponse(httpRequest,
            "application/xml",  bodyBytes);
    }

    @Override
    public Mono<Void> setupAsync() {
        return service.setUserDatabaseXml(endpoint, id, TestDataFactory.generateUserDatabase(options.getSize()));
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getUserDatabaseXmlAsync(endpoint, id)
            .map(userdatabase -> {
                userdatabase.getValue();
                return 1;
            }).then();
    }

    private static byte[] generateBodyBytes(long size) {
        return serializeData(TestDataFactory.generateUserDatabase(size), new XmlMapper());
    }
}
