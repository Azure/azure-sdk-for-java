// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils.createSymbolicLinkOrSkip;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TempDirsTests {

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void shouldCreateDirectoryWithOwnerOnlyPermissions(boolean existing) throws IOException {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path directory = tempDir.resolve("applicationinsights");
        if (existing) {
            Files.createDirectory(directory);
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwxrwxrwx"));
        }

        TempDirs.createSecureDirectory(directory);

        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(directory))).isEqualTo("rwx------");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void shouldRestrictDirectoryAclAndInheritToChildren(boolean existing) throws IOException {
        assumeTrue(!Files.getFileStore(tempDir).supportsFileAttributeView("posix")
            && Files.getFileStore(tempDir).supportsFileAttributeView("acl"));
        Path directory = tempDir.resolve("applicationinsights");
        UserPrincipal owner = Files.getOwner(tempDir);
        if (existing) {
            Files.createDirectory(directory);
            UserPrincipal everyone
                = directory.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("Everyone");
            assertThat(everyone).isNotEqualTo(owner);
            AclEntry ownerEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT)
                .build();
            AclEntry everyoneEntry = AclEntry.newBuilder(ownerEntry).setPrincipal(everyone).build();
            Files.getFileAttributeView(directory, AclFileAttributeView.class)
                .setAcl(Arrays.asList(ownerEntry, everyoneEntry));
        }

        TempDirs.createSecureDirectory(directory);

        assertOwnerOnlyAcl(directory, owner, true);
        Path file = File.createTempFile("telemetry-", ".tmp", directory.toFile()).toPath();
        assertOwnerOnlyAcl(file, owner, false);
        Path child = Files.createDirectory(directory.resolve("telemetry"));
        assertOwnerOnlyAcl(child, owner, true);
        Path nestedFile = File.createTempFile("telemetry-", ".tmp", child.toFile()).toPath();
        assertOwnerOnlyAcl(nestedFile, owner, false);
    }

    @Test
    void shouldRejectSymbolicLink() throws IOException {
        Path target = Files.createDirectory(tempDir.resolve("target"));
        Path link = tempDir.resolve("applicationinsights");
        createSymbolicLinkOrSkip(link, target);

        assertThatThrownBy(() -> TempDirs.createSecureDirectory(link)).isInstanceOf(IOException.class)
            .hasMessageContaining("symbolic link");
    }

    @Test
    void shouldRejectSymbolicLinkInIntermediateDirectory() throws IOException {
        Path target = Files.createDirectory(tempDir.resolve("target"));
        Path link = tempDir.resolve("user");
        createSymbolicLinkOrSkip(link, target);

        assertThatThrownBy(() -> TempDirs.createSecureDirectories(tempDir, link.resolve("applicationinsights")))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("symbolic link");
    }

    @ParameterizedTest
    @ValueSource(strings = { "directory", "parent", "sharedTemp", "intermediate" })
    @EnabledOnOs(OS.WINDOWS)
    void shouldRejectJunctions(String location) throws Exception {
        Path target = Files.createDirectory(tempDir.resolve("target"));
        Path marker = Files.createFile(target.resolve("marker"));
        List<AclEntry> originalAcl = Files.getFileAttributeView(target, AclFileAttributeView.class).getAcl();
        Path junction = tempDir.resolve("junction");
        try {
            createJunction(junction, target);
            BasicFileAttributes attributes
                = Files.readAttributes(junction, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            assertThat(attributes.isSymbolicLink()).isFalse();
            assertThat(attributes.isDirectory()).isTrue();
            assertThat(attributes.isOther()).isTrue();

            assertThatThrownBy(() -> {
                switch (location) {
                    case "directory":
                        TempDirs.createSecureDirectory(junction);
                        break;

                    case "parent":
                        TempDirs.createSecureDirectory(junction.resolve("telemetry"));
                        break;

                    case "sharedTemp":
                        TempDirs.createSecureDirectories(junction, junction.resolve("applicationinsights"));
                        break;

                    case "intermediate":
                        TempDirs.createSecureDirectories(tempDir, junction.resolve("applicationinsights"));
                        break;

                    default:
                        throw new AssertionError("Unexpected location: " + location);
                }
            }).isInstanceOf(IOException.class).hasMessageContaining("junction");

            assertThat(Files.getFileAttributeView(target, AclFileAttributeView.class).getAcl()).isEqualTo(originalAcl);
            try (Stream<Path> contents = Files.list(target)) {
                assertThat(contents.collect(Collectors.toList())).containsExactly(marker);
            }
        } finally {
            // Remove the junction itself before JUnit cleans up the temporary directory.
            Files.deleteIfExists(junction);
        }
    }

    private static void createJunction(Path junction, Path target) throws Exception {
        Process process = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", junction.toString(), target.toString())
            .redirectErrorStream(true)
            .start();
        try {
            assertThat(process.waitFor(10, SECONDS)).as("junction creation completed").isTrue();
            try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                assertThat(process.exitValue()).as(output.lines().collect(Collectors.joining(System.lineSeparator())))
                    .isZero();
            }
        } finally {
            process.destroyForcibly();
        }
    }

    private static void assertOwnerOnlyAcl(Path path, UserPrincipal owner, boolean directory) throws IOException {
        List<AclEntry> acl
            = Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).getAcl();
        assertThat(acl).hasSize(1);
        AclEntry entry = acl.get(0);
        assertThat(entry.principal()).isEqualTo(owner);
        assertThat(entry.type()).isEqualTo(AclEntryType.ALLOW);
        assertThat(entry.permissions()).containsExactlyInAnyOrderElementsOf(EnumSet.allOf(AclEntryPermission.class));
        if (directory) {
            assertThat(entry.flags()).containsExactlyInAnyOrder(AclEntryFlag.DIRECTORY_INHERIT,
                AclEntryFlag.FILE_INHERIT);
        } else {
            assertThat(entry.flags()).isEmpty();
        }
    }
}
