// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TempDirsTests {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateDirectoryWithOwnerOnlyPermissions() throws IOException {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path directory = tempDir.resolve("applicationinsights");

        TempDirs.createSecureDirectory(directory);

        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(directory))).isEqualTo("rwx------");
    }

    @Test
    void shouldRejectSymbolicLink() throws IOException {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path target = Files.createDirectory(tempDir.resolve("target"));
        Path link = tempDir.resolve("applicationinsights");
        Files.createSymbolicLink(link, target);

        assertThatThrownBy(() -> TempDirs.createSecureDirectory(link)).isInstanceOf(IOException.class)
            .hasMessageContaining("symbolic link");
    }

    @Test
    void shouldRejectSymbolicLinkInIntermediateDirectory() throws IOException {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path target = Files.createDirectory(tempDir.resolve("target"));
        Path link = tempDir.resolve("user");
        Files.createSymbolicLink(link, target);

        assertThatThrownBy(() -> TempDirs.createSecureDirectories(tempDir, link.resolve("applicationinsights")))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("symbolic link");
    }
}
