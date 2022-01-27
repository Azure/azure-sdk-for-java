package com.azure.sdk.build.tool.util;

import com.azure.sdk.build.tool.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MavenUtils {
    private static final Logger LOGGER = Logger.getInstance();

    public static String toGAV(Artifact a) {
        return a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion();
    }

    /**
     * Gets the latest released Azure SDK BOM version from Maven repository.
     * @return The latest Azure SDK BOM version or {@code null} if an error occurred while retrieving the latest
     * version.
     */
    public static String getLatestArtifactVersion(String groupId, String artifactId) {
        HttpURLConnection connection = null;
        try {
            groupId = groupId.replace(".", "/");
            URL url = new URL("https://repo1.maven.org/maven2/" + groupId + "/" + artifactId + "/maven-metadata.xml");
            System.out.println(url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/xml");
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
                for (int i = versionsList.getLength() - 1; i >=0; i--) {
                    Node versionNode = versionsList.item(i);
                    if (!versionNode.getTextContent().contains("beta")) {
                        latestVersion = versionNode.getTextContent();
                        break;
                    }
                }

                LOGGER.info("The latest version for SDK BOM is " + latestVersion);
                return latestVersion;
            } else {
                LOGGER.info("Got a non-successful response for  " + artifactId + ": " + responseCode);
            }
        } catch (Exception exception) {
            LOGGER.error("Got error getting latest maven dependency version ");
            exception.printStackTrace();
        } finally {
            if (connection != null) {
                // closes the input streams and discards the socket
                connection.disconnect();
            }
        }
        return null;
    }
}
