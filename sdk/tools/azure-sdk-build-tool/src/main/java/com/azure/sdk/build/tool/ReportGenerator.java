// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool;

import com.azure.core.util.BinaryData;
import com.azure.sdk.build.tool.models.BuildReport;
import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import com.azure.sdk.build.tool.util.MavenUtils;
import com.azure.sdk.build.tool.util.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.sdk.build.tool.util.MojoUtils.getAllDependencies;

/**
 * The tool to generate the final report of the build.
 */
public class ReportGenerator {
    private static final Logger LOGGER = Logger.getInstance();
    private static final String AZURE_DEPENDENCY_GROUP = "com.azure";
    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private final BuildReport report;

    /**
     * Creates an instance of {@link ReportGenerator} to generate the final report of the build.
     * @param report the report to generate.
     */
    public ReportGenerator(BuildReport report) {
        this.report = report;
    }

    /**
     * Gets the generated report.
     * @return the generated report.
     */
    public BuildReport getReport() {
        return this.report;
    }

    /**
     * Generates the final report of the build.
     */
    public void generateReport() {
        report.setAzureDependencies(computeAzureDependencies());
        report.setGroupId(getMd5(AzureSdkMojo.getMojo().getProject().getGroupId()));
        report.setArtifactId(getMd5(AzureSdkMojo.getMojo().getProject().getArtifactId()));
        report.setVersion(getMd5(AzureSdkMojo.getMojo().getProject().getVersion()));
        writeReportToFile();
    }

    private void writeReportToFile() {
        final String reportFileString = AzureSdkMojo.getMojo().getReportFile();
        if (reportFileString != null && !reportFileString.isEmpty()) {
            final File reportFile = new File(reportFileString);
            try (FileWriter fileWriter = new FileWriter(reportFile)) {
                fileWriter.write(BinaryData.fromObject(report).toString());
            } catch (IOException exception) {
                AzureSdkMojo.getMojo().getLog().warn("Unable to write report to " + reportFileString, exception);
            }
        }
    }

    private List<String> computeAzureDependencies() {
        return getAllDependencies().stream()
            // this includes Track 2 mgmt libraries, spring libraries and data plane libraries
            .filter(artifact -> artifact.getGroupId().startsWith(AZURE_DEPENDENCY_GROUP))
            .map(MavenUtils::toGAV)
            .collect(Collectors.toList());
    }

    private String getMd5(String inputText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(inputText.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException exception) {
            return "Unknown";
        }

    }

}

