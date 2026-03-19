// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SampleUtils {

    /**
     * Gets the path to a file in the sample resource folder.
     * @param fileName the name of the file in the sample resource folder
     * @return Path to the sample resource file
     */
    public static Path getResourcePath(String fileName) {
        try {
            URL resourceUrl = SampleUtils.class.getClassLoader().getResource(fileName);
            if (resourceUrl == null) {
                throw new RuntimeException("Sample resource file not found: " + fileName);
            }
            return Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for sample resource: " + fileName, e);
        }
    }
}
