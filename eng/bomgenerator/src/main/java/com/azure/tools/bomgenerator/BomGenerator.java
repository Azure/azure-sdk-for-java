// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BomDependency;
import com.azure.tools.bomgenerator.models.BomDependencyManagement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.azure.tools.bomgenerator.Utils.ANALYZE_MODE;
import static com.azure.tools.bomgenerator.Utils.AZURE_PERF_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.AZURE_TEST_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.BASE_AZURE_GROUPID;
import static com.azure.tools.bomgenerator.Utils.EXCLUSION_LIST;
import static com.azure.tools.bomgenerator.Utils.GENERATE_MODE;
import static com.azure.tools.bomgenerator.Utils.INPUT_DEPENDENCY_PATTERN;
import static com.azure.tools.bomgenerator.Utils.POM_TYPE;
import static com.azure.tools.bomgenerator.Utils.SDK_DEPENDENCY_PATTERN;
import static com.azure.tools.bomgenerator.Utils.STRING_SPLIT_BY_COLON;
import static com.azure.tools.bomgenerator.Utils.isPublishedArtifact;
import static com.azure.tools.bomgenerator.Utils.parsePomFileContent;
import static com.azure.tools.bomgenerator.Utils.parsePomFileModel;
import static com.azure.tools.bomgenerator.Utils.toBomDependencyNoVersion;
import static com.azure.tools.bomgenerator.Utils.validateNotNullOrEmpty;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

public class BomGenerator {
    private String outputFileName;
    private String inputFileName;
    private String pomFileName;
    private String overriddenInputDependenciesFileName;
    private String reportFileName;
    private String mode;
    private String outputDirectory;
    private String inputDirectory;

    private static Logger logger = LoggerFactory.getLogger(BomGenerator.class);

    BomGenerator(String inputDirectory, String outputDirectory, String mode) throws FileNotFoundException {
        validateNotNullOrEmpty(inputDirectory, "inputDirectory");
        validateNotNullOrEmpty(outputDirectory, "outputDirectory");

        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.mode = (mode == null ? GENERATE_MODE : mode);

        parseInputs();
        validateInputs();

        Path outputDirPath = Paths.get(outputDirectory);
        outputDirPath.toFile().mkdirs();
    }

   private void parseInputs() throws FileNotFoundException {
        this.outputFileName = Paths.get(outputDirectory, "pom.xml").toString();
        this.reportFileName = Paths.get(outputDirectory, "dependency_conflictlist.html").toString();
        this.inputFileName = Paths.get(inputDirectory, "version_client.txt").toString();
        this.pomFileName = Paths.get(inputDirectory, "pom.xml").toString();
        this.overriddenInputDependenciesFileName = Paths.get(inputDirectory, "dependencies.txt").toString();
   }

    private void validateInputs() throws FileNotFoundException {
        switch (this.mode) {
            case ANALYZE_MODE:
                validateFilePath(this.pomFileName);
                break;

            case GENERATE_MODE:
                // In generate mode, we should have the inputFile, outputFile and the pomFile.
                validateFilePath(this.pomFileName);
                validateFilePath(this.inputFileName);
                break;
        }
    }

   private void validateFilePath(String filePath) throws FileNotFoundException {
        if(Files.notExists(Paths.get(filePath))) {
            throw new FileNotFoundException(String.format("%s not found.", filePath));
        }
   }

    public boolean run() {
        switch (mode) {
            case ANALYZE_MODE:
                return validate();

            case GENERATE_MODE:
                return generate();

            default:
                logger.error("Unknown value for mode: {}", mode);
                break;
        }

        return false;
    }

    private boolean validate() {
        var inputDependencies = parsePomFileContent(this.pomFileName);
        DependencyAnalyzer analyzer = new DependencyAnalyzer(inputDependencies, null, this.reportFileName);
        return !analyzer.validate();
    }

    private boolean generate() {
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
        if (!validationFailed) {
            writeBom(outputDependencies);
            return true;
        }

        logger.trace("Validation for the BOM failed. Exiting...");
        return false;
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
        return parsePomFileModel(this.pomFileName, Model.class);
    }

    private void writeModel(String inputFileName, String outputFileName, Model model) {
        // First read the pom file.
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document oldBomDoc = db.parse(new File(inputFileName));
            Node oldDependencyManagementNode = oldBomDoc.getElementsByTagName("dependencyManagement").item(0);
            Node parentNode = oldDependencyManagementNode.getParentNode();

            // Now add the other node to this list.
            XmlMapper mapper = new XmlMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            BomDependencyManagement dependencyManagement = new BomDependencyManagement(model.getDependencyManagement().getDependencies());
            String dependencies = mapper.writeValueAsString(dependencyManagement);
            DocumentBuilder newBomDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document newDependencies = newBomDoc.parse(new InputSource(new StringReader(dependencies)));
            Node newDependencyManagementNode = newDependencies.getElementsByTagName("dependencyManagement").item(0);
            Node firstDocImportedDependencyManagementNode = oldBomDoc.importNode(newDependencyManagementNode, true);
            parentNode.replaceChild(firstDocImportedDependencyManagementNode, oldDependencyManagementNode);

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            DOMSource source = new DOMSource(oldBomDoc);
            FileWriter writer = new FileWriter(outputFileName);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

        } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
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

        // Remove external dependencies from the BOM.
        dependencies = dependencies.stream().filter(dependency -> BASE_AZURE_GROUPID.equals(dependency.getGroupId())).collect(Collectors.toList());
        management.setDependencies(dependencies);
        writeModel(this.pomFileName, this.outputFileName, model);
    }
}
