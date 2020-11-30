// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Contains the configurations for tests.
 *
 * For running tests, you can pass a customized endpoint configuration in one of the following
 * ways:
 * <ul>
 * <li>-DACCOUNT_KEY="[your-key]" -ACCOUNT_HOST="[your-endpoint]" as JVM
 * command-line option.</li>
 * <li>You can set ACCOUNT_KEY and ACCOUNT_HOST as environment variables.</li>
 * <li>You can have a properties file with ACCOUNT_KEY AND ACCOUNT_HOST, etc.</li>
 * </ul>
 *
 * If none of the above is set, emulator endpoint will be used.
 */
public final class TestConfigurations {
    private static Logger logger = LoggerFactory.getLogger(TestConfigurations.class);
    private static Properties properties = loadProperties();

    private final static String COSMOS_EMULATOR_KEY = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
    private final static String COSMOS_EMULATOR_HOST = "https://localhost:8081/";

    // REPLACE MASTER_KEY and HOST with values from your Azure Cosmos DB account.
    // The default values are credentials of the local emulator, which are not used in any production environment.
    // <!--[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine")]-->
    public final static String MASTER_KEY =
        properties.getProperty("ACCOUNT_KEY",
                    StringUtils.defaultString(Strings.emptyToNull(
                            System.getenv().get("ACCOUNT_KEY")),
                            COSMOS_EMULATOR_KEY));

    public final static String SECONDARY_MASTER_KEY =
        properties.getProperty("SECONDARY_ACCOUNT_KEY",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("SECONDARY_ACCOUNT_KEY")),
                COSMOS_EMULATOR_KEY));

    public final static String HOST =
        properties.getProperty("ACCOUNT_HOST",
                    StringUtils.defaultString(Strings.emptyToNull(
                            System.getenv().get("ACCOUNT_HOST")),
                            COSMOS_EMULATOR_HOST));

    public final static String CONSISTENCY =
        properties.getProperty("ACCOUNT_CONSISTENCY",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("ACCOUNT_CONSISTENCY")), "Strong"));

    public final static String PREFERRED_LOCATIONS =
        properties.getProperty("PREFERRED_LOCATIONS",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("PREFERRED_LOCATIONS")), null));

    public final static String MAX_RETRY_LIMIT =
        properties.getProperty("MAX_RETRY_LIMIT",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("MAX_RETRY_LIMIT")),
                                                         "2"));

    public final static String DESIRED_CONSISTENCIES =
        properties.getProperty("DESIRED_CONSISTENCIES",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("DESIRED_CONSISTENCIES")),
                                                         null));

    public final static String PROTOCOLS =
        properties.getProperty("PROTOCOLS",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("PROTOCOLS")),
                                                         null));

    /**
     * If ${ProjectPath}/cosmos-v4.properties is present, it will be used
     * otherwise, if ~/cosmos-v4.props is present, it will be used
     * otherwise, system properties will be used as default.
     * @return loaded properties
     */
    private static Properties loadProperties() {
        Path root = FileSystems.getDefault().getPath("").toAbsolutePath();
        Path propertiesInProject = Paths.get(root.toString(),"../cosmos-v4.properties");

        Properties props = loadFromPathIfExists(propertiesInProject);
        if (props != null) {
            return props;
        }

        Path propertiesInUserHome = Paths.get(System.getProperty("user.home"), "cosmos-v4.properties");
        props = loadFromPathIfExists(propertiesInUserHome);
        if (props != null) {
            return props;
        }

        return System.getProperties();
    }

    private static Properties loadFromPathIfExists(Path propertiesFilePath) {
        if (Files.exists(propertiesFilePath)) {
            try (InputStream in = Files.newInputStream(propertiesFilePath)) {
                Properties props = new Properties();
                props.load(in);
                logger.info("properties loaded from {}", propertiesFilePath);
                return props;
            } catch (Exception e) {
                logger.error("Loading properties {} failed", propertiesFilePath, e);
            }
        }
        return null;
    }
}
