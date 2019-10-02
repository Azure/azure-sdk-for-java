// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import java.io.File;
import java.io.IOException;

/**
 * Code snippets for {@link ClientLogger} javadocs
 */
public class ClientLoggerJavaDocCodeSnippets {

    /**
     * Code snippets to show usage of {@link ClientLogger} at all log levels
     */
    public void loggingSnippets() {

        ClientLogger logger = new ClientLogger(ClientLoggerJavaDocCodeSnippets.class);
        String name = getName();

        // BEGIN: com.azure.core.util.logging.clientlogger.verbose
        logger.verbose("A formattable message. Hello, {}", name);
        // END: com.azure.core.util.logging.clientlogger.verbose

        // BEGIN: com.azure.core.util.logging.clientlogger.info
        logger.info("A formattable message. Hello, {}", name);
        // END: com.azure.core.util.logging.clientlogger.info

        // BEGIN: com.azure.core.util.logging.clientlogger.warning
        Throwable exception = new IllegalArgumentException("An invalid argument was encountered.");
        logger.warning("A formattable message. Hello, {}", name, exception);
        // END: com.azure.core.util.logging.clientlogger.warning

        File resource = getFile();
        // BEGIN: com.azure.core.util.logging.clientlogger.error
        try {
            upload(resource);
        } catch (IOException ex) {
            logger.error("A formattable message. Hello, {}", name, ex);
        }
        // END: com.azure.core.util.logging.clientlogger.error
    }

    /**
     * Implementation not provided
     * @return {@code null}
     */
    private File getFile() {
        return null;
    }

    /**
     * Implementation not provided
     * @return {@code null}
     */
    private String getName() {
        return null;
    }

    /**
     * Implementation not provided
     * @param resource A file resource
     * @throws IOException if upload fails
     */
    private void upload(File resource) throws IOException {
        throw new IOException();
    }
}
