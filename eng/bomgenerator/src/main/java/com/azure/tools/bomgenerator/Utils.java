package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BomDependency;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
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
    public static final String COMMANDLINE_INPUTFILE = "inputfile";
    public static final String COMMANDLINE_OUTPUTFILE = "outputfile";
    public static final String COMMANDLINE_POMFILE = "pomfile";
    public static final String COMMANDLINE_EXTERNALDEPENDENCIES = "externalDependencies";
    public static final String COMMANDLINE_GROUPID = "groupid";
    public  static final Pattern COMMANDLINE_REGEX = Pattern.compile("-(.*)=(.*)");
    public static final List<String> EXCLUSION_LIST = Arrays.asList("azure-spring-data-cosmos", "azure-spring-data-cosmos-test", "azure-core-test", "azure-sdk-all", "azure-sdk-parent", "azure-client-sdk-parent");
    public static final Pattern SDK_DEPENDENCY_PATTERN = Pattern.compile("com.azure:(.+);(.+);(.+)");
    public static final String AZURE_CORE_GROUPID = "com.azure";
    public static final String AZURE_TEST_LIBRARY_IDENTIFIER = "-test";
    public static final String AZURE_PERF_LIBRARY_IDENTIFIER = "-perf";

    public static final HashSet<String> RESOLVED_EXCLUSION_LIST = new HashSet<>(Arrays.asList(
       "junit-jupiter-api"
    ));

    public static final String POM_TYPE = "pom";

    public static List<BomDependency> getExternalDependenciesContent(List<Dependency> dependencies) {
        HttpClient httpClient = HttpClient.newHttpClient();
        List<BomDependency> allResolvedDependencies = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            List<BomDependency> resolvedDependencies = getPomFileContent(httpClient, dependency);

            if (resolvedDependencies != null) {
                allResolvedDependencies.addAll(resolvedDependencies);
            }
        }

        return allResolvedDependencies;
    }


    public static List<BomDependency> getPomFileContent(HttpClient client, Dependency dependency) {
            String[] groups = dependency.getGroupId().split("[.]");
            String url = null;
            if(groups.length == 2) {
                url = "https://repo1.maven.org/maven2" + "/" + groups[0] + "/" + groups[1] + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/" + dependency.getArtifactId() + "-" + dependency.getVersion() + ".pom";
            }
            else if (groups.length == 3) {
                url = "https://repo1.maven.org/maven2" + "/" + groups[0] + "/" + groups[1] + "/" + groups[2] + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/" + dependency.getArtifactId() + "-" + dependency.getVersion() + ".pom";
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("accept", "application/xml")
                .timeout(Duration.ofMillis(5000))
                .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if(response.statusCode() == 200) {
                        return Utils.parsePomFileContent(response.body());
                    }

                    return null;
                }).join();
    }

    private static List<BomDependency> parsePomFileContent(InputStream responseStream) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(responseStream);
            DependencyManagement management = model.getDependencyManagement();

            return management.getDependencies().stream().map(dep -> {
                String version = getPropertyName(dep.getVersion());

                while(model.getProperties().getProperty(version) != null) {
                    version = getPropertyName(model.getProperties().getProperty(version));
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
