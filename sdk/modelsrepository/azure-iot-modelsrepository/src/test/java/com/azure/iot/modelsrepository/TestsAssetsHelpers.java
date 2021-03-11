// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestsAssetsHelpers {
    public static String readResourceFile(String filePathFromResources) throws IOException {
        String pathToResources = System.getProperty("user.dir") + "/src/test/resources/" + filePathFromResources;
        String content = Files.readString(Path.of(pathToResources));

        return content;
    }
}
