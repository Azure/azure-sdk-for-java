// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.scheduler.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.*;

public final class PathScanner {
    private final ClientLogger logger = new ClientLogger(PathScanner.class);
    private final String basePath;

    public PathScanner(String path) {
        try {
            // Resolve the given path to an absolute path in case it isn't one already
            Path pathObj = Paths.get(path);
            basePath = pathObj.toAbsolutePath().toString();

            // Check if the path exists; throw an error if there's nothing
            // present at the given path
            if (!(Files.isDirectory(pathObj) || Files.isRegularFile(pathObj))) {
                throw new IllegalArgumentException(String.format("No item(s) located at the path '%s'.", basePath));
            }
        } catch (RuntimeException e) {
            // Possible throws: nonexistent item, malformed path
            throw logger.logExceptionAsError(e);
        }
    }

    public Flux<String> scan(boolean skipSubdirectories) {
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
            .map(path -> path.toAbsolutePath().toString())
            .subscribeOn(Schedulers.boundedElastic());
    }
}
