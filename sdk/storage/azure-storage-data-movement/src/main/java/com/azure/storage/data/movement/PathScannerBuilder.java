// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathScannerBuilder {
    private final String path;

    public PathScannerBuilder(String path) {
        this.path = path;
    }

    public PathScanner BuildPathScanner() {
        // Check if the path exists and whether or not it's a directory; throw
        // an error if there's nothing present or readable at the given path
        Path pathObj = Paths.get(path);
        if (Files.exists(pathObj)) {
            return new PathScanner(path, Files.isDirectory(pathObj));
        } else {
            throw new IllegalArgumentException("No accessible object exists at the given path");
        }
    }
}
