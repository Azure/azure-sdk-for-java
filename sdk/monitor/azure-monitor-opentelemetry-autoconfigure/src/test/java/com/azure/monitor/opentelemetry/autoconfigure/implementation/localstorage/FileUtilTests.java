// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils.createSymbolicLinkOrSkip;
import static org.assertj.core.api.Assertions.assertThat;

class FileUtilTests {

    @TempDir
    Path tempDir;

    @Test
    void shouldListOnlyRegularTransmissionFiles() throws IOException {
        Path regularFile = Files.createFile(tempDir.resolve("regular.trn"));
        Files.createFile(tempDir.resolve("pending.tmp"));
        Files.createDirectory(tempDir.resolve("directory.trn"));

        assertThat(FileUtil.listTrnFiles(tempDir.toFile())).containsExactly(regularFile.toFile());
    }

    @Test
    void shouldIgnoreSymbolicLinkTransmissionFiles() throws IOException {
        Path regularFile = Files.createFile(tempDir.resolve("regular.trn"));
        Files.createDirectory(tempDir.resolve("directory.trn"));
        createSymbolicLinkOrSkip(tempDir.resolve("link.trn"), regularFile);
        createSymbolicLinkOrSkip(tempDir.resolve("dangling.trn"), tempDir.resolve("missing"));

        assertThat(FileUtil.listTrnFiles(tempDir.toFile())).containsExactly(regularFile.toFile());
    }
}
