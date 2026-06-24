// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.implementation.utils.FileUtils;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class FileUtilsTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void writeToFileAsyncCreatesNewFile() throws IOException {
        Path destinationFile = temporaryDirectory.resolve("new-file.txt");

        StepVerifier.create(FileUtils.writeToFileAsync(BinaryData.fromString("new content"), destinationFile))
            .verifyComplete();

        Assertions.assertEquals("new content", new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }

    @Test
    public void writeToFileAsyncOverwritesExistingFile() throws IOException {
        Path destinationFile = Files.createFile(temporaryDirectory.resolve("existing-file.txt"));

        StepVerifier.create(FileUtils.writeToFileAsync(BinaryData.fromString("updated content"), destinationFile))
            .verifyComplete();

        Assertions.assertEquals("updated content",
            new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }
}
