// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BomDependency;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.azure.tools.bomgenerator.Utils.ANALYZE_MODE;
import static com.azure.tools.bomgenerator.Utils.BASE_AZURE_GROUPID;
import static com.azure.tools.bomgenerator.Utils.AZURE_PERF_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.AZURE_TEST_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.EXCLUSION_LIST;
import static com.azure.tools.bomgenerator.Utils.GENERATE_MODE;
import static com.azure.tools.bomgenerator.Utils.INPUT_DEPENDENCY_PATTERN;
import static com.azure.tools.bomgenerator.Utils.POM_TYPE;
import static com.azure.tools.bomgenerator.Utils.SDK_DEPENDENCY_PATTERN;
import static com.azure.tools.bomgenerator.Utils.STRING_SPLIT_BY_COLON;
import static com.azure.tools.bomgenerator.Utils.isPublishedArtifact;
import static com.azure.tools.bomgenerator.Utils.parsePomFileContent;
import static com.azure.tools.bomgenerator.Utils.toBomDependencyNoVersion;
import static com.azure.tools.bomgenerator.Utils.validateNotNullOrEmpty;

public class BomGenerator {
    private String outputFileName;
    private String inputFileName;
    private String pomFileName;
    private String overriddenInputDependenciesFileName;
    private String reportFileName;
    private String mode;

    private static Logger logger = LoggerFactory.getLogger(BomGenerator.class);

    BomGenerator() {
        this.mode = GENERATE_MODE;
    }

    public String getInputFileName() {
        return this.inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getOverriddenInputDependenciesFileName() {
        return this.overriddenInputDependenciesFileName;
    }

    public void setOverriddenInputDependenciesFileName(String overriddenInputDependenciesFileName) {
        this.overriddenInputDependenciesFileName = overriddenInputDependenciesFileName;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getPomFileName() {
        return this.pomFileName;
    }

    public void setPomFileName(String pomFileName) {
        this.pomFileName = pomFileName;
    }

    public String getReportFileName() {
        return this.reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void run() {
        switch (mode) {
            case ANALYZE_MODE:
                validate();
                break;

            case GENERATE_MODE:
                generate();
                break;

            default:
                logger.error("Unknown value for mode: {}", mode);
                break;
        }
    }

    private void validate() {
        var inputDependencies = parsePomFileContent(this.pomFileName);
        DependencyAnalyzer analyzer = new DependencyAnalyzer(inputDependencies, null, this.reportFileName);
        analyzer.validate();
    }

    private void generate() {
        List<BomDependency> inputDependencies = scan();
        List<BomDependency> externalDependencies = resolveExternalDependencies();

        // 1. Create the initial tree and reduce conflicts.
        // 2. And pick only those dependencies. that were in the input set, since they become the new roots of n-ary tree.
        DependencyAnalyzer analyzer = new DependencyAnalyzer(inputDependencies, externalDependencies, this.reportFileName);
        analyzer.reduce();
        Collection<BomDependency> outputDependencies = analyzer.getBomEligibleDependencies();

        // 2. Create the new tree for the BOM.
        analyzer = new DependencyAnalyzer(outputDependencies, externalDependencies, this.reportFileName);
        boolean validationFailed = analyzer.validate();
        outputDependencies = analyzer.getBomEligibleDependencies();

        // 4. Create the new BOM file.
        if(!validationFailed) {
            // Rewrite the existing BOM to have the dependencies in the order in which we insert them, making the diff PR easier to review.
            rewriteExistingBomFile();
            writeBom(outputDependencies);
        }
        else {
            logger.trace("Validation for the BOM failed. Exiting...");
        }
    }

    private List<BomDependency> scanVersioningClientFileDependencies() {
        List<BomDependency> inputDependencies = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(Paths.get(inputFileName))) {
                BomDependency dependency = scanDependency(line);

                if(dependency != null) {
                    inputDependencies.add(dependency);
                }
            }
        } catch (IOException exception) {
            logger.error("Input file parsing failed. Exception{}", exception.toString());
        }

        return inputDependencies;
    }

    private List<BomDependency> scan() {
       var versioningClientDependency = scanVersioningClientFileDependencies();

       if (this.overriddenInputDependenciesFileName == null) {
           return versioningClientDependency;
       }

       var overriddenInputDependencies = scanOverriddenDependencies();
       var overriddenInputDependenciesNoVersion = overriddenInputDependencies.stream().map(Utils::toBomDependencyNoVersion).collect(Collectors.toUnmodifiableSet());

       var filteredInputDependencies = versioningClientDependency.stream().filter(dependency -> !overriddenInputDependenciesNoVersion.contains(toBomDependencyNoVersion(dependency))).collect(Collectors.toList());
       filteredInputDependencies.addAll(overriddenInputDependencies);

       return filteredInputDependencies;
    }

    private Map<BomDependency, String> parseRawFile() {
        Map<BomDependency, String> inputDependencies = new HashMap();

        try {
            for (String line : Files.readAllLines(Paths.get(overriddenInputDependenciesFileName))) {
                var matcher = INPUT_DEPENDENCY_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }

                var dependencyPattern = matcher.group(1);
                var pomFilePath = matcher.groupCount() == 3 ? matcher.group(2) : null;

                var dependency = STRING_SPLIT_BY_COLON.split(dependencyPattern); {
                    if(dependency.length != 3) {
                        continue;
                    }
                    validateNotNullOrEmpty(dependency, "inputDependency");
                    inputDependencies.put(new BomDependency(dependency[0], dependency[1], dependency[2]), pomFilePath);
                }
            }
        } catch (IOException exception) {
            logger.error("Input file parsing failed. Exception{}", exception.toString());
        }

        return inputDependencies;
    }

