// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class SampleUtils {

    public static Path getPath(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = SampleUtils.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }

        File file = new File(resource.toURI());
        return file.toPath();
    }
}
