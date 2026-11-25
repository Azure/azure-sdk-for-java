// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.cosmos.implementation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Loads an explicitly selected JNI library. Bundled extraction is intentionally deferred.
 */
final class NativeLibraryLoader {
    static final NativeLibraryLoader INSTANCE = new NativeLibraryLoader(System::load);
    private final Consumer<String> linker;
    private Path loadedPath;

    NativeLibraryLoader(Consumer<String> linker) {
        this.linker = Objects.requireNonNull(linker, "'linker' cannot be null.");
    }

    synchronized void load(Path library) throws IOException {
        Path resolved = Objects.requireNonNull(library, "'library' cannot be null.").toRealPath();
        if (!Files.isRegularFile(resolved)) {
            throw new IllegalArgumentException("The Cosmos JNI library must be a regular file: " + resolved);
        }
        if (loadedPath != null) {
            if (!loadedPath.equals(resolved)) {
                throw new IllegalStateException("A different Cosmos JNI library is already loaded");
            }
            return;
        }
        // Record success only after linking; a failed explicit path must never trigger a fallback.
        linker.accept(resolved.toString());
        loadedPath = resolved;
    }
}
