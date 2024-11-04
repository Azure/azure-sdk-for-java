// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TempDirs {

    private static final List<String> CANDIDATE_USERNAME_ENVIRONMENT_VARIABLES
        = Collections.unmodifiableList(Arrays.asList("USER", "LOGNAME", "USERNAME"));

    @Nullable
    public static File getApplicationInsightsTempDir(ClientLogger logger, String message) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        tempDir = maybeAddUserSubDir(tempDir);
        tempDir = new File(tempDir, "applicationinsights");

        if (!tempDir.exists() && !tempDir.mkdirs()) {
            logger.info(
                "Unable to create directory: {}. {}. If this is unexpected, please check"
                    + " that the process has the necessary permissions to create the directory.",
                tempDir.getAbsolutePath(), message);
            return null;
        }
        if (!tempDir.canRead()) {
            logger.info(
                "Missing read permissions on directory: {}. {}. If this is unexpected, please check"
                    + " that the process has the necessary permissions to read from the directory.",
                tempDir.getAbsolutePath(), message);
            return null;
        }
        if (!tempDir.canWrite()) {
            logger.info(
                "Missing write permissions on directory: {}. {}. If this is unexpected, please check"
                    + " that the process has the necessary permissions to write to the directory.",
                tempDir.getAbsolutePath(), message);
            return null;
        }
        return tempDir;
    }

    public static File getSubDir(File parent, String name) {
        File dir = new File(parent, name);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalArgumentException("Unable to create directory: " + dir);
        }
        if (!dir.canRead()) {
            throw new IllegalArgumentException("Missing read permission to subdirectory: " + dir);
        }
        if (!dir.canWrite()) {
            throw new IllegalArgumentException("Missing write permission to subdirectory: " + dir);
        }
        return dir;
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
