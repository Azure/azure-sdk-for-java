// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TempDirs {

    private static final List<String> CANDIDATE_USERNAME_ENVIRONMENT_VARIABLES
        = Collections.unmodifiableList(Arrays.asList("USER", "LOGNAME", "USERNAME"));

    @Nullable
    public static File getApplicationInsightsTempDir(ClientLogger logger, String message) {
        File sharedTempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = maybeAddUserSubDir(sharedTempDir);
        tempDir = new File(tempDir, "applicationinsights");

        try {
            createSecureDirectories(sharedTempDir.toPath(), tempDir.toPath());
        } catch (IOException | UnsupportedOperationException e) {
            logger.info(
                "Unable to securely create directory: {}. {}. If this is unexpected, please check"
                    + " that the directory is owned by the current user and is not a symbolic link.",
                tempDir.getAbsolutePath(), message, e);
            return null;
        }
        return tempDir;
    }

    public static File getSubDir(File parent, String name) {
        File dir = new File(parent, name);
        try {
            createSecureDirectory(dir.toPath());
        } catch (IOException | UnsupportedOperationException e) {
            throw new IllegalArgumentException("Unable to securely create directory: " + dir, e);
        }
        return dir;
    }

    static void createSecureDirectories(Path sharedTempDirectory, Path directory) throws IOException {
        Path normalizedSharedTempDirectory = sharedTempDirectory.toAbsolutePath().normalize();
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        if (!normalizedDirectory.startsWith(normalizedSharedTempDirectory)
            || normalizedDirectory.equals(normalizedSharedTempDirectory)) {
            throw new IOException("Invalid temporary directory path: " + directory);
        }
        if (!Files.isDirectory(normalizedSharedTempDirectory, LinkOption.NOFOLLOW_LINKS)
            || containsSymbolicLink(normalizedSharedTempDirectory)) {
            throw new IOException(
                "Shared temporary directory is invalid or contains a symbolic link: " + normalizedSharedTempDirectory);
        }

        Path current = normalizedSharedTempDirectory;
        for (Path component : normalizedSharedTempDirectory.relativize(normalizedDirectory)) {
            current = current.resolve(component);
            createSecureDirectory(current);
        }
    }

    static void createSecureDirectory(Path directory) throws IOException {
        Path parent = directory.getParent();
        if (parent == null || !Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS) || containsSymbolicLink(parent)) {
            throw new IOException("Directory parent is missing, invalid, or a symbolic link: " + parent);
        }

        boolean posix = Files.getFileStore(parent).supportsFileAttributeView("posix");
        Path ownerProbe = posix
            ? Files.createTempFile(parent, ".applicationinsights-owner-", ".tmp",
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
            : Files.createTempFile(parent, ".applicationinsights-owner-", ".tmp");
        UserPrincipal currentOwner;
        try {
            currentOwner = Files.getOwner(ownerProbe, LinkOption.NOFOLLOW_LINKS);
        } finally {
            Files.deleteIfExists(ownerProbe);
        }

        boolean created = false;
        try {
            if (posix) {
                Files.createDirectory(directory,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } else {
                Files.createDirectory(directory);
            }
            created = true;
        } catch (FileAlreadyExistsException ignored) {
            // Validated below without following links.
        }

        if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Directory is invalid or a symbolic link: " + directory);
        }
        UserPrincipal owner = Files.getOwner(directory, LinkOption.NOFOLLOW_LINKS);
        if (!owner.equals(currentOwner)) {
            throw new IOException("Directory is not owned by the current user: " + directory);
        }

        if (posix) {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
        } else {
            restrictAclToOwner(directory, owner);
        }

        if (!created && !Files.isReadable(directory)) {
            throw new IOException("Directory is not readable: " + directory);
        }
    }

    private static void restrictAclToOwner(Path directory, UserPrincipal owner) throws IOException {
        AclFileAttributeView aclView
            = Files.getFileAttributeView(directory, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (aclView == null) {
            throw new UnsupportedOperationException("File system does not support POSIX permissions or ACLs");
        }
        Set<AclEntryPermission> permissions = EnumSet.allOf(AclEntryPermission.class);
        AclEntry ownerEntry = AclEntry.newBuilder()
            .setType(AclEntryType.ALLOW)
            .setPrincipal(owner)
            .setPermissions(permissions)
            .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT)
            .build();
        aclView.setAcl(Collections.singletonList(ownerEntry));
    }

    // Files.isSymbolicLink(path, NOFOLLOW_LINKS) only inspects path's final component; an
    // intermediate component (e.g. a multi-segment java.io.tmpdir) could itself be a symlink, so
    // every ancestor is checked individually here.
    private static boolean containsSymbolicLink(Path path) {
        Path root = path.getRoot();
        if (root == null) {
            return true;
        }
        Path current = root;
        if (Files.isSymbolicLink(current)) {
            return true;
        }
        for (Path component : path) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a suitable folder to use for temporary files, while avoiding the risk of collision when
     * multiple users are running applications that make use of Application Insights.
     *
     * <p>See the third paragraph at
     * http://www.chiark.greenend.org.uk/~peterb/uxsup/project/tmp-per-user/ for a great explanation
     * of the motivation behind this method.
     *
     * @return a {@link File} representing a folder in which temporary files will be stored for the
     * current user.
     */
    private static File maybeAddUserSubDir(File dir) {

        // does it look shared?
        // TODO: this only catches the Linux case
        //  I think a few system users on Windows might share C:\Windows\Temp
        if ("/tmp".contentEquals(dir.getAbsolutePath())) {
            String username = determineCurrentUsername();
            if (username != null) {
                return new File(dir, username);
            }
        }

        return dir;
    }

    /**
     * Attempts to find the login/sign-in name of the user.
     *
     * @return the best guess at what the current user's login name is.
     */
    @Nullable
    private static String determineCurrentUsername() {
        // Start with the value of the "user.name" property
        String username = System.getProperty("user.name");

        if (!CoreUtils.isNullOrEmpty(username)) {
            return username;
        }

        // Try some environment variables
        for (String candidate : CANDIDATE_USERNAME_ENVIRONMENT_VARIABLES) {
            username = System.getenv(candidate);
            if (!CoreUtils.isNullOrEmpty(username)) {
                return username;
            }
        }

        return null;
    }

    private TempDirs() {
    }
}
