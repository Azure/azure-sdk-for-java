// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BomDependency;
import com.azure.tools.bomgenerator.models.BomDependencyNoVersion;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystemBase;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomlessResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static final String COMMANDLINE_INPUTDIRECTORY = "inputdir";
    public static final String COMMANDLINE_OUTPUTDIRECTORY = "outputdir";
    public static final String COMMANDLINE_MODE = "mode";
    public static final String ANALYZE_MODE = "analyze";
    public static final String GENERATE_MODE = "generate";
    public  static final Pattern COMMANDLINE_REGEX = Pattern.compile("-(.*)=(.*)");
    public static final List<String> EXCLUSION_LIST = Arrays.asList("azure-spring-data-cosmos", "azure-spring-data-cosmos-test", "azure-core-test", "azure-sdk-all", "azure-sdk-parent", "azure-client-sdk-parent");
    public static final Pattern SDK_DEPENDENCY_PATTERN = Pattern.compile("com.azure:(.+);(.+);(.+)");
    public static final String BASE_AZURE_GROUPID = "com.azure";
    public static final String AZURE_TEST_LIBRARY_IDENTIFIER = "-test";
    public static final String AZURE_PERF_LIBRARY_IDENTIFIER = "-perf";
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final Pattern STRING_SPLIT_BY_DOT = Pattern.compile("[.]");
    public static final Pattern STRING_SPLIT_BY_COLON = Pattern.compile("[:]");
    public static final Pattern INPUT_DEPENDENCY_PATTERN = Pattern.compile("(.+);(.*)");
    public static final String PROJECT_VERSION = "project.version";

    public static final HashSet<String> RESOLVED_EXCLUSION_LIST = new HashSet<>(Arrays.asList(
       "junit-jupiter-api"
    ));

    public static final HashSet<String> IGNORE_CONFLICT_LIST = new HashSet<>(/*Arrays.asList(
        "slf4j-api" // slf4j is compatible across versions.
    )*/);

    public static final String POM_TYPE = "pom";
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    static void validateNotNullOrEmpty(String argValue, String argName) {
        if(argValue == null || argValue.isEmpty()) {
            throw new NullPointerException(String.format("%s can't be null", argName));
        }
    }

    static void validateNotNullOrEmpty(String[] argValue, String argName) {
        if(Arrays.stream(argValue).anyMatch(value -> value == null || value.isEmpty())) {
            throw new IllegalArgumentException(String.format("%s can't be null", argName));
        }
    }

    static MavenResolverSystemBase<PomEquippedResolveStage, PomlessResolveStage, MavenStrategyStage, MavenFormatStage> getMavenResolver() {
        return Maven.configureResolver().withMavenCentralRepo(true);
    }

    static boolean isPublishedArtifact(BomDependency dependency) {
        try {
            return getResolvedArtifact(dependency) != null;
        } catch (Exception ex) {
            logger.error(ex.toString());
        }
        return false;
    }

    static MavenResolvedArtifact getResolvedArtifact(MavenDependency dependency) {
        MavenResolvedArtifact mavenResolvedArtifact = null;

        mavenResolvedArtifact = getMavenResolver()
            .addDependency(dependency)
            .resolve()
            .withoutTransitivity()
            .asSingleResolvedArtifact();

        return mavenResolvedArtifact;
    }

    static void validateNull(String argValue, String argName) {
        if(argValue != null) {
            throw new IllegalArgumentException(String.format("%s should be null", argName));
        }
    }

    static void validateValues(String argName, String argValue, String ... expectedValues) {
        if(Arrays.stream(expectedValues).noneMatch(a -> a.equals(argValue))) {
            throw new IllegalArgumentException(String.format("%s must match %s", argName, Arrays.toString(expectedValues)));
        }
    }

    static List<BomDependency> getExternalDependenciesContent(List<Dependency> dependencies) {
        List<BomDependency> allResolvedDependencies = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            List<BomDependency> resolvedDependencies = getPomFileContent(dependency);

            if (resolvedDependencies != null) {
                allResolvedDependencies.addAll(resolvedDependencies);
            }
        }

        return allResolvedDependencies;
    }

    static List<BomDependency> getPomFileContent(Dependency dependency) {
            String[] groups = STRING_SPLIT_BY_DOT.split(dependency.getGroupId());
            String url = null;
            if(groups.length == 2) {
                url = "https://repo1.maven.org/maven2" + "/" + groups[0] + "/" + groups[1] + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/" + dependency.getArtifactId() + "-" + dependency.getVersion() + ".pom";
            }
            else if (groups.length == 3) {
                url = "https://repo1.maven.org/maven2" + "/" + groups[0] + "/" + groups[1] + "/" + groups[2] + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/" + dependency.getArtifactId() + "-" + dependency.getVersion() + ".pom";
            }
            else {
                throw new UnsupportedOperationException("Can't parse the external BOM file.");
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("accept", "application/xml")
                .timeout(Duration.ofMillis(5000))
                .build();

            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if(response.statusCode() == 200) {
                        try (InputStreamReader reader = new InputStreamReader(response.body())) {
                            return Utils.parsePomFileContent(reader);
                        }
                        catch (IOException ex) {
                            logger.error("Failed to read contents for {}", dependency.toString());
                        }
                    }

                    return null;
                }).join();
    }

    static BomDependencyNoVersion toBomDependencyNoVersion(BomDependency bomDependency) {
        return new BomDependencyNoVersion(bomDependency.getGroupId(), bomDependency.getArtifactId());
    }

    static List<BomDependency> parsePomFileContent(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            return parsePomFileContent(reader);
        } catch (IOException exception) {
            logger.error("Failed to read the contents of the pom file: {}", fileName);
        }

        return new ArrayList<>();
    }

    static List<BomDependency> parsePomFileContent(Reader responseStream) {
        List<BomDependency> bomDependencies = new ArrayList<>();

        ObjectMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            Model value = mapper.readValue(responseStream, Model.class);
            List<Dependency> dependencies = value.getDependencies();

            if(dependencies == null) {
                return bomDependencies;
            }

            for(Dependency dependency : dependencies) {
                ScopeType scopeType = ScopeType.COMPILE;

                if("test".equals(dependency.getScope())) {
                    scopeType = ScopeType.TEST;
                }

                bomDependencies.add(new BomDependency(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getVersion(),
                    scopeType));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return bomDependencies.stream().distinct().collect(Collectors.toList());
    }

    static List<BomDependency> parseBomFileContent(Reader responseStream) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(responseStream);
            DependencyManagement management = model.getDependencyManagement();

            return management.getDependencies().stream().map(dep -> {
                String version = getPropertyName(dep.getVersion());

                while(model.getProperties().getProperty(version) != null) {
                    version = getPropertyName(model.getProperties().getProperty(version));

                    if(version.equals(PROJECT_VERSION)) {
                        version = model.getVersion();
                    }
                }

                if(version == null) {
                    version = dep.getVersion();
                }

                BomDependency bomDependency = new BomDependency(dep.getGroupId(), dep.getArtifactId(), version);
                return bomDependency;
            }).collect(Collectors.toList());
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getPropertyName(String propertyValue) {
        if(propertyValue.startsWith("${")) {
            return propertyValue.substring(2, propertyValue.length() - 1);
        }

        return propertyValue;
    }
}
