package com.azure.tools.bomgenerator.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "dependencyManagement")
public class BomDependencyManagement {

    @JacksonXmlElementWrapper(localName = "dependencies")
    @JacksonXmlProperty(localName = "dependency")
    private List<Dependency> dependencies;

    public BomDependencyManagement(List<org.apache.maven.model.Dependency> dependencies) {
        this.dependencies = dependencies
            .stream()
            .map(dependency ->
                new Dependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getType(), dependency.getScope()))
            .collect(Collectors.toList());
    }
}

class Dependency {
    @JacksonXmlProperty(localName = "groupId")
    private String groupId;

    @JacksonXmlProperty(localName = "artifactId")
    private String artifactId;

    @JacksonXmlProperty(localName = "version")
    private String version;

    @JacksonXmlProperty(localName = "type")
    private String type;

    @JacksonXmlProperty(localName = "scope")
    private String scope;

    Dependency(String groupId, String artifactId, String version, String type, String scope) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;

        // We only want to serialize the pom type.
        if(type.equals("pom")) {
            this.type = type;
        }
        this.scope = scope;
    }
}
