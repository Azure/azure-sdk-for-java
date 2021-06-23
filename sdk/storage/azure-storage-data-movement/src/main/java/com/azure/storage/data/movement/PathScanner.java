// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.util.logging.ClientLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

public class PathScanner {
    private final ClientLogger logger = new ClientLogger(PathScanner.class);
    private final String basePath;
    private final boolean isDir;

    public PathScanner(String path) {
        // Resolve the given path to an absolute path in case it isn't one already
        Path pathObj = Paths.get(path);
        basePath = pathObj.toAbsolutePath().toString();

        // Check if the path exists and whether or not it's a directory; throw
        // an error if there's nothing present or readable at the given path
        if (Files.exists(pathObj)) {
            isDir = Files.isDirectory(pathObj);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format("No accessible item exists at the path '%s'.", basePath)));
        }
    }

    public Flux<String> scan(boolean skipSubdirectories) {
        // Iterate through the path if it's a directory
        if (isDir) {
            // Set up Mono for recursing through directories
            Mono<Path> basePath = Mono.just(Paths.get(this.basePath));

            // Return a Flux constructed from expanding path
            return basePath.expand(path -> {
                // Return an empty publisher when the path is not a directory
                // (files will cause errors when being used with Files::list)
                if (!Files.isDirectory(path)) {
                    return Mono.empty();
                } else {
                    return Flux.using(() -> Files.list(path),
                        Flux::fromStream,
                        BaseStream::close)
                        .onErrorResume(e -> {
                            // If set to skip subdirectories, continue processing; else,
                            // pass an error which will stop the Flux stream
                            if (skipSubdirectories) {
                                logger.warning(e.getMessage(), e);
                                return Mono.empty();
                            } else {
                                return Mono.error(logger.logThrowableAsError(e));
                            }
                        });
                }})
                // Return the paths as strings
                .map(path -> path.toAbsolutePath().toString());
        } else {
            // Otherwise, just return the path alone in the Flux
            return Flux.just(basePath);
        }
    }
}
