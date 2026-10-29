// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureSdkHandlerTests {
    @ParameterizedTest
    @ValueSource(longs = { 0, -1, 10000 })
    public void responseTimeoutStartsAfterWritingCompletes(long writeTimeoutMillis) {
        EmbeddedChannel channel = new EmbeddedChannel(new AzureSdkHandler(null, writeTimeoutMillis, 1000, 0));
        channel.freezeTime();
        try {
            channel.writeOutbound(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/"));
            channel.advanceTimeBy(1, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            assertDoesNotThrow(channel::checkException);

            channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
            channel.advanceTimeBy(1, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();

            TimeoutException exception = assertThrows(TimeoutException.class, channel::checkException);
            assertEquals("Channel response timed out after 1000 milliseconds.", exception.getMessage());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @ParameterizedTest
    @CsvSource({ "0, 0", "0, 10000", "-1, 0", "-1, 10000", "10000, 10000" })
    public void readTimeoutStartsAfterReceivingResponse(long writeTimeoutMillis, long responseTimeoutMillis) {
        EmbeddedChannel channel
            = new EmbeddedChannel(new AzureSdkHandler(null, writeTimeoutMillis, responseTimeoutMillis, 1000));
        channel.freezeTime();
        try {
            channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
            // Leave the read incomplete so there is no read progress before the idle timer fires.
            channel.pipeline().fireChannelRead(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            channel.advanceTimeBy(1, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();

            TimeoutException exception = assertThrows(TimeoutException.class, channel::checkException);
            assertEquals("Channel read timed out after 1000 milliseconds.", exception.getMessage());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, 10000 })
    public void completedResponseCancelsTimeouts(long writeTimeoutMillis) {
        EmbeddedChannel channel = new EmbeddedChannel(new AzureSdkHandler(null, writeTimeoutMillis, 1000, 1000));
        channel.freezeTime();
        try {
            channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
            channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK),
                LastHttpContent.EMPTY_LAST_CONTENT);
            channel.advanceTimeBy(1, TimeUnit.MINUTES);
            channel.runScheduledPendingTasks();

            assertDoesNotThrow(channel::checkException);
            assertTrue(channel.isActive());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void disabledTimeoutsDoNotFire() {
        EmbeddedChannel channel = new EmbeddedChannel(new AzureSdkHandler(null, 0, 0, 0));
        channel.freezeTime();
        try {
            channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
            channel.advanceTimeBy(1, TimeUnit.MINUTES);
            channel.runScheduledPendingTasks();
            assertDoesNotThrow(channel::checkException);

            channel.pipeline().fireChannelRead(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            channel.advanceTimeBy(1, TimeUnit.MINUTES);
            channel.runScheduledPendingTasks();
            assertDoesNotThrow(channel::checkException);
            assertTrue(channel.isActive());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