    private List<BomDependency> scanOverriddenDependencies() {
        List<BomDependency> allInputDependencies = new ArrayList<>();

        var overriddenInputDependencies = parseRawFile();
        // Some of these libraries may not have been published yet.
        for(BomDependency dependency: overriddenInputDependencies.keySet()) {
            if(isPublishedArtifact(dependency)) {
                allInputDependencies.add(dependency);
                continue;
            }

            // Since the artifact is not published. We need to read the dependencies from the POM file directly
            // and add them as input dependencies.
            allInputDependencies.addAll(parsePomFileContent(overriddenInputDependencies.get(dependency)));
        }

        return allInputDependencies;
    }

    private BomDependency scanDependency(String line) {
        Matcher matcher = SDK_DEPENDENCY_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        if (matcher.groupCount() != 3) {
            return null;
        }

        String artifactId = matcher.group(1);
        String version = matcher.group(2);

        if(version.contains("-")) {
            // This is a non-GA library
            return null;
        }

        if (EXCLUSION_LIST.contains(artifactId)
            || artifactId.contains(AZURE_PERF_LIBRARY_IDENTIFIER)
            || (artifactId.contains(AZURE_TEST_LIBRARY_IDENTIFIER))) {
            logger.trace("Skipping dependency {}:{}", BASE_AZURE_GROUPID, artifactId);
            return null;
        }

        return new BomDependency(BASE_AZURE_GROUPID, artifactId, version);
    }

    private Model readModel() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(this.pomFileName));
            return model;
        } catch (XmlPullParserException | IOException e) {
            logger.error("BOM reading failed with: {}", e.toString());
        }

        return null;
    }

	private void writeModel(Model model) {
        String pomFileName = this.pomFileName;
        writeModel(pomFileName, model);
    }

    private void writeModel(String fileName, Model model) {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try {
            writer.write(new FileWriter(fileName), model);
        } catch (IOException exception) {
            logger.error("BOM writing failed with: {}", exception.toString());
        }
    }

    private List<BomDependency> resolveExternalDependencies() {
        List<BomDependency> externalDependencies = new ArrayList<>();
        List<Dependency> externalBomDependencies = getExternalDependencies();
        externalDependencies.addAll(Utils.getExternalDependenciesContent(externalBomDependencies));
        return externalDependencies;
    }

    private List<Dependency> getExternalDependencies() {
        Model model = readModel();
        DependencyManagement management = model.getDependencyManagement();
        return management.getDependencies().stream().filter(dependency -> dependency.getType().equals(POM_TYPE)).collect(Collectors.toList());
    }

    private void rewriteExistingBomFile() {
        Model model = readModel();
        DependencyManagement management = model.getDependencyManagement();
        List<Dependency> dependencies = management.getDependencies();
        dependencies.sort(new DependencyComparator());
        management.setDependencies(dependencies);
        writeModel(model);
    }

    private void writeBom(Collection<BomDependency> bomDependencies) {
        Model model = readModel();
        DependencyManagement management = model.getDependencyManagement();
        List<Dependency> externalBomDependencies = management.getDependencies().stream().filter(dependency -> dependency.getType().equals(POM_TYPE)).collect(Collectors.toList());
        List<Dependency> dependencies = bomDependencies.stream().map(bomDependency -> {
            Dependency dependency = new Dependency();
            dependency.setGroupId(bomDependency.getGroupId());
            dependency.setArtifactId(bomDependency.getArtifactId());
            dependency.setVersion(bomDependency.getVersion());
            return dependency;
        }).collect(Collectors.toList());
        dependencies.addAll(externalBomDependencies);
        dependencies.sort(new DependencyComparator());
        management.setDependencies(dependencies);
        writeModel(this.outputFileName, model);
    }
}
