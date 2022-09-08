// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import java.util.List;

import static java.util.Collections.singletonList;

public final class Exceptions {

    public static List<ExceptionDetailBuilder> minimalParse(String str) {
        ExceptionDetailBuilder builder = new ExceptionDetailBuilder();
        int separator = -1;
        int length = str.length();
        int current;
        for (current = 0; current < length; current++) {
            char c = str.charAt(current);
            if (c == ':' && separator == -1) {
                separator = current;
            } else if (c == '\r' || c == '\n') {
                break;
            }
        }
        // at the end of the loop, current will be end of the first line
        if (separator != -1) {
            String typeName = str.substring(0, separator);
            String message = str.substring(separator + 1, current).trim();
            if (message.isEmpty()) {
                message = typeName;
            }
            builder.setTypeName(typeName);
            builder.setMessage(message);
        } else {
            String typeName = str.substring(0, current);
            builder.setTypeName(typeName);
            builder.setMessage(typeName);
        }
        builder.setStack(str);
        return singletonList(builder);
    }

    private Exceptions() {
    }
}
