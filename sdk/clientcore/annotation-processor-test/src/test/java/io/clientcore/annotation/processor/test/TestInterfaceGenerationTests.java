// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.Foo;
import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestInterfaceGenerationTests {
    public static final String SYNC_TOKEN_VALUE = "syncToken";
    @Test
    public void testGetNewInstance() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
    }

    @Test
    public void testGetFoo() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);

         Response<Foo> response = testInterface.getFoo("key", "label", SYNC_TOKEN_VALUE);
         assertNotNull(response);
    }

}
