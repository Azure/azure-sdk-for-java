// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.perf.core.TestDataFactory;
import com.azure.xml.XmlWriter;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

public class XmlReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public XmlReceiveTest(CorePerfStressOptions options) {
        super(options, createMockResponseSupplier(options));
    }

    private static Function<HttpRequest, HttpResponse> createMockResponseSupplier(CorePerfStressOptions options) {
        byte[] bodyBytes = generateBodyBytes(options.getSize());
        return httpRequest -> createMockResponse(httpRequest, "application/xml", bodyBytes);
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
        return service.getUserDatabaseXmlAsync(endpoint, id).map(userdatabase -> {
            userdatabase.getValue();
            return 1;
        }).then();
    }

    private static byte[] generateBodyBytes(long size) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XmlWriter xmlWriter = XmlWriter.toStream(outputStream)) {
            TestDataFactory.generateUserDatabase(size).toXml(xmlWriter).flush();
            return outputStream.toByteArray();
        } catch (IOException | XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }
}
