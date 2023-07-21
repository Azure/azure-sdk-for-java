// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.mojo;

import com.azure.core.util.BinaryData;
import com.azure.sdk.build.tool.ReportGenerator;
import com.azure.sdk.build.tool.Tools;
import com.azure.sdk.build.tool.implementation.ApplicationInsightsClient;
import com.azure.sdk.build.tool.implementation.ApplicationInsightsClientBuilder;
import com.azure.sdk.build.tool.implementation.models.MonitorBase;
import com.azure.sdk.build.tool.implementation.models.TelemetryEventData;
import com.azure.sdk.build.tool.implementation.models.TelemetryItem;
import com.azure.sdk.build.tool.models.BuildError;
import com.azure.sdk.build.tool.models.BuildErrorLevel;
import com.azure.sdk.build.tool.models.BuildReport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Azure SDK build tools Maven plugin Mojo for analyzing Maven configuration of an application to provide Azure
 * SDK-specific recommendations.
 */
@Mojo(name = "run",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    requiresDependencyCollection = ResolutionScope.RUNTIME,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class AzureSdkMojo extends AbstractMojo {

    public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private static AzureSdkMojo mojo;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String APP_INSIGHTS_INSTRUMENTATION_KEY = "1d377c0e-44f8-4d56-bee7-7f13a3fef594";
    private static final String APP_INSIGHTS_ENDPOINT = "https://centralus-2.in.applicationinsights.azure.com/";
    private static final String AZURE_SDK_BUILD_TOOL = "azure-sdk-build-tool";
    private final ApplicationInsightsClient applicationInsightsClient;

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

    @Parameter(property = "validateNoBetaApiUsed", defaultValue = "true")
    private boolean validateNoBetaApiUsed;

    @Parameter(property = "validateLatestBomVersionUsed", defaultValue = "true")
    private boolean validateLatestBomVersionUsed;

    @Parameter(property = "reportFile", defaultValue = "")
    private String reportFile;

    @Parameter(property = "sendToMicrosoft", defaultValue = "true")
    private boolean sendToMicrosoft;

    private final BuildReport buildReport;

    /**
     * Creates an instance of Azure SDK build tool Mojo.
     */
    public AzureSdkMojo() {
        mojo = this;
        this.buildReport = new BuildReport();
        applicationInsightsClient = new ApplicationInsightsClientBuilder()
            .host(APP_INSIGHTS_ENDPOINT)
            .buildClient();
    }

    /**
     * The {@link AzureSdkMojo} instance.
     * @return The {@link AzureSdkMojo} instance.
     */
    public static AzureSdkMojo getMojo() {
        return mojo;
    }

    /**
     * Returns the build report.
     *
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

        Tools.getTools().forEach(Runnable::run);
        ReportGenerator reportGenerator = new ReportGenerator(buildReport);
        reportGenerator.generateReport();
        BuildReport report = reportGenerator.getReport();
        if (sendToMicrosoft) {
            sendReportToAppInsights(report);
        }

        StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
        boolean hasErrors = false;
        for (BuildError error : report.getErrors()) {
            if (BuildErrorLevel.WARNING.equals(error.getLevel())) {
                getLog().warn(error.getMessage());
            } else if (BuildErrorLevel.ERROR.equals(error.getLevel())) {
                hasErrors = true;
                sb.append(" - " + error.getMessage() + "\n");
            }
        }
        // we throw a single runtime exception encapsulating all failure messages into one
        if (hasErrors) {
            throw new RuntimeException(sb.toString());
        }
    }

    private void sendReportToAppInsights(BuildReport report) {
        try {
            TelemetryItem telemetryItem = new TelemetryItem();
            telemetryItem.setTime(OffsetDateTime.now());
            telemetryItem.setName(AZURE_SDK_BUILD_TOOL);
            telemetryItem.setInstrumentationKey(APP_INSIGHTS_INSTRUMENTATION_KEY);
            TelemetryEventData data = new TelemetryEventData();
            Map<String, String> customEventProperties = getCustomEventProperties(report);
            data.setProperties(customEventProperties);
            MonitorBase monitorBase = new MonitorBase();
            monitorBase.setBaseData(data).setBaseType("EventData");
            data.setName("azure-sdk-java-build-telemetry");
            telemetryItem.setData(monitorBase);
            List<TelemetryItem> telemetryItems = new ArrayList<>();
            telemetryItems.add(telemetryItem);
            applicationInsightsClient.trackAsync(telemetryItems).block();
        } catch (Exception ex) {
            getLog().warn("Unable to send report to Application Insights. " + ex.getMessage());
        }
    }

    private Map<String, String> getCustomEventProperties(BuildReport report) {
        Map<String, Object> properties = OBJECT_MAPPER.convertValue(report, MAP_TYPE_REFERENCE);
        Map<String, String> customEventProperties = new HashMap<>(properties.size());
        // AppInsights customEvents table does not support nested JSON objects in "properties" field
        // So, we have to convert the nested objects to strings
        properties.forEach((key, value) -> {
            if (value instanceof String) {
                customEventProperties.put(key, (String) value);
            } else {
                customEventProperties.put(key, BinaryData.fromObject(value).toString());
            }
        });
        return customEventProperties;
    }

    /**
     * Returns the Maven project.
     *
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
     *
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
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaLibraryUsed() {
        return validateNoBetaLibraryUsed;
    }

    /**
     * If this validation is enabled, build will fail if any method annotated with @Beta is called. By
     * default, this is set to {@code true}.
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaApiUsed() {
        return validateNoBetaApiUsed;
    }

    /**
     * If this validation is enabled, build will fail if the latest version of Azure SDK BOM is not used. By default,
     * this is set to {@code true}.
     *
     * @return {@code true} if the latest version of Azure SDK BOM is used.
     */
    public boolean isValidateLatestBomVersionUsed() {
        return validateLatestBomVersionUsed;
    }

    /**
     * The report file to which the build report is written to.
     *
     * @return The report file.
     */
    public String getReportFile() {
        return reportFile;
    }
}
