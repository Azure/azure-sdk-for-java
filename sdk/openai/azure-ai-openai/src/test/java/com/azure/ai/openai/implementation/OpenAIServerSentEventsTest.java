// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link OpenAIServerSentEvents}
 */
public class OpenAIServerSentEventsTest {

    @Test
    public void testEmptyFluxByteBuffer() {
        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.empty(), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .verifyComplete();
    }

    @Test
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

    @Test
    public void testMultiByteChars() {
        String jsonTestModel = BinaryData.fromObject(new TestModel().setName("罗杰·费德勒").setValue("瑞士")).toString();
        String sse1 = "data: " + jsonTestModel;

        String jsonTestModel2 = BinaryData.fromObject(new TestModel().setName("foo2").setValue("value2")).toString();
        String sse2 = "data: " + jsonTestModel2;

        String jsonTestModel3 = BinaryData.fromObject(new TestModel().setName("罗杰·").setValue("Switzerland")).toString();
        String sse3 = "data: " + jsonTestModel3;

        String fullData = sse1 + "\n\n" + sse2 + "\r\n\r\n" + sse3;

        byte[] fullDataBytes = fullData.getBytes(StandardCharsets.UTF_8);
        // split the events with some bytes of a multi-byte char are in separate byte buffers
        ByteBuffer bb1 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 0, 47));
        ByteBuffer bb2 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 47, 100));
        ByteBuffer bb3 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 100, fullDataBytes.length));

        OpenAIServerSentEvents<TestModel> objectOpenAIServerSentEvents = new OpenAIServerSentEvents<>(Flux.just(bb1, bb2, bb3), TestModel.class);
        StepVerifier.create(objectOpenAIServerSentEvents.getEvents())
            .assertNext(testModel -> {
                assertEquals("罗杰·费德勒", testModel.getName());
                assertEquals("瑞士", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("foo2", testModel.getName());
                assertEquals("value2", testModel.getValue());
            }).assertNext(testModel -> {
                assertEquals("罗杰·", testModel.getName());
                assertEquals("Switzerland", testModel.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void segmentedEvent() {
        Flux<ByteBuffer> source = Flux.fromIterable(Arrays.asList(
                ByteBuffer.wrap("data: {\"choices\":[{\"content_filter_results\":{\"hate\":{\"filtered\":false,\"severity\":\"safe\"},\"self_harm\":{\"filtered\":false,\"severity\":\"safe\"}".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap(",\"sexual\":{\"filtered\":false,\"severity\":\"safe\"},\"violence\":{\"filtered\":false,\"severity\":\"safe\"}},\"delta\":{\"content\":\" par\"},\"finish_reason\":null,\"index\":0,\"logprobs\":null}],\"created\":1724446441,\"id\":\"id\",\"model\":\"model\",\"object\":\"chat.completion.chunk\",\"system_fingerprint\":\"fingerprint\"}".getBytes(StandardCharsets.UTF_8))));
        BinaryData fullJson = BinaryData.fromString(
                "{\"choices\":[{\"content_filter_results\":{\"hate\":{\"filtered\":false,\"severity\":\"safe\"},\"self_harm\":{\"filtered\":false,\"severity\":\"safe\"}" +
                        ",\"sexual\":{\"filtered\":false,\"severity\":\"safe\"},\"violence\":{\"filtered\":false,\"severity\":\"safe\"}},\"delta\":{\"content\":\" par\"},\"finish_reason\":null,\"index\":0,\"logprobs\":null}],\"created\":1724446441,\"id\":\"id\",\"model\":\"model\",\"object\":\"chat.completion.chunk\",\"system_fingerprint\":\"fingerprint\"}");

        ChatCompletions expected = fullJson.toObject(ChatCompletions.class);
        OpenAIServerSentEvents<ChatCompletions> parser = new OpenAIServerSentEvents<>(source, ChatCompletions.class);
        ChatCompletions actual = parser.getEvents().blockLast();
        assertNotNull(actual);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getUsage(), actual.getUsage());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expected.getSystemFingerprint(), actual.getSystemFingerprint());
        assertEquals(expected.getChoices().size(), actual.getChoices().size());

        for (int i = 0; i < expected.getChoices().size(); i++) {
            assertEquals(expected.getChoices().get(i).getIndex(), actual.getChoices().get(i).getIndex());
            assertEquals(expected.getChoices().get(i).getFinishReason(), actual.getChoices().get(i).getFinishReason());
            assertEquals(expected.getChoices().get(i).getLogprobs(), actual.getChoices().get(i).getLogprobs());
            assertEquals(expected.getChoices().get(i).getDelta().getContent(), actual.getChoices().get(i).getDelta().getContent());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getHate().isFiltered(), actual.getChoices().get(i).getContentFilterResults().getHate().isFiltered());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getHate().getSeverity(), actual.getChoices().get(i).getContentFilterResults().getHate().getSeverity());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getSelfHarm().isFiltered(), actual.getChoices().get(i).getContentFilterResults().getSelfHarm().isFiltered());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getSelfHarm().getSeverity(), actual.getChoices().get(i).getContentFilterResults().getSelfHarm().getSeverity());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getSexual().isFiltered(), actual.getChoices().get(i).getContentFilterResults().getSexual().isFiltered());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getSexual().getSeverity(), actual.getChoices().get(i).getContentFilterResults().getSexual().getSeverity());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getViolence().isFiltered(), actual.getChoices().get(i).getContentFilterResults().getViolence().isFiltered());
            assertEquals(expected.getChoices().get(i).getContentFilterResults().getViolence().getSeverity(), actual.getChoices().get(i).getContentFilterResults().getViolence().getSeverity());
        }
    }

}
