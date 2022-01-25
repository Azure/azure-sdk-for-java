// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

final class TestUtils {
    /**
     * Creates a temporary file that can be passed into the Checkstyle test verification.
     *
     * @param testName Name of the test, used as the base file name.
     * @param lines Contents of the file.
     * @return A file that can be passed into Checkstyle test verification.
     * @throws IOException If an error happens creating or writing the file.
     */
    static File createCheckFile(String testName, List<String> lines) throws IOException {
        Path file = Files.createTempFile(testName + UUID.randomUUID(), ".java");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }

        File realFile = file.toFile();
        realFile.deleteOnExit();

        return realFile;
    }

    private TestUtils() {
    }
}
