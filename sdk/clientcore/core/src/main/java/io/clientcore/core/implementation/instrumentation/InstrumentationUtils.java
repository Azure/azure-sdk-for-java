// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import java.net.URI;

/**
 * Utility class for instrumentation.
 */
public class InstrumentationUtils {

    /**
     * Does the best effort to capture the server port with minimum perf overhead.
     * If port is not set, we check scheme for "http" and "https" (case-sensitive).
     * If scheme is not one of those, returns -1.
     *
     * @param uri request URI
     */
    public static int getServerPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            switch (uri.getScheme()) {
                case "http":
                    return 80;

                case "https":
                    return 443;

                default:
                    break;
            }
        }
        return port;
    }

    private InstrumentationUtils() {
    }
}
