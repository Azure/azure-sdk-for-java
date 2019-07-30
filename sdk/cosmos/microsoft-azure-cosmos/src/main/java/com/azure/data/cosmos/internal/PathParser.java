// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide functionality to parse resource paths in the Azure Cosmos DB database service.
 */
public final class PathParser {

    private static final char segmentSeparator = '/';

    public static List<String> getPathParts(String path) {
        ArrayList<String> tokens = new ArrayList<String>();
        int currentIndex = 0;

        while (currentIndex < path.length()) {
            if (path.charAt(currentIndex) != segmentSeparator) {
                throw new IllegalArgumentException(String.format("INVALID path, failed at index %d.", currentIndex));
            }

            if (++currentIndex == path.length())
                break;

            if (path.charAt(currentIndex) == '\"' || path.charAt(currentIndex) == '\'') {
                char quote = path.charAt(currentIndex);
                int newIndex = ++currentIndex;
                while (true) {
                    newIndex = path.indexOf(quote, newIndex);
                    if (newIndex == -1) {
                        throw new IllegalArgumentException(String.format("INVALID path, failed at index %d.", currentIndex));
                    }

                    if (path.charAt(newIndex - 1) != '\\') {
                        break;
                    }

                    ++newIndex;
                }

                String token = path.substring(currentIndex, newIndex);
                tokens.add(token);
                currentIndex = newIndex + 1;
            } else {
                int newIndex = path.indexOf(segmentSeparator, currentIndex);
                String token = null;
                if (newIndex == -1) {
                    token = path.substring(currentIndex);
                    currentIndex = path.length();
                } else {
                    token = path.substring(currentIndex, newIndex);
                    currentIndex = newIndex;
                }

                token = token.trim();
                tokens.add(token);
            }
        }

        return tokens;
    }
}
