// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util;

import com.azure.sdk.build.tool.models.BuildError;
import com.azure.sdk.build.tool.models.BuildErrorCode;
import com.azure.sdk.build.tool.models.BuildErrorLevel;
import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import org.apache.maven.artifact.Artifact;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Utility class to perform an miscellaneous Mojo-related operations.
 */
public final class MojoUtils {

    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("strings");

    private MojoUtils() {
        // no-op
    }

    /**
     * Get the set of direct dependencies for the project.
     * @return the set of direct dependencies for the project.
     */
    @SuppressWarnings("unchecked")
    public static Set<Artifact> getDirectDependencies() {
        return AzureSdkMojo.getMojo().getProject().getDependencyArtifacts();
    }

    /**
     * Get the list of dependencies for the project.
     * @return the list of dependencies for the project.
     */
    @SuppressWarnings("unchecked")
    public static Set<Artifact> getAllDependencies() {
        return AzureSdkMojo.getMojo().getProject().getArtifacts();
    }

    /**
     * Get the list of source roots for the project.
     *
     * @return the list of source roots for the project.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCompileSourceRoots() {
        return ((List<String>) AzureSdkMojo.getMojo().getProject()
                           .getCompileSourceRoots());
    }

    /**
     * Get a string from the resource bundle.
     *
     * @param key the key to the string in the resource bundle.
     * @return the string from the resource bundle.
     */
    public static String getString(String key) {
        return STRINGS.getString(key);
    }

    /**
     * Get a formatted string from the resource bundle.
     *
     * @param key the key to the string in the resource bundle.
     * @param parameters the parameters to be passed to the string.
     * @return the formatted string.
     */
    public static String getString(String key, String... parameters) {
        return MessageFormat.format(getString(key), parameters);
    }

    /**
     * Fail or warn the build based on the condition.
     *
     * @param condition if true, fail the build, otherwise warn
     * @param errorCode the build error code.
     * @param message the error message.
     */
    public static void failOrWarn(Supplier<Boolean> condition, BuildErrorCode errorCode, String message) {
        failOrWarn(condition, errorCode, message, null);
    }

    /**
     * Fail or warn the build based on the condition.
     * @param condition if true, fail the build, otherwise warn
     * @param errorCode the build error code.
     * @param message the error message.
     * @param additionalDetails additional details to be added to the error.
     */
    public static void failOrWarn(Supplier<Boolean> condition, BuildErrorCode errorCode, String message, List<String> additionalDetails) {
        BuildError buildError;
        if (condition.get()) {
            buildError = new BuildError(message, errorCode, BuildErrorLevel.ERROR, additionalDetails);
        } else {
            buildError = new BuildError(message, errorCode, BuildErrorLevel.WARNING, additionalDetails);
        }
        AzureSdkMojo.getMojo().getReport().addError(buildError);
    }
}
