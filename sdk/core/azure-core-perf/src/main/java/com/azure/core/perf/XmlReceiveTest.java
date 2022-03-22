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
    public Mono<Void> globalSetupAsync() {
        XmlSendTest sendTest = new XmlSendTest(options);
        return super.globalSetupAsync()
            .then(Mono.defer(sendTest::globalSetupAsync))
            .then(Mono.defer(sendTest::setupAsync))
            .then(Mono.defer(sendTest::runAsync))
            .then(Mono.defer(sendTest::cleanupAsync))
            .then(Mono.defer(sendTest::globalCleanupAsync));
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getUserDatabaseXmlAsync(endpoint)
            .map(userdatabase -> {
                userdatabase.getValue();
                return 1;
            }).then();
    }

    private static byte[] generateBodyBytes(long size) {
        return serializeData(TestDataFactory.generateUserDatabase(size), new XmlMapper());
    }
}
