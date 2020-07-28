// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import java.io.File;
import java.io.IOException;

/**
 * The wrapper class for file related operations.
 */
public class FileProvider {
    /**
     * Prepares the location for file to be created.
     * @param file the file to be created.
     * @throws IOException thrown when failed on IO.
     */
    public void prepareFileLocation(File file) throws IOException {
        // do nothing
    }

    /**
     * Prepares the location for file to be created.
     * @param files the files to be created.
     * @throws IOException thrown when failed on IO.
     */
    public void prepareFileLocation(File... files) throws IOException {
        for (File file : files) {
            prepareFileLocation(file);
        }
    }
}
