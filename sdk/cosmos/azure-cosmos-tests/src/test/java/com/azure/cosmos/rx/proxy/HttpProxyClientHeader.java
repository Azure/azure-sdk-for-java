// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;

/**
 * The http header of client.
 *
 */
public class HttpProxyClientHeader {
    private static final int MAX_HEADER_BYTES = 64 * 1024;

    private static final boolean LEAK_DEBUG =
        ResourceLeakDetector.getLevel().ordinal() >= ResourceLeakDetector.Level.ADVANCED.ordinal();

    private volatile boolean touchedAlloc;

    private String method;
    private String host;
    private int port;
    private boolean https;
    private boolean complete;
    private ByteBuf byteBuf = Unpooled.buffer();

    private final StringBuilder lineBuf = new StringBuilder();

    public boolean isComplete() {
        return complete;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public boolean isHttps() {
        return https;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public ByteBuf getByteBuf() {
        if (!complete) throw new IllegalStateException("header not complete");
        if (LEAK_DEBUG) {
            this.byteBuf.touch("getByteBuf handoff");
        }
        ByteBuf out = this.byteBuf;
        this.byteBuf = Unpooled.EMPTY_BUFFER; // we no longer own anything
        return out; // caller must write/release this
    }

    public void setComplete(boolean complete) {
        if (complete) {
            if (LEAK_DEBUG) {
                this.byteBuf.touch("end-of-headers; https=" + https + " host=" + host + " port=" + port);
            }
        }
        this.complete = complete;
    }

    /** Release any internal buffer we still own (idempotent). */
    public void releaseQuietly() {
        if (LEAK_DEBUG) {
            this.byteBuf.touch("releaseQuietly");
        }
        io.netty.util.ReferenceCountUtil.safeRelease(this.byteBuf);
        this.byteBuf = Unpooled.EMPTY_BUFFER;
    }

    public void digest(ByteBuf in) {
        if (LEAK_DEBUG && !touchedAlloc) {
            touchedAlloc = true;
            this.byteBuf.touch("allocated header buffer");
        }

        while (in.isReadable()) {
            if (complete) throw new IllegalStateException("already complete");

            String line = readLine(in);
            if (line == null) return;

            if (method == null) {
                method = line.split(" ", 2)[0];
                https = "CONNECT".equalsIgnoreCase(method);
            }

            if (line.regionMatches(true, 0, "Host:", 0, 5)) {
                // be tolerant to extra spaces and IPv6
                String value = line.substring(5).trim();
                int idx = value.lastIndexOf(':'); // last colon to allow IPv6 literals
                if (idx > 0 && value.indexOf(']') < idx) {
                    host = value.substring(0, idx).trim();
                    port = Integer.parseInt(value.substring(idx + 1).trim());
                } else {
                    host = value;
                    port = https ? 443 : 80;
                }
            }

            if (line.isEmpty()) {
                if (host == null || port == 0) {
                    releaseQuietly();
                    throw new IllegalStateException("cannot find header 'Host'");
                }
                // If HTTPS, we don’t forward the CONNECT request → release now.
                if (https) {
                    releaseQuietly();
                } else {
                    // non-HTTPS: make read-only for safety; caller will drain & own it
                    byteBuf = byteBuf.asReadOnly();
                }
                setComplete(true);
                break;
            }

            // size guard to avoid OOM from giant headers
            if (byteBuf.writerIndex() >= MAX_HEADER_BYTES) {
                releaseQuietly();
                if (LEAK_DEBUG) {
                    this.byteBuf.touch("header too large at writerIndex=" + byteBuf.writerIndex());
                }
                throw new IllegalStateException("header too large");
            }
        }
    }

    private String readLine(ByteBuf in) {
        while (in.isReadable()) {
            byte b = in.readByte();
            byteBuf.writeByte(b);
            lineBuf.append((char) b);
            int len = lineBuf.length();
            if (len >= 2 && lineBuf.charAt(len - 2) == '\r' && lineBuf.charAt(len - 1) == '\n') {
                String line = lineBuf.substring(0, len - 2);
                lineBuf.setLength(0);
                if (LEAK_DEBUG) {
                    this.byteBuf.touch("CRLF reached, writerIndex=" + byteBuf.writerIndex());
                }
                return line;
            }
        }
        return null;
    }
}
