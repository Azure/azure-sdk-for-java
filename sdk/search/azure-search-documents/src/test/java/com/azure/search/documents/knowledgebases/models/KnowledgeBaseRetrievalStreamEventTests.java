// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.models;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeBaseRetrievalStreamEventTests {
    private static final String CONVERTER_NAME = KnowledgeBaseRetrievalStreamEventConverter.class.getName();

    @Test
    public void baseIsConcreteAndNonFinal() {
        assertTrue(Modifier.isPublic(KnowledgeBaseRetrievalStreamEvent.class.getModifiers()));
        assertFalse(Modifier.isAbstract(KnowledgeBaseRetrievalStreamEvent.class.getModifiers()));
        assertFalse(Modifier.isFinal(KnowledgeBaseRetrievalStreamEvent.class.getModifiers()));
        KnowledgeBaseRetrievalStreamEvent event = new KnowledgeBaseRetrievalStreamEvent("future.event");
        assertEquals(KnowledgeBaseRetrievalStreamEvent.class, event.getClass());
        assertEquals("future.event", event.getEventName());
        assertFalse(event.isTerminal());
    }

    private static Stream<Arguments> wrapperConstructors() {
        return Stream.of(Arguments.of(KnowledgeBaseRetrievalStreamEvent.class, String.class),
            Arguments.of(KnowledgeBaseRetrievalStartedStreamEvent.class, KnowledgeBaseRetrievalStartedEvent.class),
            Arguments.of(KnowledgeBaseActivityStartedStreamEvent.class, KnowledgeBaseActivityStartedEvent.class),
            Arguments.of(KnowledgeBaseActivityCompletedStreamEvent.class, KnowledgeBaseActivityRecord.class),
            Arguments.of(KnowledgeBaseAnswerCompletedStreamEvent.class, KnowledgeBaseAnswerCompletedEvent.class),
            Arguments.of(KnowledgeBaseReferencesCompletedStreamEvent.class, List.class),
            Arguments.of(KnowledgeBaseErrorStreamEvent.class, KnowledgeBaseStreamErrorEvent.class),
            Arguments.of(KnowledgeBaseResponseCompletedStreamEvent.class, KnowledgeBaseResponseCompletedEvent.class));
    }

    @ParameterizedTest
    @MethodSource("wrapperConstructors")
    public void wrappersHaveOnlyExactPackagePrivateConstructorsAndEnvelopeApis(Class<?> wrapper, Class<?> payloadType)
        throws NoSuchMethodException {
        assertTrue(Modifier.isPublic(wrapper.getModifiers()));
        assertEquals(1, wrapper.getDeclaredConstructors().length);
        assertEquals(0, wrapper.getConstructors().length);
        Constructor<?> constructor = wrapper.getDeclaredConstructor(payloadType);
        assertEquals(0, constructor.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
        assertFalse(JsonSerializable.class.isAssignableFrom(wrapper));
        List<String> methods = Arrays.stream(wrapper.getDeclaredMethods())
            .filter(method -> !method.isSynthetic() && Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .sorted()
            .collect(Collectors.toList());
        if (wrapper == KnowledgeBaseRetrievalStreamEvent.class) {
            assertEquals(Arrays.asList("getEventName", "isTerminal"), methods);
        } else {
            assertTrue(Modifier.isFinal(wrapper.getModifiers()));
            assertEquals(wrapper == KnowledgeBaseErrorStreamEvent.class
                || wrapper == KnowledgeBaseResponseCompletedStreamEvent.class
                    ? Arrays.asList("getValue", "isTerminal")
                    : Collections.singletonList("getValue"),
                methods);
        }
        assertEquals(
            Collections.singletonList(wrapper == KnowledgeBaseRetrievalStreamEvent.class ? "eventName" : "value"),
            Arrays.stream(wrapper.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .map(field -> field.getName())
                .collect(Collectors.toList()));
    }

    private static Stream<Arguments> knownEvents() {
        return Stream.of(Arguments.of("retrieval.started", "{\"requestId\":\"request\",\"knowledgeBaseName\":\"kb\"}"),
            Arguments.of("activity.started",
                "{\"id\":0,\"type\":\"searchIndex\",\"startedAt\":\"2025-01-01T00:00:00Z\"}"),
            Arguments.of("activity.completed",
                "{\"type\":\"searchIndex\",\"id\":0,\"knowledgeSourceName\":\"source\"}"),
            Arguments.of("answer.completed",
                "{\"messageIndex\":0,\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"answer\"}]}}"),
            Arguments.of("references.completed",
                "[{\"type\":\"searchIndex\",\"id\":\"reference\",\"activitySource\":0,\"docKey\":\"document\"}]"),
            Arguments.of("error", "{\"error\":{\"code\":\"BadRequest\",\"message\":\"bad request\"}}"),
            Arguments.of("response.completed", "{\"statusCode\":200,\"response\":{}}"));
    }

    @ParameterizedTest
    @MethodSource("knownEvents")
    public void payloadOnlyConstructorsRetainTypedPayloads(String eventName, String json) {
        KnowledgeBaseRetrievalStreamEvent converted
            = KnowledgeBaseRetrievalStreamEventConverter.convert(eventName, json);
        KnowledgeBaseRetrievalStreamEvent event = withPayload(converted);
        assertSame(payload(converted), payload(event));
        assertEquals(converted.getClass(), event.getClass());
        assertEquals(eventName, event.getEventName());
        assertEquals("error".equals(eventName) || "response.completed".equals(eventName), event.isTerminal());
        if ("activity.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexActivityRecord.class, payload(event));
        }
        if ("references.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexReference.class,
                ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue().get(0));
        }
    }

    @Test
    public void nullPayloadsAndEmptyReferenceListsRemainAccessible() {
        List<KnowledgeBaseRetrievalStreamEvent> absent = Arrays.asList(
            new KnowledgeBaseRetrievalStartedStreamEvent(null), new KnowledgeBaseActivityStartedStreamEvent(null),
            new KnowledgeBaseActivityCompletedStreamEvent(null), new KnowledgeBaseAnswerCompletedStreamEvent(null),
            new KnowledgeBaseReferencesCompletedStreamEvent(null), new KnowledgeBaseErrorStreamEvent(null),
            new KnowledgeBaseResponseCompletedStreamEvent(null));
        absent.forEach(event -> assertNull(payload(event)));
        List<KnowledgeBaseReference> empty = Collections.emptyList();
        assertSame(empty, new KnowledgeBaseReferencesCompletedStreamEvent(empty).getValue());
    }

    @Test
    public void constructorsAndPayloadAccessNeverSerialize() {
        AtomicInteger writes = new AtomicInteger();
        KnowledgeBaseActivityRecord activity = new KnowledgeBaseActivityRecord(0) {
            @Override
            public JsonWriter toJson(JsonWriter writer) throws IOException {
                writes.incrementAndGet();
                throw new IOException("The event wrapper must not serialize its payload.");
            }
        };
        KnowledgeBaseActivityCompletedStreamEvent event = new KnowledgeBaseActivityCompletedStreamEvent(activity);
        assertSame(activity, event.getValue());
        assertSame(activity, event.getValue());
        assertEquals(0, writes.get());

        AtomicInteger referenceWrites = new AtomicInteger();
        KnowledgeBaseReference reference = new KnowledgeBaseReference("reference", 0) {
            @Override
            public JsonWriter toJson(JsonWriter writer) throws IOException {
                referenceWrites.incrementAndGet();
                throw new IOException("The event wrapper must not serialize its payload.");
            }
        };
        List<KnowledgeBaseReference> references = Collections.singletonList(reference);
        KnowledgeBaseReferencesCompletedStreamEvent array = new KnowledgeBaseReferencesCompletedStreamEvent(references);
        assertSame(references, array.getValue());
        assertSame(reference, array.getValue().get(0));
        assertEquals(0, referenceWrites.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "future.event", "retrieval.started" })
    public void converterInitializesConstructorsOnFirstUse(String eventName) throws Exception {
        try (URLClassLoader loader = createEventClassLoader()) {
            Class<?> converter = Class.forName(CONVERTER_NAME, true, loader);
            Object event = converter.getMethod("convert", String.class, String.class)
                .invoke(null, eventName, "retrieval.started".equals(eventName) ? "{}" : "not json");
            assertEquals(loader, event.getClass().getClassLoader());
            assertEquals("retrieval.started".equals(eventName)
                ? KnowledgeBaseRetrievalStartedStreamEvent.class.getName()
                : KnowledgeBaseRetrievalStreamEvent.class.getName(), event.getClass().getName());
            assertEquals(eventName, event.getClass().getMethod("getEventName").invoke(event));
            assertEquals(false, event.getClass().getMethod("isTerminal").invoke(event));
        }
    }

    @Test
    public void modelInitializationRegistersConstructorsBeforeConverterUse() throws Exception {
        try (URLClassLoader loader = createEventClassLoader()) {
            Class<?> base = Class.forName(KnowledgeBaseRetrievalStreamEvent.class.getName(), true, loader);
            Class<?> converter = Class.forName(CONVERTER_NAME, true, loader);
            Object event
                = converter.getMethod("convert", String.class, String.class).invoke(null, "future.event", "not json");
            assertSame(base, event.getClass());
            assertEquals("future.event", base.getMethod("getEventName").invoke(event));
            assertEquals(false, base.getMethod("isTerminal").invoke(event));
        }
    }

    @Test
    public void converterInitializesConstructorsOnConcurrentFirstUse() throws Exception {
        try (URLClassLoader loader = createEventClassLoader()) {
            Class<?> converter = Class.forName(CONVERTER_NAME, true, loader);
            Method convert = converter.getMethod("convert", String.class, String.class);
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CountDownLatch ready = new CountDownLatch(8);
            CountDownLatch start = new CountDownLatch(1);
            try {
                List<Future<Object>> results = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    String eventName = i % 2 == 0 ? "future.event" : "retrieval.started";
                    results.add(executor.submit(() -> {
                        ready.countDown();
                        assertTrue(start.await(30, TimeUnit.SECONDS));
                        return convert.invoke(null, eventName, "{}");
                    }));
                }
                assertTrue(ready.await(30, TimeUnit.SECONDS));
                start.countDown();
                for (int i = 0; i < results.size(); i++) {
                    Object event = results.get(i).get(30, TimeUnit.SECONDS);
                    assertSame(loader, event.getClass().getClassLoader());
                    assertEquals(i % 2 == 0
                        ? KnowledgeBaseRetrievalStreamEvent.class.getName()
                        : KnowledgeBaseRetrievalStartedStreamEvent.class.getName(), event.getClass().getName());
                    assertEquals(false, event.getClass().getMethod("isTerminal").invoke(event));
                }
            } finally {
                start.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
            }
        }
    }

    private static URLClassLoader createEventClassLoader() throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(KnowledgeBaseRetrievalStreamEvent.class.getProtectionDomain().getCodeSource().getLocation());
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"))
            + File.pathSeparator + System.getProperty("jdk.module.path", "");
        for (String entry : classPath.split(File.pathSeparator)) {
            urls.add(new File(entry).toURI().toURL());
        }
        // A platform parent can route named SDK modules back to the already initialized application loader.
        return URLClassLoader.newInstance(urls.toArray(new URL[0]), null);
    }

    private static Object payload(KnowledgeBaseRetrievalStreamEvent event) {
        switch (event.getEventName()) {
            case "retrieval.started":
                return ((KnowledgeBaseRetrievalStartedStreamEvent) event).getValue();

            case "activity.started":
                return ((KnowledgeBaseActivityStartedStreamEvent) event).getValue();

            case "activity.completed":
                return ((KnowledgeBaseActivityCompletedStreamEvent) event).getValue();

            case "answer.completed":
                return ((KnowledgeBaseAnswerCompletedStreamEvent) event).getValue();

            case "references.completed":
                return ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue();

            case "error":
                return ((KnowledgeBaseErrorStreamEvent) event).getValue();

            case "response.completed":
                return ((KnowledgeBaseResponseCompletedStreamEvent) event).getValue();

            default:
                throw new IllegalArgumentException("Expected a known event: " + event.getEventName());
        }
    }

    private static KnowledgeBaseRetrievalStreamEvent withPayload(KnowledgeBaseRetrievalStreamEvent event) {
        switch (event.getEventName()) {
            case "retrieval.started":
                return new KnowledgeBaseRetrievalStartedStreamEvent(
                    ((KnowledgeBaseRetrievalStartedStreamEvent) event).getValue());

            case "activity.started":
                return new KnowledgeBaseActivityStartedStreamEvent(
                    ((KnowledgeBaseActivityStartedStreamEvent) event).getValue());

            case "activity.completed":
                return new KnowledgeBaseActivityCompletedStreamEvent(
                    ((KnowledgeBaseActivityCompletedStreamEvent) event).getValue());

            case "answer.completed":
                return new KnowledgeBaseAnswerCompletedStreamEvent(
                    ((KnowledgeBaseAnswerCompletedStreamEvent) event).getValue());

            case "references.completed":
                return new KnowledgeBaseReferencesCompletedStreamEvent(
                    ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue());

            case "error":
                return new KnowledgeBaseErrorStreamEvent(((KnowledgeBaseErrorStreamEvent) event).getValue());

            case "response.completed":
                return new KnowledgeBaseResponseCompletedStreamEvent(
                    ((KnowledgeBaseResponseCompletedStreamEvent) event).getValue());

            default:
                throw new IllegalArgumentException("Expected a known event: " + event.getEventName());
        }
    }
}
