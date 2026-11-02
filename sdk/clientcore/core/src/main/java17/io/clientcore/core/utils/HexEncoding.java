// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import java.util.HexFormat;

/**
 * Hexadecimal formatting selected for the running Java version.
 */
final class HexEncoding {
    private static final HexFormat FORMAT = HexFormat.of();

    private HexEncoding() {
    }

    static String toHexString(byte[] bytes) {
        return FORMAT.formatHex(bytes);
    }
}
