// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Utility class for testing {@code azure-core}.
 */
public final class CoreTestUtils {
    /**
     * Convenience method for creating {@link URL} now that as of Java 20+ all {@link URL} constructors are deprecated.
     * <p>
     * This uses the logic {@code URI.create(String).toURL()}, which is recommended instead of the URL constructors.
     *
     * @param urlString The URL string.
     * @return The URL representing the URL string.
     * @throws MalformedURLException If the URL string isn't a valid URL.
     */
    public static URL createUrl(String urlString) throws MalformedURLException {
        return URI.create(urlString).toURL();
    }

    private CoreTestUtils() {
    }
}
