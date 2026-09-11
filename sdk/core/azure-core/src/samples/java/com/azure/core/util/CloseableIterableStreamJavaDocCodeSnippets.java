// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Code snippets for {@link CloseableIterableStream}.
 */
public class CloseableIterableStreamJavaDocCodeSnippets {
    /**
     * Iterates over values backed by a closeable resource and transfers ownership to the stream.
     */
    public void iterateValues() {
        // BEGIN: com.azure.core.util.closeableIterableStream.iterate
        BufferedReader reader = getReader();
        Iterable<String> lines = () -> reader.lines().iterator();

        try (CloseableIterableStream<String> stream = new CloseableIterableStream<>(lines, reader)) {
            for (String line : stream) {
                System.out.println(line);
            }
        }
        // END: com.azure.core.util.closeableIterableStream.iterate
    }

    private BufferedReader getReader() {
        return new BufferedReader(new StringReader("one\ntwo"));
    }
}
