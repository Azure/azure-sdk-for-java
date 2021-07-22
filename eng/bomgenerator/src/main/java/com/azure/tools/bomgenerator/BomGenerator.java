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
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.azure.tools.bomgenerator.Utils.AZURE_CORE_GROUPID;
import static com.azure.tools.bomgenerator.Utils.AZURE_PERF_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.AZURE_TEST_LIBRARY_IDENTIFIER;
import static com.azure.tools.bomgenerator.Utils.EXCLUSION_LIST;
import static com.azure.tools.bomgenerator.Utils.POM_TYPE;
import static com.azure.tools.bomgenerator.Utils.SDK_DEPENDENCY_PATTERN;

public class BomGenerator {
    private String outputFileName;
    private String inputFileName;
    private String pomFileName;

    private static Logger logger = LoggerFactory.getLogger(BomGenerator.class);

    BomGenerator(String inputFileName, String outputFileName, String pomFileName) {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.pomFileName = pomFileName;
    }

    public void generate() {
        List<BomDependency> inputDependencies = scan();
        List<BomDependency> externalDependencies = resolveExternalDependencies();

        // 1. Create the initial tree and reduce conflicts.
        // 2. And pick only those dependencies. that were in the input set, since they become the new roots of n-ary tree.
        DependencyAnalyzer analyzer = new DependencyAnalyzer(inputDependencies, externalDependencies);
        analyzer.reduce();
        Collection<BomDependency> outputDependencies = analyzer.getBomEligibleDependencies();

        // 2. Create the new tree for the BOM.
        analyzer = new DependencyAnalyzer(outputDependencies, externalDependencies);
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

    private List<BomDependency> scan() {
        List<BomDependency> inputDependencies = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(Paths.get(inputFileName))) {
                BomDependency dependency = scanDependency(line);

                if(dependency != null) {
                    inputDependencies.add(dependency);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return inputDependencies;
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
            logger.trace("Skipping dependency {}:{}", AZURE_CORE_GROUPID, artifactId);
            return null;
        }

        return new BomDependency(AZURE_CORE_GROUPID, artifactId, version);
    }

    private List<BomDependency> resolveExternalDependencies() {
        List<BomDependency> externalDependencies = new ArrayList<>();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(this.pomFileName));
            DependencyManagement management = model.getDependencyManagement();
            List<Dependency> externalBomDependencies = management.getDependencies().stream().filter(dependency -> dependency.getType().equals(POM_TYPE)).collect(Collectors.toList());
            externalDependencies.addAll(Utils.getExternalDependenciesContent(externalBomDependencies));
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return externalDependencies;
    }

    private void rewriteExistingBomFile() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(this.pomFileName));
            DependencyManagement management = model.getDependencyManagement();
            List<Dependency> dependencies = management.getDependencies();
            dependencies.sort(new DependencyComparator());
            management.setDependencies(dependencies);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(this.pomFileName), model);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void writeBom(Collection<BomDependency> bomDependencies) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(this.pomFileName));
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

            // Now that we have the new dependencies.
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(this.outputFileName), model);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
