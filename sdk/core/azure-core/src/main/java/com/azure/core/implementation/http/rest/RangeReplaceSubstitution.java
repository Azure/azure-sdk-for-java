// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.util.CoreUtils;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A range replace substitution is a substitution that replaces a range of characters in a String with the supplied
 * value. This type of substitution is commonly used for host and path replacements where the Swagger interface will
 * have a definition such as {@code @Host("{url}")} which will be replaced with a value such as
 * {@code https://myhost.com}.
 * <p>
 * Before the introduction of this replacement {@link String#replace(CharSequence, CharSequence)} was used which would
 * generate a {@link Pattern} to perform replacing.
 */
public class RangeReplaceSubstitution extends Substitution {
    private final String substitutionBase;
    private final String placeholder;
    private final Set<Range> ranges;

    /**
     * Create a new Substitution.
     *
     * @param urlParameterName The name that is used between curly quotes as a placeholder in the target URL.
     * @param methodParameterIndex The index of the parameter in the original interface method where the value for the
     * placeholder is.
     * @param shouldEncode Whether the value from the method's argument should be encoded when the substitution is
     * taking place.
     * @param substitutionBase The string that will have its ranges substituted.
     */
    public RangeReplaceSubstitution(String urlParameterName, int methodParameterIndex, boolean shouldEncode,
        String substitutionBase) {
        super(urlParameterName, methodParameterIndex, shouldEncode);
        this.substitutionBase = substitutionBase;
        this.placeholder = "{" + urlParameterName + "}";
        this.ranges = new TreeSet<>();

        int indexOf = 0;
        while (true) {
            indexOf = substitutionBase.indexOf(placeholder, indexOf);

            if (indexOf == -1) {
                break;
            }

            ranges.add(new Range(indexOf, indexOf + placeholder.length()));
            indexOf = indexOf + placeholder.length();
        }
    }

    /**
     * Replaces all ranges with the specified {@code replaceValue}.
     *
     * @param replaceValue The value to replace ranges with.
     * @return The resulting string with ranges replaced with the specified value.
     * @throws NullPointerException If {@code replaceValue} is null.
     */
    public String replace(String replaceValue) {
        if (CoreUtils.isNullOrEmpty(ranges)) {
            return substitutionBase;
        }

        int initialLength = substitutionBase.length();

        // Replacement placeholder is the parameter name wrapped with '{' '}', '{<parameter name>}'
        int placeholderLength = placeholder.length();

        // Replaced string size should be the String size plus (replaced - placeholder) * replace_count.
        // For example if the replacement is 'hello' and the placeholder is '{message}' the resulting replaced
        // string will be smaller than the initial size.
        StringBuilder replaced = new StringBuilder(Math.max(0, initialLength
            - ((placeholderLength - replaceValue.length()) * ranges.size())));

        int last = 0;
        for (Range range : ranges) {
            if (range.end > initialLength) {
                continue;
            }

            if (last < range.start) {
                replaced.append(substitutionBase, last, range.start);
            }

            replaced.append(replaceValue);
            last = range.end;
        }

        if (last < initialLength) {
            replaced.append(substitutionBase, last, initialLength);
        }

        return replaced.toString();
    }

    private static final class Range implements Comparable<Range> {
        final int start;
        final int end;

        Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(Range o) {
            if (start < o.start) {
                return -1;
            } else if (start > o.start) {
                return 1;
            } else {
                return Integer.compare(end, o.end);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Range)) {
                return false;
            }

            Range other = (Range) obj;

            return start == other.start && end == other.end;
        }
    }
}
