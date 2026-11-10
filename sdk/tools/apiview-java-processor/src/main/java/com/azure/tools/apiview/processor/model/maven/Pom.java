package com.azure.tools.apiview.processor.model.maven;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This represents an entire Maven POM file, consisting of the GAV, parent GAV, dependencies, and other metadata.
 */
public class Pom {
    private static final String ARTIFACT_ID = "artifactId";
    private static final String PACKAGE_VERSION = "version";

    // Expected format <artifact-id>-<majorversion>.<minorversion>.<patchversion>(-beta.<betaversion>)-sources.jar
    private static final Pattern SOURCES_JAR_PATTERN = Pattern.compile("(.+)-(\\d+\\.\\d+.\\d+(-.+)?)-sources\\.jar");

    private final Gav gav;
    private final Gav parent;

    private final String name;
    private final String description;

    private final List<Dependency> dependencies;

    private final Float jacocoMinLineCoverage;
    private final Float jacocoMinBranchCoverage;

    private final String checkstyleExcludes;

    private final boolean fileExists;

    // These are the dependencies specifies in the maven-enforcer that are allowed
    private final List<String> allowedDependencies;

    public static Pom fromSourcesJarFile(File sourcesJarFile) {
        Pom pom = null;
        final String filename = sourcesJarFile.getName();
        Map<String, String> filenameParts = parseFilename(filename);

        // we will firstly try to get the artifact ID from the maven file inside the jar file...if it exists
        try (final JarFile jarFile = new JarFile(sourcesJarFile)) {
            final Enumeration<JarEntry> enumOfJar = jarFile.entries();
            while (enumOfJar.hasMoreElements()) {
                final JarEntry entry = enumOfJar.nextElement();
                final String fullPath = entry.getName();

                // use the pom.xml of this artifact only
                // shaded jars can contain a pom.xml file each for every shaded dependencies
                if (fullPath.startsWith("META-INF/maven") && fullPath.endsWith(filenameParts.get(ARTIFACT_ID) + "/pom.xml")) {
                    pom = new Pom(jarFile.getInputStream(entry));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if we can't get the maven details out of the Jar file, we will just use the filename itself...
        if (pom == null) {
            // we failed to read it from the maven pom file, we will just take the file name without any extension
            pom = new Pom("", filenameParts.get(ARTIFACT_ID), filenameParts.get(PACKAGE_VERSION), false);
        }

        return pom;
    }

    static Map<String, String> parseFilename(String filename) {
        Matcher matcher = SOURCES_JAR_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Filename does not match the expected package naming pattern " + filename
                    + ". Expected format <artifact-id>-<majorversion>.<minorversion>.<patchversion>(-beta.<betaversion>)-sources.jar");
        }
        Map<String, String> filenameParts = new HashMap<>();
        String artifactId = matcher.group(1);
        String packageVersion = matcher.group(2);
        filenameParts.put(ARTIFACT_ID, artifactId);
        filenameParts.put(PACKAGE_VERSION, packageVersion);
        return filenameParts;
    }

    private Pom(final String groupId, final String artifactId, final String version, boolean fileExists) {
        this.gav = new Gav(groupId, artifactId, version);
        this.fileExists = fileExists;
        this.dependencies = new ArrayList<>();
        this.allowedDependencies = new ArrayList<>();
        this.parent = null;
        this.name = null;
        this.description = null;
        this.jacocoMinLineCoverage = null;
        this.jacocoMinBranchCoverage = null;
        this.checkstyleExcludes = null;
    }

    private Pom(InputStream pomFileStream) throws IOException {
        this.dependencies = new ArrayList<>();
        this.allowedDependencies = new ArrayList<>();
        this.fileExists = true;

        try {
            // use xpath to get the artifact ID
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builderFactory.setXIncludeAware(false);
            builderFactory.setExpandEntityReferences(false);

            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            final Document xmlDocument = builder.parse(pomFileStream);
            final XPath xPath = XPathFactory.newInstance().newXPath();

            this.gav = createGav(xPath, xmlDocument, "/project");
            this.parent = createGav(xPath, xmlDocument, "/project/parent");

            if (!gav.isValid()) {
                throw new IOException("Cannot parse given file as a Maven POM");
            }

            // jacoco configuration
            Node n = (Node) xPath.evaluate("/project/properties/jacoco.min.linecoverage", xmlDocument, XPathConstants.NODE);
            this.jacocoMinLineCoverage = n == null ? null : Float.parseFloat(n.getTextContent());

            n = (Node) xPath.evaluate("/project/properties/jacoco.min.branchcoverage", xmlDocument, XPathConstants.NODE);
            this.jacocoMinBranchCoverage = n == null ? null : Float.parseFloat(n.getTextContent());

            // checkstyle excludes
            n = (Node) xPath.evaluate("/project/build/plugins/plugin/artifactId[text()='maven-checkstyle-plugin']/../configuration/excludes", xmlDocument, XPathConstants.NODE);
            this.checkstyleExcludes = n == null ? null : n.getTextContent();

            // Maven name
            n = (Node) xPath.evaluate("/project/name", xmlDocument, XPathConstants.NODE);
            this.name = (n == null) ? null : n.getTextContent();

            // Maven description
            n = (Node) xPath.evaluate("/project/description", xmlDocument, XPathConstants.NODE);
            this.description = (n == null) ? null : n.getTextContent();

            // actual dependencies
            final String dependencyExpression = "/project/dependencies/dependency";
            NodeList dependenciesNodeList = (NodeList) xPath.evaluate(dependencyExpression, xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < dependenciesNodeList.getLength(); i++) {
                Node dep = dependenciesNodeList.item(i);
                String depGroupId = xPath.evaluate("groupId", dep);
                String depArtifactId = xPath.evaluate("artifactId", dep);
                String depVersion = xPath.evaluate("version", dep);
                String depScope = xPath.evaluate("scope", dep);
                dependencies.add(new Dependency(depGroupId, depArtifactId, depVersion, depScope));
            }

            // allowed dependencies
            final String allowedDependencies = "/project/build/plugins/plugin/artifactId[text()='maven-enforcer-plugin']/../configuration/rules/bannedDependencies/includes/include";
            dependenciesNodeList = (NodeList) xPath.evaluate(allowedDependencies, xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < dependenciesNodeList.getLength(); i++) {
                this.allowedDependencies.add(dependenciesNodeList.item(i).getTextContent().trim());
            }
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            throw new IOException("Cannot parse given file as a Maven POM");
        }
    }

    public String getGroupId() {
        return gav.getGroupId() != null ? gav.getGroupId() : parent.getGroupId();
    }

    public String getArtifactId() {
        return gav.getArtifactId();
    }

    public String getVersion() {
        return gav.getVersion() != null ? gav.getVersion() : parent.getVersion();
    }

    public Gav getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public Float getJacocoMinLineCoverage() {
        return jacocoMinLineCoverage;
    }

    public Float getJacocoMinBranchCoverage() {
        return jacocoMinBranchCoverage;
    }

    public String getCheckstyleExcludes() {
        return checkstyleExcludes;
    }

    public List<String> getAllowedDependencies() {
        return allowedDependencies;
    }

    /**
     * Sometimes we can't find a pom file, so we fake it with the info we do have.
     */
    public boolean isPomFileReal() {
        return fileExists;
    }

    private Gav createGav(final XPath xPath, final Document xmlDocument, final String root) throws XPathExpressionException {
        final String groupIdExpression = root + "/groupId";
        final Node groupIdNode = (Node) xPath.evaluate(groupIdExpression, xmlDocument, XPathConstants.NODE);
        String groupId = groupIdNode == null ? "" : groupIdNode.getTextContent();

        final String artifactIdExpression = root + "/artifactId";
        final Node artifactIdNode = (Node) xPath.evaluate(artifactIdExpression, xmlDocument, XPathConstants.NODE);
        final String artifactId = artifactIdNode == null ? "" : artifactIdNode.getTextContent();

        final String versionExpression = root + "/version";
        final Node versionNode = (Node) xPath.evaluate(versionExpression, xmlDocument, XPathConstants.NODE);
        String version = versionNode == null ? "" : versionNode.getTextContent();

        // it is possible to inherit the groupId and version from the parent, so if group ID or version is empty here,
        // lets go up to the parent to get it from there
        if (groupId.isEmpty()) {
            final String parentGroupIdExpression = root + "/parent/groupId";
            final Node parentGroupIdNode = (Node) xPath.evaluate(parentGroupIdExpression, xmlDocument, XPathConstants.NODE);
            groupId = parentGroupIdNode == null ? "" : parentGroupIdNode.getTextContent();
        }
        if (version.isEmpty()) {
            final String parentVersionExpression = root + "/parent/version";
            final Node parentVersionNode = (Node) xPath.evaluate(parentVersionExpression, xmlDocument, XPathConstants.NODE);
            version = parentVersionNode == null ? "" : parentVersionNode.getTextContent();
        }

        return new Gav(groupId, artifactId, version);
    }
}
