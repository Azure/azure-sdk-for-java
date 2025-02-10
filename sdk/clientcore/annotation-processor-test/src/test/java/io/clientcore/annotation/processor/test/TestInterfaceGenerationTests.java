// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.Foo;
import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestInterfaceGenerationTests {

    @Test
    public void testGetNewInstance() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
    }

    @Test
    public void testGetFoo() {
        String wireValue
            =
            "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient((request) -> {
            // what is the default response body mode?
            request.setRequestOptions(new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE));
            return new MockHttpResponse(request, 200, wireValue.getBytes(StandardCharsets.UTF_8));
        }).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);

        // test getFoo method
        Response<Foo> response = testInterface.getFoo("key", "label", "sync-token-value");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        Foo foo = response.getValue();
        assertEquals("key", foo.getKey());
    }

}
