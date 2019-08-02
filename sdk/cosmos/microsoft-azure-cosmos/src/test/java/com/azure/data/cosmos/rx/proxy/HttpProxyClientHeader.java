// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * The http header of client.
 *
 */
public class HttpProxyClientHeader {
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

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isHttps() {
        return https;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public StringBuilder getLineBuf() {
        return lineBuf;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void digest(ByteBuf in) {
        while (in.isReadable()) {
            if (complete) {
                throw new IllegalStateException("already complete");
            }

            String line = readLine(in);
            if (line == null) {
                return;
            }

            if (method == null) {
                method = line.split(" ")[0]; // the first word is http method name
                https = method.equalsIgnoreCase("CONNECT"); // method CONNECT means https
            }

            if (line.startsWith("Host: ") || line.startsWith("host: ")) {
                String[] arr = line.split(":");
                host = arr[1].trim();
                if (arr.length == 3) {
                    port = Integer.parseInt(arr[2]);
                } else if (https) {
                    port = 443; // https
                } else {
                    port = 80; // http
                }
            }

            if (line.isEmpty()) {
                if (host == null || port == 0) {
                    throw new IllegalStateException("cannot find header \'Host\'");
                }

                byteBuf = byteBuf.asReadOnly();
                complete = true;
                break;
            }
        }
    }

    private String readLine(ByteBuf in) {
        while (in.isReadable()) {
            byte b = in.readByte();
            byteBuf.writeByte(b);
            lineBuf.append((char) b);
            int len = lineBuf.length();
            if (len >= 2 && lineBuf.substring(len - 2).equals("\r\n")) {
                String line = lineBuf.substring(0, len - 2);
                lineBuf.delete(0, len);
                return line;
            }

        }
        return null;
    }
}
