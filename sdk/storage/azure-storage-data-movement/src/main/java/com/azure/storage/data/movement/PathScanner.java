// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

/**
 * Scanner for enumerating local files/folders at a specified location through publishers.
 *
 * TODO: Replace placeholder Javadoc
 */
public class PathScanner {
    private static final ClientLogger logger = new ClientLogger(PathScanner.class);
    private final String basePath;

    /**
     * Constructor for {@link PathScanner}.
     *
     * TODO: Replace placeholder Javadoc
     *
     * @param path The local path to be scanned, either relative to execution location or absolute.\
     */
    public PathScanner(String path) {
        // Resolve the given path to an absolute path in case it isn't one already
        Path pathObj = Paths.get(path);
        basePath = pathObj.toAbsolutePath().toString();

        // Check if the path exists; throw an error if there's nothing
        // present at the given path
        if (!(Files.isDirectory(pathObj) || Files.isRegularFile(pathObj))) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(String.format("No item(s) located at the path '%s'.", basePath)));
        }
    }

    /**
     * Enumerates the files/folders at the path this scanner points to using a non-blocking
     * publisher.
     *
     * TODO: Replace placeholder Javadoc comment
     *
     * @param continueOnError Sets whether to continue enumerating with warnings or throw through the
     *                           Flux when a folder can't be accessed.
     * @return a {@link Flux} containing the absolute paths of all matching entries.
     */
    public Flux<String> scan(boolean continueOnError) {
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
                        if (continueOnError) {
                            logger.warning(e.getMessage(), e);
                            return Mono.empty();
                        } else {
                            return Mono.error(logger.logThrowableAsError(e));
                        }
                    });
            }
        })
        // Return the paths as strings
        .map(path -> path.toAbsolutePath().toString());
    }
}
