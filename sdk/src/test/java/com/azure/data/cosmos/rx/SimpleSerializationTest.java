/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;

import org.apache.commons.lang3.NotImplementedException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSerializationTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private CosmosClient client;

    private static class TestObject {
        public static class BadSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) {
                throw new NotImplementedException("bad");
            }
        }

        @JsonProperty("mypk")
        private String mypk;

        @JsonProperty("id")
        private String id;

        @JsonProperty("prop")
        @JsonSerialize(using = BadSerializer.class)
        private String prop;
    }

    @Factory(dataProvider = "clientBuildersWithDirect")
    public SimpleSerializationTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createDocument() throws InterruptedException {
        TestObject testObject = new TestObject();
        testObject.id = UUID.randomUUID().toString();
        testObject.mypk = UUID.randomUUID().toString();
        testObject.prop = UUID.randomUUID().toString();

        try {
            createdCollection.createItem(testObject);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Can't serialize the object into the json string");
            assertThat(e.getCause()).isInstanceOf(JsonMappingException.class);
            assertThat(e.getCause().getMessage()).contains("bad");
        }
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
