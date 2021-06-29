// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement;

/**
 * Factory for constructing {@link PathScanner}. Call {@link #getPathScanner()} to create a path scanner with the
 * path passed to constructor.
 *
 * TODO: Replace placeholder Javadoc comment
 */
public final class PathScannerFactory {
    private final String path;

    public PathScannerFactory(String path) {
        this.path = path;
    }

    /**
     * Creates a {@link PathScanner} with the configured path.
     *
     * TODO: Replace placeholder Javadoc comment
     *
     * @return a {@link PathScanner} created from the path passed to this factory.
     * @throws IllegalArgumentException if the provided path does not point to anything.
     * @throws java.nio.file.InvalidPathException if the provided path is malformed/cannot be resolved to absolute.
     */
    public PathScanner getPathScanner() {
        return new PathScanner(path);
    }
}
