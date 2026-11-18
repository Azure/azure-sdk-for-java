// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for the {@code Expect: 100-continue} request header.
 */
public final class ExpectContinueUtils {
    private static final String CONTINUE = "100-continue";

    /**
     * Checks whether the request contains a {@code 100-continue} expectation.
     *
     * @param headers The request headers.
     * @return Whether the expectation is present.
     */
    public static boolean isExpected(HttpHeaders headers) {
        for (String expectation : expectations(headers)) {
            if (CONTINUE.equalsIgnoreCase(expectation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the expectations other than {@code 100-continue}.
     *
     * @param headers The request headers.
     * @return The remaining expectations.
     */
    public static List<String> otherExpectations(HttpHeaders headers) {
        List<String> other = new ArrayList<>();
        for (String expectation : expectations(headers)) {
            if (!CONTINUE.equalsIgnoreCase(expectation)) {
                other.add(expectation);
            }
        }
        return other;
    }

    private static List<String> expectations(HttpHeaders headers) {
        HttpHeader header = headers == null ? null : headers.get(HttpHeaderName.EXPECT);
        if (header == null) {
            return Collections.emptyList();
        }

        List<String> expectations = new ArrayList<>();
        for (String value : header.getValues()) {
            int start = 0;
            boolean quoted = false;
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (quoted && character == '\\' && i + 1 < value.length()) {
                    i++;
                } else if (character == '"') {
                    quoted = !quoted;
                } else if (character == ',' && !quoted) {
                    addExpectation(expectations, value.substring(start, i));
                    start = i + 1;
                }
            }
            addExpectation(expectations, value.substring(start));
        }
        return expectations;
    }

    private static void addExpectation(List<String> expectations, String value) {
        String expectation = value.trim();
        if (!expectation.isEmpty()) {
            expectations.add(expectation);
        }
    }

    private ExpectContinueUtils() {
    }
}
