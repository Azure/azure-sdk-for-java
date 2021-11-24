// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.util.CoreUtils;

/**
 * Contains utility methods for logging.
 */
public final class LoggingUtils {
    private static final char CR = '\r';
    private static final char LF = '\n';
    
    private LoggingUtils() {
    }

    /*
     * Removes CRLF pattern in the {@code logMessage}.
     *
     * @param logMessage The log message to sanitize.
     * @return The updated logMessage.
     */
    public static String sanitizeLogMessageInput(String logMessage) {
        if (CoreUtils.isNullOrEmpty(logMessage)) {
            return logMessage;
        }

        StringBuilder sb = null;
        int prevStart = 0;

        for (int i = 0; i < logMessage.length(); i++) {
            if (logMessage.charAt(i) == CL || logMessage.charAt(i) == RF) {
                if (sb == null) {
                    sb = new StringBuilder(logMessage.length());
                }

                if (prevStart != i) {
                    sb.append(logMessage, prevStart, i);
                }
                prevStart = i + 1;
            }
        }

        if (sb == null) {
            return logMessage;
        }
        sb.append(logMessage, prevStart, logMessage.length());
        return sb.toString();
    }
}
