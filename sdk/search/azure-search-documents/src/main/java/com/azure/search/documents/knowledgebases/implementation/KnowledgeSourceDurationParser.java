// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases.implementation;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses duration values returned in knowledge source status responses.
 */
public final class KnowledgeSourceDurationParser {
    private static final Pattern COMPACT_DURATION = Pattern.compile("^(\\d+)([dhms])$");

    private KnowledgeSourceDurationParser() {
    }

    /**
     * Parses an ISO-8601 duration or a compact service duration such as {@code 1d}.
     *
     * @param value The duration value.
     * @return The parsed duration.
     * @throws NullPointerException If {@code value} is null.
     * @throws DateTimeParseException If {@code value} isn't a supported duration.
     */
    public static Duration parse(String value) {
        Objects.requireNonNull(value, "'value' cannot be null.");

        try {
            return Duration.parse(value);
        } catch (DateTimeParseException isoException) {
            return parseCompactDuration(value, isoException);
        }
    }

    private static Duration parseCompactDuration(String value, DateTimeParseException isoException) {
        Matcher matcher = COMPACT_DURATION.matcher(value);
        if (!matcher.matches()) {
            throw isoException;
        }

        try {
            long amount = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
                case "d":
                    return Duration.ofDays(amount);

                case "h":
                    return Duration.ofHours(amount);

                case "m":
                    return Duration.ofMinutes(amount);

                case "s":
                    return Duration.ofSeconds(amount);

                default:
                    throw isoException;
            }
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new DateTimeParseException("Knowledge source duration is too large.", value, 0, exception);
        }
    }
}
