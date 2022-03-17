package com.azure.sdk.build.tool;

import com.azure.sdk.build.tool.models.BuildReport;
import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import com.azure.sdk.build.tool.util.AnnotatedMethodCallerResult;
import com.azure.sdk.build.tool.util.MavenUtils;
import com.azure.sdk.build.tool.util.logging.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    public ReportGenerator(BuildReport report) {
        this.report = report;
    }

    public void generateReport() {
        if (!report.getWarningMessages().isEmpty() && LOGGER.isWarnEnabled()) {
            report.getWarningMessages().forEach(LOGGER::warn);
        }
        if (!report.getErrorMessages().isEmpty() && LOGGER.isErrorEnabled()) {
            report.getErrorMessages().forEach(LOGGER::error);
        }
        report.setBomVersion(computeBomVersion());
        report.setAzureDependencies(computeAzureDependencies());

        createJsonReport();
        // we throw a single runtime exception encapsulating all failure messages into one
        if (!report.getFailureMessages().isEmpty()) {
            StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
            report.getFailureMessages().forEach(s -> sb.append(" - " + s + "\n"));
            throw new RuntimeException(sb.toString());
        }
    }

    private String computeBomVersion() {
        DependencyManagement depMgmt = AzureSdkMojo.MOJO.getProject().getDependencyManagement();
        Optional<Dependency> bomDependency = Optional.empty();
        if (depMgmt != null) {
            bomDependency = depMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        if (bomDependency.isPresent()) {
            return bomDependency.get().getVersion();
        }
        return null;
    }

    private void createJsonReport() {

        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(writer).useDefaultPrettyPrinter();

            generator.writeStartObject();
            generator.writeStringField("group", AzureSdkMojo.MOJO.getProject().getGroupId());
            generator.writeStringField("artifact", AzureSdkMojo.MOJO.getProject().getArtifactId());
            generator.writeStringField("version", AzureSdkMojo.MOJO.getProject().getVersion());
            generator.writeStringField("name", AzureSdkMojo.MOJO.getProject().getName());
            if (report.getBomVersion() != null && !report.getBomVersion().isEmpty()) {
                generator.writeStringField("bomVersion", report.getBomVersion());
            }
            if (report.getAzureDependencies() != null && !report.getAzureDependencies().isEmpty()) {
                writeArray("azureDependencies", report.getAzureDependencies(), generator);
            }

            if (report.getServiceMethodCalls() != null && !report.getServiceMethodCalls().isEmpty()) {
                writeArray("serviceMethodCalls", report.getServiceMethodCalls()
                        .stream()
                        .map(AnnotatedMethodCallerResult::toString)
                        .collect(Collectors.toList()), generator);
            }

            if (report.getBetaMethodCalls() != null && !report.getBetaMethodCalls().isEmpty()) {
                writeArray("betaMethodCalls", report.getBetaMethodCalls()
                        .stream()
                        .map(AnnotatedMethodCallerResult::toString)
                        .collect(Collectors.toList()), generator);
            }


            if (!report.getErrorMessages().isEmpty()) {
                writeArray("errorMessages", report.getErrorMessages(), generator);
            }

            if (!report.getWarningMessages().isEmpty()) {
                writeArray("warningMessages", report.getWarningMessages(), generator);
            }

            if (!report.getFailureMessages().isEmpty()) {
                writeArray("failureMessages", report.getFailureMessages(), generator);
            }

            generator.writeEndObject();
            generator.close();
            writer.close();

            report.setJsonReport(writer.toString());
            final String reportFileString = AzureSdkMojo.MOJO.getReportFile();
            if (reportFileString != null && !reportFileString.isEmpty()) {
                final File reportFile = new File(reportFileString);
                try (FileWriter fileWriter = new FileWriter(reportFile)) {
                    fileWriter.write(report.getJsonReport());
                }
            }
        } catch (IOException exception) {

        }
    }

    private void writeArray(String fieldName, Collection<String> values, JsonGenerator generator) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartArray();
        for (String value : values) {
            generator.writeString(value);
        }
        generator.writeEndArray();
    }

    private List<String> computeAzureDependencies() {
        return getAllDependencies().stream()
                // this includes Track 2 mgmt libraries, spring libraries and data plane libraries
                .filter(artifact -> artifact.getGroupId().startsWith(AZURE_DEPENDENCY_GROUP))
                .map(MavenUtils::toGAV)
                .collect(Collectors.toList());
    }
}

