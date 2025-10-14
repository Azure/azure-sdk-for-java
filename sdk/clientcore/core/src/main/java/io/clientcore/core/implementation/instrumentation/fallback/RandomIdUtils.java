// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// This code was copied from the OpenTelemetry Java SDK
// https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/trace/src/main/java/io/opentelemetry/sdk/trace/RandomIdGenerator.java
// and modified to fit the needs of the ClientCore library.
/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.clientcore.core.implementation.instrumentation.fallback;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class RandomIdUtils {
    public static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    public static final String INVALID_SPAN_ID = "0000000000000000";

    private static final int BYTE_BASE16 = 2;
    private static final long INVALID_ID = 0;
    private static final int TRACE_ID_HEX_LENGTH = 32;
    private static final int SPAN_ID_HEX_LENGTH = 16;
    private static final char[] ENCODING = buildEncodingArray();

    public static String generateSpanId() {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong();
        } while (id == INVALID_ID);
        return getSpanId(id);
    }

    public static String generateTraceId() {
        Random random = ThreadLocalRandom.current();
        long idHi = random.nextLong();
        long idLo;
        do {
            idLo = random.nextLong();
        } while (idLo == INVALID_ID);
        return getTraceId(idHi, idLo);
    }

    private static String getSpanId(long id) {
        if (id == 0) {
            return INVALID_SPAN_ID;
        }
        char[] result = new char[SPAN_ID_HEX_LENGTH];
        longToBase16String(id, result, 0);
        return new String(result, 0, SPAN_ID_HEX_LENGTH);
    }

    private static String getTraceId(long traceIdLongHighPart, long traceIdLongLowPart) {
        if (traceIdLongHighPart == 0 && traceIdLongLowPart == 0) {
            return INVALID_TRACE_ID;
        }
        char[] chars = new char[TRACE_ID_HEX_LENGTH];
        longToBase16String(traceIdLongHighPart, chars, 0);
        longToBase16String(traceIdLongLowPart, chars, 16);
        return new String(chars, 0, TRACE_ID_HEX_LENGTH);
    }

    private static void longToBase16String(long value, char[] dest, int destOffset) {
        byteToBase16((byte) (value >> 56 & 0xFFL), dest, destOffset);
        byteToBase16((byte) (value >> 48 & 0xFFL), dest, destOffset + BYTE_BASE16);
        byteToBase16((byte) (value >> 40 & 0xFFL), dest, destOffset + 2 * BYTE_BASE16);
        byteToBase16((byte) (value >> 32 & 0xFFL), dest, destOffset + 3 * BYTE_BASE16);
        byteToBase16((byte) (value >> 24 & 0xFFL), dest, destOffset + 4 * BYTE_BASE16);
        byteToBase16((byte) (value >> 16 & 0xFFL), dest, destOffset + 5 * BYTE_BASE16);
        byteToBase16((byte) (value >> 8 & 0xFFL), dest, destOffset + 6 * BYTE_BASE16);
        byteToBase16((byte) (value & 0xFFL), dest, destOffset + 7 * BYTE_BASE16);
    }

    private static void byteToBase16(byte value, char[] dest, int destOffset) {
        int b = value & 0xFF;
        dest[destOffset] = ENCODING[b];
        dest[destOffset + 1] = ENCODING[b | 0x100];
    }

    private static char[] buildEncodingArray() {
        String alphabet = "0123456789abcdef";
        char[] encoding = new char[512];
        for (int i = 0; i < 256; ++i) {
            encoding[i] = alphabet.charAt(i >>> 4);
            encoding[i | 0x100] = alphabet.charAt(i & 0xF);
        }
        return encoding;
    }
}
