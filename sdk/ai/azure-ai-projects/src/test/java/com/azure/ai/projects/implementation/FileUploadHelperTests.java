// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUploadHelperTests {

    @Test
    void getFilesRejectsFilesystemRoot() {
        Path root = Paths.get("").toAbsolutePath().getRoot();

        assertThrows(IllegalArgumentException.class, () -> FileUploadHelper.getFiles(root, null));
    }
}