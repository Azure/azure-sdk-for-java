// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NettyHttpHeaderTests {
    @Test
    public void addValue() {
        io.netty.handler.codec.http.HttpHeaders nettyHeaders = new DefaultHttpHeaders();
        final HttpHeaders headers = new ReactorNettyHttpResponseBase.NettyHttpHeaders(nettyHeaders);

        final HttpHeader header = new ReactorNettyHttpResponseBase.NettyHttpHeader(headers, "a", "b");
        header.addValue("c");
        assertEquals("a:b,c", header.toString());
    }
}
