// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class TestsAssetsHelpers {
    public static String readResourceFile(String filePathFromResources) throws IOException {
        String pathToResources = System.getProperty("user.dir") + "/src/test/resources/" + filePathFromResources;

        return new String(Files.readAllBytes(Paths.get(pathToResources)));
    }
}
