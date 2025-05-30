// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.models.ResponseError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public final class RestProxyExceptionTests {

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Get("my/url/path")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(value = HttpResponseException.class, exceptionBodyClass = ResponseError.class)
        Mono<Response<Void>> testMethod();
    }

    @Test
    public void exceptionBodyClassTests() {
        String responseStr = "{\"error\":{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"resource not found\"}}";

        HttpClient client = request -> Mono.just(
                new MockHttpResponse(request, 404, new HttpHeaders(), responseStr.getBytes(StandardCharsets.UTF_8)));
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class, () -> {
            testInterface.testMethod().block();
        });
        Assertions.assertInstanceOf(ResponseError.class, exception.getValue());
    }
}
