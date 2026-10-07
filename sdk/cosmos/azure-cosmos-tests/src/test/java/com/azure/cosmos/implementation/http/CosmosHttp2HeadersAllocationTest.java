// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.sun.management.ThreadMXBean;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.Reporter;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosHttp2HeadersAllocationTest {
    private static volatile Http2Headers lastHeaders;
    private static final AsciiString SERVER_VERSION = AsciiString.cached("x-ms-serviceversion");

    @Test(groups = "unit")
    public void unpaddedHeadersDoNotAddDecodeAllocations() throws Exception {
        assertAllocationParity("version", false);
    }

    @Test(groups = "unit")
    public void paddedHeadersDoNotExceedLegacyDecodeAndCleanupAllocations() throws Exception {
        assertAllocationParity(" version ", true);
    }

    private static void assertAllocationParity(String value, boolean legacyCleanup) throws Exception {
        java.lang.management.ThreadMXBean management = ManagementFactory.getThreadMXBean();
        if (!(management instanceof ThreadMXBean) || !((ThreadMXBean) management).isThreadAllocatedMemorySupported()) {
            throw new SkipException("The JVM does not support per-thread allocation measurement");
        }
        ThreadMXBean bean = (ThreadMXBean) management;
        bean.setThreadAllocatedMemoryEnabled(true);
        ByteBuf block = headerBlock(value);
        try {
            DefaultHttp2HeadersDecoder baseline = new DefaultHttp2HeadersDecoder(true, !legacyCleanup, 8192);
            CosmosHttp2HeadersDecoder cosmos = new CosmosHttp2HeadersDecoder(8192);
            run(baseline, block, legacyCleanup, 100000);
            run(cosmos, block, false, 100000);
            double baselineBytes = measure(bean, baseline, block, legacyCleanup);
            double cosmosBytes = measure(bean, cosmos, block, false);
            Reporter.log("HTTP/2 decode allocation: padded=" + legacyCleanup + ", baseline=" + baselineBytes
                + " bytes/block, Cosmos=" + cosmosBytes + " bytes/block", true);
            assertThat(cosmosBytes).as("Cosmos bytes/header block versus Netty baseline %.1f", baselineBytes)
                .isLessThanOrEqualTo(baselineBytes);
            assertThat(lastHeaders.get(SERVER_VERSION).toString()).isEqualTo("version");
        } finally {
            block.release();
        }
    }

    private static double measure(ThreadMXBean bean, DefaultHttp2HeadersDecoder decoder, ByteBuf block,
                                  boolean legacyCleanup) throws Exception {
        double[] samples = new double[5];
        for (int i = 0; i < samples.length; i++) {
            long start = bean.getThreadAllocatedBytes(Thread.currentThread().getId());
            run(decoder, block, legacyCleanup, 50000);
            samples[i] = (bean.getThreadAllocatedBytes(Thread.currentThread().getId()) - start) / 50000.0;
        }
        Arrays.sort(samples);
        return samples[samples.length / 2];
    }

    private static void run(DefaultHttp2HeadersDecoder decoder, ByteBuf block, boolean legacyCleanup, int count)
        throws Exception {
        for (int i = 0; i < count; i++) {
            block.readerIndex(0);
            Http2Headers headers = decoder.decodeHeaders(1, block);
            if (legacyCleanup) {
                headers.set(SERVER_VERSION, headers.get(SERVER_VERSION).toString().trim());
            }
            lastHeaders = headers;
        }
    }

    private static ByteBuf headerBlock(String value) {
        ByteBuf block = Unpooled.buffer();
        block.writeByte(0).writeByte(7).writeCharSequence(":status", StandardCharsets.US_ASCII);
        block.writeByte(3).writeCharSequence("200", StandardCharsets.US_ASCII);
        block.writeByte(0).writeByte(SERVER_VERSION.length()).writeCharSequence(SERVER_VERSION, StandardCharsets.US_ASCII);
        block.writeByte(value.length()).writeCharSequence(value, StandardCharsets.US_ASCII);
        return block;
    }
}
