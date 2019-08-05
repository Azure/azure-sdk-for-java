// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
        super(clientBuilder);
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
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
