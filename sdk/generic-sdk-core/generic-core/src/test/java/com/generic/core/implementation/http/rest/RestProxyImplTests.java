// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.MockHttpResponse;
import com.generic.core.http.RestProxy;
import com.generic.core.http.annotation.BodyParam;
import com.generic.core.http.annotation.HeaderParam;
import com.generic.core.http.annotation.HttpRequestInformation;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.Response;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.util.Context;
import com.generic.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyImplTests {
    @ServiceInterface(name = "myService", host = "https://azure.com")
    interface TestInterface {
        @HttpRequestInformation(method = HttpMethod.POST, path = "my/url/path", expectedStatusCodes = {200})
        Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
                                  @HeaderParam("Content-Type") String contentType,
                                  @HeaderParam("Content-Length") Long contentLength, Context context
        );

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/url/path", expectedStatusCodes = {200})
        void testVoidMethod(Context context);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testVoidMethod(Context.EMPTY);

        assertTrue(client.lastResponseClosed);
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        byte[] bytes = "hello".getBytes();
        Response<Void> response = testInterface.testMethod(BinaryData.fromStream(new ByteArrayInputStream(bytes),
            (long) bytes.length), "application/json", (long) bytes.length, Context.EMPTY);

        assertEquals(200, response.getStatusCode());
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile boolean lastResponseClosed;

        @Override
        public Response<?> send(HttpRequest request) {
            boolean success = request.getUrl().getPath().equals("/my/url/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() throws IOException {
                    lastResponseClosed = true;

                    super.close();
                }
            };
        }
    }
}
