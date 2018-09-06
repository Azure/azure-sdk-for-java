/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provide functionality to parse resource paths in the Azure Cosmos DB database service.
 */
public final class PathParser {

    private static final char segmentSeparator = '/';

    public static Collection<String> getPathParts(String path) {
        ArrayList<String> tokens = new ArrayList<String>();
        int currentIndex = 0;

        while (currentIndex < path.length()) {
            if (path.charAt(currentIndex) != segmentSeparator) {
                throw new IllegalArgumentException(String.format("Invalid path, failed at index %d.", currentIndex));
            }

            if (++currentIndex == path.length())
                break;

            if (path.charAt(currentIndex) == '\"' || path.charAt(currentIndex) == '\'') {
                char quote = path.charAt(currentIndex);
                int newIndex = ++currentIndex;
                while (true) {
                    newIndex = path.indexOf(quote, newIndex);
                    if (newIndex == -1) {
                        throw new IllegalArgumentException(String.format("Invalid path, failed at index %d.", currentIndex));
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
