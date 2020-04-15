// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FluxUtilTest {
    @Test
    public void testCallWithContextGetSingle() {
        String response = getSingle()
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .block();
        assertEquals("Hello, Foo Bar", response);
    }

    @Test
    public void testCallWithContextGetCollection() {
        List<String> expectedLines = Arrays.asList("Hello,", "Foo", "Bar");
        List<String> actualLines = new ArrayList<>();
        getCollection()
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .doOnNext(actualLines::add)
            .subscribe();
        assertEquals(expectedLines, actualLines);
    }

    @Test
    public void testCallWithDefaultContextGetSingle() {
        String response = getSingleWithContextAttributes()
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo"))
            .block();
        assertEquals("Hello, Foo additionalContextValue", response);
    }

    @Test
    public void toReactorContextNull() {
        assertTrue(FluxUtil.toReactorContext(null).isEmpty());
    }

    @Test
    public void toReactorContextContextNone() {
        assertTrue(FluxUtil.toReactorContext(Context.NONE).isEmpty());
    }

    @Test
    public void toReactorContextCleansesNullValues() {
        assertTrue(FluxUtil.toReactorContext(new Context("key", null)).isEmpty());
    }

    @Test
    public void toReactorContext() {
        Context context = new Context("key1", "value1");

        reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(1, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value1", reactorContext.get("key1"));

        context = context.addData("key2", "value2")
            .addData("key1", "value3");

        reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(2, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value3", reactorContext.get("key1"));
        assertTrue(reactorContext.hasKey("key2"));
        assertEquals("value2", reactorContext.get("key2"));
    }

    @Test
    public void testIsFluxByteBufferInvalidType() {
        assertFalse(FluxUtil.isFluxByteBuffer(Mono.class));
    }

    @Test
    public void testIsFluxByteBufferValidType() throws Exception {
        Method method = FluxUtilTest.class.getMethod("mockReturnType");
        Type returnType = method.getGenericReturnType();
        assertTrue(FluxUtil.isFluxByteBuffer(returnType));
    }

    @Test
    public void testToMono() {
        String testValue = "some value";
        Response<String> response = new SimpleResponse<String>(new HttpRequest(HttpMethod.GET, "http://www.test.com"),
            202, new HttpHeaders(), testValue);
        StepVerifier.create(FluxUtil.toMono(response))
            .assertNext(val -> assertEquals(val, testValue))
            .verifyComplete();
    }

    @Test
    public void testMonoError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.monoError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.fluxError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testPageFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.pagedFluxError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testWriteFile() throws Exception {
        String toReplace = "test";
        String original = "hello there";
        String target = "testo there";

        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(toReplace.getBytes(StandardCharsets.UTF_8)));
        File file = createFileIfNotExist("target/test1");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(original.getBytes(StandardCharsets.UTF_8));
        stream.close();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            FluxUtil.writeFile(body, channel).block();
            byte[] outputStream = Files.readAllBytes(file.toPath());
            assertTrue(Arrays.equals(outputStream, target.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public Flux<ByteBuffer> mockReturnType() {
        return Flux.just(ByteBuffer.wrap(new byte[0]));
    }

    private Mono<String> getSingle() {
        return FluxUtil.withContext(this::serviceCallSingle);
    }

    private Flux<String> getCollection() {
        return FluxUtil
            .fluxContext(this::serviceCallCollection);
    }

    private Mono<String> getSingleWithContextAttributes() {
        return FluxUtil.withContext(this::serviceCallWithContextMetadata,
            Collections.singletonMap("additionalContextKey", "additionalContextValue"));
    }

    private Mono<String> serviceCallSingle(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCallCollection(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }

    private Mono<String> serviceCallWithContextMetadata(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("additionalContextKey").orElse("Not found");
        return Mono.just(msg);
    }

    private File createFileIfNotExist(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        return file;
    }
}
