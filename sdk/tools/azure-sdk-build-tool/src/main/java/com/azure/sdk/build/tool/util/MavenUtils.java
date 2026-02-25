// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util;

import com.azure.sdk.build.tool.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class to perform Maven related operations.
 */
public class MavenUtils {
    private static final Logger LOGGER = Logger.getInstance();

    /**
     * Creates artifact string representation of an artifact.
     * @param artifact The artifact.
     * @return The string representation of the artifact.
     */
    public static String toGAV(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }

    /**
     * Creates artifact string representation of a dependency.
     * @param dependency The dependency.
     * @return The string representation of the dependency.
     */
    public static String toGAV(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
    }

    /**
     * Gets the latest released version of the given artifact from Maven repository.
     * @param groupId The group id of the artifact.
     * @param artifactId The artifact id of the artifact.
     * @return The latest version or {@code null} if an error occurred while retrieving the latest
     * version.
     */
    public static String getLatestArtifactVersion(String groupId, String artifactId) {
        HttpURLConnection connection = null;
        try {
            groupId = groupId.replace(".", "/");
            URL url = new URL("https://repo1.maven.org/maven2/" + groupId + "/" + artifactId + "/maven-metadata.xml");
            if (LOGGER.isVerboseEnabled()) {
                LOGGER.verbose(url.toString());
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/xml");
            connection.setRequestProperty("Content-signal", "search=yes,ai-train=no");
            connection.setRequestProperty("User-Agent", "azure-sdk-for-java");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == responseCode) {
                InputStream responseStream = connection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(responseStream);

                // Maven-metadata.xml lists versions from oldest to newest. Therefore, we want the bottom-most version
                // that is not a beta, preview, etc release.
                NodeList versionsList = doc.getElementsByTagName("version");
                String latestVersion = null;
                for (int i = versionsList.getLength() - 1; i >= 0; i--) {
                    Node versionNode = versionsList.item(i);
                    if (!versionNode.getTextContent().contains("beta")) {
                        latestVersion = versionNode.getTextContent();
                        break;
                    }
                }

                if (LOGGER.isVerboseEnabled()) {
                    LOGGER.verbose("The latest version of " + artifactId + " is " + latestVersion);
                }
                return latestVersion;
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Got a non-successful response for  " + artifactId + ": " + responseCode);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException exception) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Got error getting latest maven dependency version. " + exception.getMessage());
            }
        } finally {
            if (connection != null) {
                // closes the input streams and discards the socket
                connection.disconnect();
            }
        }
        return null;
    }
}
