// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link OpenAIServerSentEvents}
 */
public class OpenAIServerSentEventsTest {

    @Test
    @DoNotRecord
    public void testEmptyFluxByteBuffer() {
        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.empty(), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .verifyComplete();
    }

    @Test
    @DoNotRecord
    public void testSingleEventFluxByteBuffer() {
        String jsonTestModel = BinaryData.fromObject(new TestModel().setName("foo").setValue("value")).toString();
        String sse = "data: " + jsonTestModel;
        ByteBuffer byteBuffer = ByteBuffer.wrap(sse.getBytes(StandardCharsets.UTF_8));
        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(byteBuffer), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("foo", testModel.getName());
                assertEquals("value", testModel.getValue());
            })
            .verifyComplete();
    }

    @Test
    @DoNotRecord
    public void testMultipleEventsFluxByteBuffer() {
        String jsonTestModel = BinaryData.fromObject(new TestModel().setName("foo").setValue("value")).toString();
        String sse = "data: " + jsonTestModel;

        String jsonTestModel2 = BinaryData.fromObject(new TestModel().setName("foo2").setValue("value2")).toString();
        String sse2 = "data: " + jsonTestModel2;

        sse = sse + "\n\n" + sse2;

        ByteBuffer byteBuffer = ByteBuffer.wrap(sse.getBytes(StandardCharsets.UTF_8));
        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(byteBuffer), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("foo", testModel.getName());
                assertEquals("value", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("foo2", testModel.getName());
                assertEquals("value2", testModel.getValue());
            })
            .verifyComplete();
    }

    @Test
    @DoNotRecord
    public void testMultipleEventsSplitAcrossFluxByteBuffer() {
        String jsonTestModel = BinaryData.fromObject(new TestModel().setName("foo").setValue("value")).toString();
        String sse1 = "data: " + jsonTestModel;

        String jsonTestModel2 = BinaryData.fromObject(new TestModel().setName("foo2").setValue("value2")).toString();
        String sse2 = "data: " + jsonTestModel2;

        ByteBuffer bb1 = ByteBuffer.wrap(sse1.getBytes(StandardCharsets.UTF_8));
        ByteBuffer bb2 = ByteBuffer.wrap(("\n\n" + sse2).getBytes(StandardCharsets.UTF_8));

        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(bb1, bb2), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("foo", testModel.getName());
                assertEquals("value", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("foo2", testModel.getName());
                assertEquals("value2", testModel.getValue());
            })
            .verifyComplete();
    }

    @Test
    @DoNotRecord
    public void testEventSplitAcrossByteBuffers() {
        String jsonTestModel = BinaryData.fromObject(new TestModel().setName("foo").setValue("value")).toString();
        String sse1 = "data: " + jsonTestModel;

        String jsonTestModel2 = BinaryData.fromObject(new TestModel().setName("foo2").setValue("value2")).toString();
        String sse2 = "data: " + jsonTestModel2;

        ByteBuffer bb1 = ByteBuffer.wrap((sse1 + "\n\n" + sse2.substring(0, 10)).getBytes(StandardCharsets.UTF_8));
        ByteBuffer bb2 = ByteBuffer.wrap(sse2.substring(10).getBytes(StandardCharsets.UTF_8));

        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(bb1, bb2), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("foo", testModel.getName());
                assertEquals("value", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("foo2", testModel.getName());
                assertEquals("value2", testModel.getValue());
            })
            .verifyComplete();


        bb1 = ByteBuffer.wrap((sse1 + "\n\n" + sse2.substring(0, 2)).getBytes(StandardCharsets.UTF_8));
        bb2 = ByteBuffer.wrap(sse2.substring(2).getBytes(StandardCharsets.UTF_8));

        objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(bb1, bb2), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("foo", testModel.getName());
                assertEquals("value", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("foo2", testModel.getName());
                assertEquals("value2", testModel.getValue());
            })
            .verifyComplete();
    }

}
