// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.cosmos.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeLibraryLoaderTests {
    @TempDir
    Path directory;

    @Test
    void missingExplicitLibraryDoesNotFallBackToSearchPaths() {
        assertThrows(IOException.class, () -> NativeLibraryLoader.INSTANCE.load(directory.resolve("missing")));
    }

    @Test
    void directoryIsNotALibrary() {
        assertThrows(IllegalArgumentException.class, () -> NativeLibraryLoader.INSTANCE.load(directory));
    }

    @Test
    void loadsCanonicalPathOnceAndRejectsReplacement() throws IOException {
        List<String> linked = new ArrayList<>();
        NativeLibraryLoader loader = new NativeLibraryLoader(linked::add);
        Path first = Files.createFile(directory.resolve("first"));
        Path second = Files.createFile(directory.resolve("second"));
        loader.load(first);
        loader.load(directory.resolve(".").resolve("first"));
        assertThrows(IllegalStateException.class, () -> loader.load(second));
        assertEquals(List.of(first.toRealPath().toString()), linked);
    }

    @Test
    void failedLinkCanBeRetriedWithoutCachingFailure() throws IOException {
        AtomicInteger attempts = new AtomicInteger();
        NativeLibraryLoader loader = new NativeLibraryLoader(path -> {
            if (attempts.getAndIncrement() == 0) {
                throw new UnsatisfiedLinkError("test link failure");
            }
        });
        Path library = Files.createFile(directory.resolve("retry"));
        assertThrows(UnsatisfiedLinkError.class, () -> loader.load(library));
        loader.load(library);
        loader.load(library);
        assertEquals(2, attempts.get());
    }
}
