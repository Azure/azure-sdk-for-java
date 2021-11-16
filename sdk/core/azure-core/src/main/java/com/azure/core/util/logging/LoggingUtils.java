package com.azure.core.util.logging;

import com.azure.core.util.CoreUtils;

class LoggingUtils {
    private static final String NEW_LINE = System.lineSeparator();
    private static final int NEW_LINE_LENGTH = NEW_LINE.length();

    /*
     * Removes CRLF pattern in the {@code logMessage}.
     *
     * @param logMessage The log message to sanitize.
     * @return The updated logMessage.
     */
    static String sanitizeLogMessageInput(String logMessage) {
        if (CoreUtils.isNullOrEmpty(logMessage)) {
            return logMessage;
        }

        int i = 0;

        StringBuilder sb = null;
        int prevStart = 0;

        while (i <= logMessage.length() - NEW_LINE_LENGTH) {

            boolean match = true;
            for (int j = 0; match && (j < NEW_LINE_LENGTH); j++) {
                match = logMessage.charAt(i + j) == NEW_LINE.charAt(j);
            }

            if (match) {
                if (sb == null) {
                    sb = new StringBuilder(logMessage.length());
                }
                sb.append(logMessage, prevStart, i);
                i += NEW_LINE_LENGTH;
                prevStart = i;
            } else {
                i ++;
            }
        }

        if (sb == null) {
            return logMessage;
        }
        sb.append(logMessage, prevStart, logMessage.length());
        return sb.toString();
    }
}
