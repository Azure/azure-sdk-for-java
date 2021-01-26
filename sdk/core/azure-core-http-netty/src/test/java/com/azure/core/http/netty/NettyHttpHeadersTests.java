// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.netty.implementation.NettyToAzureCoreHttpHeadersWrapper;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NettyHttpHeadersTests {
//    @Test
//    public void testSet() {
//        io.netty.handler.codec.http.HttpHeaders nettyHeaders = new DefaultHttpHeaders();
//
//        final HttpHeaders headers = new NettyToAzureCoreHttpHeadersWrapper(nettyHeaders);
//
//        headers.set("a", "b");
//        assertEquals("b", headers.getValue("a"));
//
//        headers.set("a", "c");
//        assertEquals("c", headers.getValue("a"));
//
//        headers.set("a", (String) null);
//        assertNull(headers.getValue("a"));
//
//        headers.set("A", "");
//        assertEquals("", headers.getValue("a"));
//
//        headers.set("A", "b");
//        assertEquals("b", headers.getValue("A"));
//
//        headers.set("a", (String) null);
//        assertNull(headers.getValue("a"));
//    }
//
//    @Test
//    public void testToStringShouldBeRepresentingKeyEqualsignValue() {
//        final HttpHeaders headers = new HttpHeaders();
//        headers.set("key1", "value1");
//        headers.set("key2", "value2");
//        headers.set("key3", "value3");
//
//        assertEquals("key1=value1, key2=value2, key3=value3", headers.toString());
//    }
}
