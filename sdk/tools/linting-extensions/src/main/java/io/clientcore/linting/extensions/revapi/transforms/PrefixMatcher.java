// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.revapi.transforms;

import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link Predicate} implementation that matches an input against an initial prefix and an optional list of
 * sub-prefixes.
 * <p>
 * The initial prefix must always be matched to be able to return true, then if there are any sub-prefixes only one of
 * those must be matched to return true. If sub-prefixes are empty then only the initial prefix must be matched.
 */
final class PrefixMatcher implements Predicate<String> {
    private final String initialPrefix;
    private final int initialPrefixLength;
    private final List<String> subPrefixes;

    PrefixMatcher(String initialPrefix, List<String> subPrefixes) {
        this.initialPrefix = initialPrefix;
        this.initialPrefixLength = initialPrefix.length();
        this.subPrefixes = subPrefixes;
    }

    @Override
    public boolean test(String s) {
        if (s == null) {
            return false;
        }

        if (!s.startsWith(initialPrefix)) {
            return false;
        }

        if (subPrefixes.isEmpty()) {
            return true;
        }

        for (String subPrefix : subPrefixes) {
            if (s.regionMatches(initialPrefixLength, subPrefix, 0, subPrefix.length())) {
                return true;
            }
        }

        return false;
    }
}
