// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonNodeStorePayloadTests {
    @DataProvider(name = "fallbackActions")
    public Object[][] fallbackActions() {
        return new Object[][] {{"REPLACE", "\ufffd"}, {"IGNORE", ""}, {"", null}, {"REPORT", null}};
    }

    @Test(groups = {"unit"}, dataProvider = "fallbackActions")
    public void parsingBytesWithInvalidUTF8Bytes(String action, String replacement) throws Exception {
        withFallbackAction(action, () -> {
            byte[] bytes = invalidUtf8Payload();
            if (replacement == null) {
                assertThatThrownBy(() -> parsePayload(bytes, false))
                    .isInstanceOfSatisfying(CosmosException.class, exception -> {
                        assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
                        assertThat(exception.getSubStatusCode())
                            .isEqualTo(HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE);
                    });
            } else {
                String expected = replacement + new String(bytes, 7, bytes.length - 9, StandardCharsets.UTF_8);
                assertThat(parsePayload(bytes, false)).isEqualTo(expected);
                assertThat(parsePayload(bytes, true)).isEqualTo(expected);
            }
        });
    }

    @Test(groups = {"unit"}, timeOut = 30000)
    public void concurrentFallbackDecoding() throws Exception {
        withFallbackAction("REPLACE", () -> {
            byte[] bytes = invalidUtf8Payload();
            String expected = "\ufffd" + new String(bytes, 7, bytes.length - 9, StandardCharsets.UTF_8);
            int workers = 8;
            ExecutorService executor = Executors.newFixedThreadPool(workers);
            CountDownLatch ready = new CountDownLatch(workers);
            CountDownLatch start = new CountDownLatch(1);
            try {
                List<Future<?>> futures = new ArrayList<>();
                for (int worker = 0; worker < workers; worker++) {
                    futures.add(executor.submit(() -> {
                        ready.countDown();
                        assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                        for (int iteration = 0; iteration < 100; iteration++) {
                            assertThat(parsePayload(bytes, iteration % 2 == 0)).isEqualTo(expected);
                        }
                        return null;
                    }));
                }
                assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
                start.countDown();
                for (Future<?> future : futures) {
                    future.get(20, TimeUnit.SECONDS);
                }
            } finally {
                start.countDown();
                executor.shutdownNow();
                assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
            }
        });
    }

    private static byte[] invalidUtf8Payload() {
        byte[] bytes = new byte[32768];
        Arrays.fill(bytes, (byte) 'x');
        byte[] prefix = "{\"v\":\"".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(prefix, 0, bytes, 0, prefix.length);
        bytes[prefix.length] = (byte) 0x8c;
        bytes[bytes.length - 2] = '"';
        bytes[bytes.length - 1] = '}';
        return bytes;
    }

    private static String parsePayload(byte[] bytes, boolean arrayHeaders) throws IOException {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer, true)) {
            JsonNodeStorePayload payload = arrayHeaders
                ? new JsonNodeStorePayload(stream, bytes.length, new String[0], new String[0])
                : new JsonNodeStorePayload(stream, bytes.length, Collections.emptyMap());
            return payload.getPayload().get("v").asText();
        }
    }

    private static void withFallbackAction(String action, FallbackTest test) throws Exception {
        String malformedProperty = Configs.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT;
        String unmappedProperty = Configs.CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER;
        String originalMalformed = System.getProperty(malformedProperty);
        String originalUnmapped = System.getProperty(unmappedProperty);
        try {
            System.setProperty(malformedProperty, action);
            System.setProperty(unmappedProperty, action);
            test.run();
        } finally {
            restoreProperty(malformedProperty, originalMalformed);
            restoreProperty(unmappedProperty, originalUnmapped);
        }
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @FunctionalInterface
    private interface FallbackTest {
        void run() throws Exception;
    }

    @Test(groups = {"unit"})
    public void arrayHeaderConstructorParsesValidJson() {
        String jsonContent = "{\"id\":\"test\",\"name\":\"value\"}";
        String[] headerNames = {"content-type", "x-request-id"};
        String[] headerValues = {"application/json", "req-123"};

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(buffer, true), buffer.readableBytes(), headerNames, headerValues);

        assertThat(payload.getPayload()).isNotNull();
        assertThat(payload.getPayload().get("id").asText()).isEqualTo("test");
        assertThat(payload.getPayload().get("name").asText()).isEqualTo("value");
        assertThat(payload.getResponsePayloadSize()).isEqualTo(jsonContent.getBytes().length);
    }

    @Test(groups = {"unit"})
    public void arrayHeaderConstructorWithEmptyStreamReturnsNull() {
        String[] headerNames = {"content-type"};
        String[] headerValues = {"application/json"};

        ByteBuf buffer = Unpooled.EMPTY_BUFFER;
        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(buffer), 0, headerNames, headerValues);

        assertThat(payload.getPayload()).isNull();
        assertThat(payload.getResponsePayloadSize()).isEqualTo(0);
    }

    @Test(groups = {"unit"})
    public void mapConstructorParsesValidJson() {
        String jsonContent = "{\"id\":\"test\"}";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(buffer, true), buffer.readableBytes(), headers);

        assertThat(payload.getPayload()).isNotNull();
        assertThat(payload.getPayload().get("id").asText()).isEqualTo("test");
    }
}
