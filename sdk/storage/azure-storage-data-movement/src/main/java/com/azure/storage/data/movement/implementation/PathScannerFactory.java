// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement.implementation;

/**
 * Factory for constructing {@link PathScanner}. Call {@link #getPathScanner()} to create a path scanner with the
 * path passed to constructor.
 *
 * TODO: Replace placeholder Javadoc comment
 */
public final class PathScannerFactory {
    private final String path;

    /**
     * Constructor for {@link PathScannerFactory}.
     *
     * TODO: Replace placeholder Javadoc
     *
     * @param path The local path to be scanned, either relative to execution location or absolute.
     */
    public PathScannerFactory(String path) {
        this.path = path;
    }

    /**
     * Creates a {@link PathScanner} with the configured path.
     *
     * TODO: Replace placeholder Javadoc comment
     *
     * @return a {@link PathScanner} created from the path passed to this factory.
     */
    public PathScanner getPathScanner() {
        return new PathScanner(path);
    }
}
