/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import org.apache.commons.lang3.StringUtils;

class Configs {

    private static final String MAX_HTTP_BODY_LENGTH_IN_BYTES = "MAX_HTTP_BODY_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES = "MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_CHUNK_SIZE_IN_BYTES = "MAX_HTTP_CHUNK_SIZE_IN_BYTES";
    private static final String MAX_HTTP_HEADER_SIZE_IN_BYTES = "MAX_HTTP_HEADER_SIZE_IN_BYTES";

    private static final int DEFAULT_MAX_HTTP_BODY_LENGTH_IN_BYTES = 2 * 1024 * 1024; //2MB
    private static final int DEFAULT_MAX_HTTP_INITIAL_LINE_LENGTH = 4096; //4KB
    private static final int DEFAULT_MAX_HTTP_CHUNK_SIZE_IN_BYTES = 8192; //8KB
    private static final int DEFAULT_MAX_HTTP_REQUEST_HEADER_SIZE = 32 * 1024; //32 KB

    public int getMaxHttpHeaderSize() {
        return getJVMConfigAsInt(MAX_HTTP_HEADER_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_REQUEST_HEADER_SIZE);
    }

    public int getMaxHttpInitialLineLength() {
        return getJVMConfigAsInt(MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_INITIAL_LINE_LENGTH);
    }

    public int getMaxHttpChunkSize() {
        return getJVMConfigAsInt(MAX_HTTP_CHUNK_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_CHUNK_SIZE_IN_BYTES);
    }

    public int getMaxHttpBodyLength() {
        return getJVMConfigAsInt(MAX_HTTP_BODY_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_BODY_LENGTH_IN_BYTES);
    }

    private static int getJVMConfigAsInt(String propName, int defaultValue) {
        String propValue = System.getProperty(propName);
        return getIntValue(propValue, defaultValue);
    }

    private static int getIntValue(String val, int defaultValue) {
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        } else {
            return Integer.valueOf(val);
        }
    }
}
