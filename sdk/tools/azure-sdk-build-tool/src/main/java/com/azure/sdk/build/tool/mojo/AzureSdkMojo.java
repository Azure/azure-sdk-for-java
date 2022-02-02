package com.azure.sdk.build.tool.mojo;

import com.azure.sdk.build.tool.Tools;
import com.azure.sdk.build.tool.models.BuildReport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Azure SDK build tools Maven plugin Mojo for analyzing Maven configuration of an application to provide Azure
 * SDK-specific recommendations.
 */
@Mojo(name = "run",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyCollection = ResolutionScope.RUNTIME,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class AzureSdkMojo extends AbstractMojo {
    public static AzureSdkMojo MOJO;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "validateAzureSdkBomUsed", defaultValue = "true")
    private boolean validateAzureSdkBomUsed;

    @Parameter(property = "validateNoDeprecatedMicrosoftLibraryUsed", defaultValue = "true")
    private boolean validateNoDeprecatedMicrosoftLibraryUsed;

    @Parameter(property = "validateBomVersionsAreUsed", defaultValue = "true")
    private boolean validateBomVersionsAreUsed;

    @Parameter(property = "validateNoBetaLibraryUsed", defaultValue = "true")
    private boolean validateNoBetaLibraryUsed;

    @Parameter(property = "validateNoBetaAPIUsed", defaultValue = "true")
    private boolean validateNoBetaApiUsed;

    @Parameter(property = "reportFile", defaultValue = "")
    private String reportFile;

    private final BuildReport buildReport;

    /**
     * Creates an instance of Azure SDK build tool Mojo.
     */
    public AzureSdkMojo() {
        MOJO = this;
        this.buildReport = new BuildReport();
    }

    /**
     * Returns the build report.
     * @return The build report.
     */
    public BuildReport getReport() {
        return buildReport;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("========================================================================");
        getLog().info("= Running the Azure SDK Maven Build Tool                               =");
        getLog().info("========================================================================");

        // Run all of the tools. They will collect their results in the report.
        Tools.getTools().forEach(Runnable::run);

        buildReport.conclude();
    }

    /**
     * Returns the Maven project.
     * @return The Maven project.
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * If this validation is enabled, build is configured to fail if Azure SDK BOM is not used. By default, this is
     * set to {@code true}.
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateAzureSdkBomUsed() {
        return validateAzureSdkBomUsed;
    }

    /**
     * If this validation is enabled, build will fail if the application uses deprecated Microsoft libraries. By
     * default, this is set to {@code true}.
     * @return {@code true} if validation is enabled.
     */
    public boolean isValidateNoDeprecatedMicrosoftLibraryUsed() {
        return validateNoDeprecatedMicrosoftLibraryUsed;
    }

    /**
     * If this validation is enabled, build will fail if the any dependency overrides the version used in Azure SDK
     * BOM. By default, this is set to {@code true}.
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateBomVersionsAreUsed() {
        return validateBomVersionsAreUsed;
    }

    /**
     * If this validation is enabled, build will fail if a beta (preview) version of Azure library is used. By
     * default, this is set to {@code true}.
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaLibraryUsed() {
        return validateNoBetaLibraryUsed;
    }

    /**
     * If this validation is enabled, build will fail if any method annotated with @Beta is called. By
     * default, this is set to {@code true}.
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaApiUsed() {
        return validateNoBetaApiUsed;
    }

    /**
     * The report file to which the build report is written to.
     * @return The report file.
     */
    public String getReportFile() {
        return reportFile;
    }
}
