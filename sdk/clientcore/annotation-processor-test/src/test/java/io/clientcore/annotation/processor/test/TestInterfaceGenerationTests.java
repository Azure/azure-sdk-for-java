// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);

        // test getFoo method
        // LocalHttpClient doesn't handle /kv/{key} yet and will just return 400.
        assertThrows(RuntimeException.class, () -> testInterface.getFoo("key", "label", "syncToken"));
    }

}
