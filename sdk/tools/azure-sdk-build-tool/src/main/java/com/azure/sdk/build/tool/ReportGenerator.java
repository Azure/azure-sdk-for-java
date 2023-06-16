package com.azure.sdk.build.tool;

import com.azure.sdk.build.tool.models.BuildError;
import com.azure.sdk.build.tool.models.BuildErrorLevel;
import com.azure.sdk.build.tool.models.BuildReport;
import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import com.azure.sdk.build.tool.util.AnnotatedMethodCallerResult;
import com.azure.sdk.build.tool.util.MavenUtils;
import com.azure.sdk.build.tool.util.logging.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
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

    public BuildReport getReport() {
        return this.report;
    }

    public void generateReport() {
        report.setBomVersion(computeBomVersion());
        report.setAzureDependencies(computeAzureDependencies());
        createJsonReport();
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
            generator.writeStringField("group", getMd5(AzureSdkMojo.MOJO.getProject().getGroupId()));
            generator.writeStringField("artifact", getMd5(AzureSdkMojo.MOJO.getProject().getArtifactId()));
            generator.writeStringField("version", getMd5(AzureSdkMojo.MOJO.getProject().getVersion()));
            if (report.getBomVersion() != null && !report.getBomVersion().isEmpty()) {
                generator.writeStringField("bomVersion", report.getBomVersion());
            }
            if (report.getAzureDependencies() != null && !report.getAzureDependencies().isEmpty()) {
                writeArray("azureDependencies", report.getAzureDependencies(), generator);
            }

            if (report.getServiceMethodCalls() != null && !report.getServiceMethodCalls().isEmpty()) {
                writeArray(generator, "serviceMethodCalls", report.getServiceMethodCalls());
            }

            if (report.getBetaMethodCalls() != null && !report.getBetaMethodCalls().isEmpty()) {
                writeArray(generator, "betaMethodCalls", report.getBetaMethodCalls());
            }


            if(!report.getErrors().isEmpty()) {
                writeErrors(generator, "errors", report.getErrors());
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

    private void writeErrors(JsonGenerator generator, String key, List<BuildError> errors)  throws IOException {
        generator.writeFieldName(key);
        generator.writeStartArray();

        errors.forEach(error -> {
            try {
                generator.writeStartObject();
                generator.writeStringField("code", error.getCode().toString());
                generator.writeStringField("level", error.getLevel().toString());
                if (error.getAdditionalDetails() != null && !error.getAdditionalDetails().isEmpty()) {
                    writeArray("additionalDetails", error.getAdditionalDetails(), generator);
                }
                generator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        generator.writeEndArray();
    }

    private void writeArray(JsonGenerator generator, String serviceMethodCalls, Set<AnnotatedMethodCallerResult> report) throws IOException {
        generator.writeFieldName(serviceMethodCalls);
        generator.writeStartArray();

        Map<String, Integer> methodCallFrequency = report
            .stream()
            .map(AnnotatedMethodCallerResult::getAnnotatedMethod)
            .map(Method::toGenericString)
            .sorted()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        methodCallFrequency.forEach((key, value) -> {
            try {
                generator.writeStartObject();
                generator.writeStringField("methodName", key);
                generator.writeNumberField("frequency", value);
                generator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        generator.writeEndArray();
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

