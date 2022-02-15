package com.azure.sdk.build.tool.util;

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

    private static final ResourceBundle strings = ResourceBundle.getBundle("strings");

    private MojoUtils() {
        // no-op
    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getDirectDependencies() {
        return AzureSdkMojo.MOJO.getProject().getDependencyArtifacts();
    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getAllDependencies() {
        return AzureSdkMojo.MOJO.getProject().getArtifacts();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCompileSourceRoots() {
        return ((List<String>)AzureSdkMojo.MOJO.getProject()
                           .getCompileSourceRoots());
    }

    public static String getString(String key) {
        return strings.getString(key);
    }

    public static String getString(String key, String... parameters) {
        return MessageFormat.format(getString(key), parameters);
    }

    public static void failOrWarn(Supplier<Boolean> condition, String message) {
        if (condition.get()) {
            AzureSdkMojo.MOJO.getReport().addFailureMessage(message);
        } else {
            AzureSdkMojo.MOJO.getReport().addWarningMessage(message);
        }
    }
}
