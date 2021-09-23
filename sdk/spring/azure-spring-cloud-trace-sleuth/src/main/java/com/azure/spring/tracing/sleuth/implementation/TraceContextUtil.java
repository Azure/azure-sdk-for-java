// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import org.springframework.cloud.sleuth.TraceContext;

public class TraceContextUtil {

    public static boolean isValid(TraceContext context) {
        return TraceId.isValid(context.traceId()) && SpanId.isValid(context.spanId());
    }

    /**
     * Returns whether the {@link CharSequence} is a valid hex string.
     */
    public static boolean isValidBase16String(CharSequence value) {
        for (int i = 0; i < value.length(); i++) {
            char b = value.charAt(i);
            // 48..57 && 97..102 are valid
            if (!isDigit(b) && !isLowercaseHexCharacter(b)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDigit(char b) {
        return 48 <= b && b <= 57;
    }

    private static boolean isLowercaseHexCharacter(char b) {
        return 97 <= b && b <= 102;
    }
}
