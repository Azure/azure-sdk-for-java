package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for fetching the user agent properties like client library name and version.
 */
public class AzureUserAgentUtil {

    private static final String UNKNOWN_VERSION = "UnknownVersion";
    private static final String UNKNOWN_NAME = "UnknownName";
    private static final String VERSION_PROPERTY = "version";
    private static final String NAME_PROPERTY = "name";
    private static final Map<String, UserAgentProperties> USER_AGENT_PROPERTIES = new ConcurrentHashMap<>();

    private AzureUserAgentUtil() {
        // no instances for this class
    }

    /**
     * Helper method that returns the {@link UserAgentProperties} defined in the {@code propertiesFileName}.
     *
     * @param propertiesFileName The file name defining the user agent properties.
     * @return the {@link UserAgentProperties}.
     */
    public static UserAgentProperties getUserAgentProperties(String propertiesFileName) {
        return USER_AGENT_PROPERTIES.computeIfAbsent(propertiesFileName, AzureUserAgentUtil::readFromPropertiesFile);
    }

    private static UserAgentProperties readFromPropertiesFile(String propertiesFileName) {
        ClientLogger logger = new ClientLogger(AzureUserAgentUtil.class);
        String version = null;
        String name = null;
        try (InputStream inputStream = AzureUserAgentUtil.class.getClassLoader()
            .getResourceAsStream(propertiesFileName)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty(VERSION_PROPERTY);
            name = properties.getProperty(NAME_PROPERTY);
            logger.info("User agent properties: name = " + name + ", version = " + version + ", properties file = "
                + propertiesFileName);
        } catch (Exception ex) {
            logger.warning("Failed to get user agent properties from " + propertiesFileName, ex);
        }
        version = version != null ? version : UNKNOWN_VERSION;
        name = name != null ? name : UNKNOWN_NAME;
        UserAgentProperties userAgentProperties = new UserAgentProperties(name, version);
        return userAgentProperties;
    }
}
