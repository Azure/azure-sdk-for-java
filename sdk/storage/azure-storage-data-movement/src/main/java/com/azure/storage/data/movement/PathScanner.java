// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public class PathScanner {
    private final String basePath;
    private final boolean isDir;

    public PathScanner(String path, boolean isDir) {
        basePath = path;
        this.isDir = isDir;
    }

    public Flux<String> scan(boolean skipSubdirectories) {
        // Iterate through the path if it's a directory
        if (isDir) {
            // Set up Mono for recursing through directories
            Mono<Path> basePath = Mono.just(Paths.get(this.basePath));

            // Return a Flux constructed from expanding path
            return basePath.expand(path ->
                Flux.using(() -> Files.list(path),
                    Flux::fromStream,
                    BaseStream::close)
                    .onErrorResume((e) -> {
                        // Ignore errors resulting from trying to enumerate contents of
                        // a file as if it were a directory, but log other errors
                        if (!(e instanceof NotDirectoryException))
                            System.err.println(e);

                        // If set to skip subdirectories, continue processing; else,
                        // pass an error which will stop the Flux stream
                        if (skipSubdirectories)
                            return Mono.empty();
                        else
                            return Mono.error(e);
                    }))
                // Remove the folders from the final returned values
                .filter(path -> !Files.isDirectory(path))
                // Return the paths as strings
                .map(path -> path.toAbsolutePath().toString());
        } else {
            // Otherwise, just return the path alone in the Flux
            return Flux.just(basePath);
        }
    }
}
