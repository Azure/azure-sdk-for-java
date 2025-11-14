// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.DefaultHttp2SettingsFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Http2ResponseHeaderCleanerHandlerTest {

    @Test(groups = { "unit" })
    public void testHeadersFramePassedThrough() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        
        Http2Headers headers = new DefaultHttp2Headers();
        headers.set("x-ms-serviceversion", "test-value");
        Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, false);
        
        // Write the frame to the channel
        channel.writeInbound(headersFrame);
        
        // The frame should be passed through
        Http2HeadersFrame outFrame = channel.readInbound();
        assertNotNull(outFrame, "Headers frame should be passed through");
        assertEquals(outFrame.headers().get("x-ms-serviceversion").toString(), "test-value");
        
        // Clean up
        channel.finishAndReleaseAll();
    }

    @Test(groups = { "unit" })
    public void testHeadersFrameWithLeadingWhitespace() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        
        Http2Headers headers = new DefaultHttp2Headers();
        headers.set("x-ms-serviceversion", " test-value");
        Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, false);
        
        // Write the frame to the channel
        channel.writeInbound(headersFrame);
        
        // The frame should be passed through with trimmed value
        Http2HeadersFrame outFrame = channel.readInbound();
        assertNotNull(outFrame, "Headers frame should be passed through");
        assertEquals(outFrame.headers().get("x-ms-serviceversion").toString(), "test-value");
        
        // Clean up
        channel.finishAndReleaseAll();
    }

    @Test(groups = { "unit" })
    public void testHeadersFrameWithTrailingWhitespace() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        
        Http2Headers headers = new DefaultHttp2Headers();
        headers.set("x-ms-serviceversion", "test-value ");
        Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, false);
        
        // Write the frame to the channel
        channel.writeInbound(headersFrame);
        
        // The frame should be passed through with trimmed value
        Http2HeadersFrame outFrame = channel.readInbound();
        assertNotNull(outFrame, "Headers frame should be passed through");
        assertEquals(outFrame.headers().get("x-ms-serviceversion").toString(), "test-value");
        
        // Clean up
        channel.finishAndReleaseAll();
    }

    @Test(groups = { "unit" })
    public void testSettingsFrameIsNotPassedThrough() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        
        Http2Settings settings = new Http2Settings();
        Http2SettingsFrame settingsFrame = new DefaultHttp2SettingsFrame(settings);
        
        // Write the frame to the channel
        channel.writeInbound(settingsFrame);
        
        // The frame should NOT be passed through (it's released inside the handler)
        Http2HeadersFrame outFrame = channel.readInbound();
        assertNull(outFrame, "Settings frame should not be propagated");
        
        channel.finishAndReleaseAll();
    }

    @Test(groups = { "unit" })
    public void testOtherMessagesPassedThrough() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        
        // Create a simple ByteBuf message
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes("test data".getBytes());
        
        // Write the message to the channel
        channel.writeInbound(buf);
        
        // The message should be passed through
        ByteBuf outBuf = channel.readInbound();
        assertNotNull(outBuf, "ByteBuf should be passed through");
        assertEquals(outBuf.readableBytes(), 9);
        
        // Clean up
        outBuf.release();
        channel.finishAndReleaseAll();
    }
}
