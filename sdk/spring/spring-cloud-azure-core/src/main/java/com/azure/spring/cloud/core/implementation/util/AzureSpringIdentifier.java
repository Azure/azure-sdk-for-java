// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Util class for AzureSpringIdentifier
 */
public final class AzureSpringIdentifier {

    private AzureSpringIdentifier() {

    }
    //    There is 24 char limitation about the app id. So some abbreviation needs to be applied:
    //    az: for Azure
    //    sp: for Spring
    //    sc: for Spring Cloud
    //    sd: for Spring Data
    //    ss: for Spring Streams
    //    kv: for Key Vault
    //    se: for Security
    //    jca: for JCA
    //    ct: for Certificates
    //    sb: for Storage Blobs
    //    sf: for Storage Files
    //    eh: for Event Hub
    //    bus: for Service Bus
    //    cfg: for App Config
    //    cos: for Cosmos
    //    aad: for AAD
    //    b2c: for AAD B2C
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureSpringIdentifier.class);
    public static final String VERSION = getVersion();
    public static final String AZURE_SPRING_APP_CONFIG = "az-sp-cfg/" + VERSION;
    public static final String AZURE_SPRING_EVENT_HUBS = "az-sp-eh/" + VERSION;
    //Kafka supports set client software name and version individually.
    public static final String AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH = "az-sp-kafka";
    public static final String AZURE_SPRING_KEY_VAULT_SECRETS = "az-sp-kv/" + VERSION;
    public static final String AZURE_SPRING_KEY_VAULT_CERTIFICATES = "az-sp-kv-ct/" + VERSION;

    /**
     * Azure Spring ServiceBus
     */
    public static final String AZURE_SPRING_SERVICE_BUS = "az-sp-bus/" + VERSION;

    /**
     * Azure Spring Storage Blob
     */
    public static final String AZURE_SPRING_STORAGE_BLOB = "az-sp-sb/" + VERSION;

    /**
     * Azure Spring Storage Files
     */
    public static final String AZURE_SPRING_STORAGE_FILES = "az-sp-sf/" + VERSION;
    public static final String AZURE_SPRING_COSMOS = "az-sp-cos/" + VERSION;
    public static final String AZURE_SPRING_STORAGE_QUEUE = "az-sp-sq/" + VERSION;
    public static final String AZURE_SPRING_INTEGRATION_STORAGE_QUEUE = "az-si-sq/" + VERSION;
    public static final String AZURE_SPRING_INTEGRATION_SERVICE_BUS = "az-si-sb/" + VERSION;
    public static final String AZURE_SPRING_INTEGRATION_EVENT_HUBS = "az-si-eh/" + VERSION;

    /**
     * AZURE_SPRING_AAD does not contain VERSION, because AAD server support 2 headers: 1. x-client-SKU; 2.
     * x-client-VER;
     */
    public static final String AZURE_SPRING_AAD = "az-sp-aad";

    /**
     * Azure Spring B2C
     */
    public static final String AZURE_SPRING_B2C = "az-sp-b2c";

    private static String getVersion() {
        String version = "unknown";
        try {
            Properties properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("azure-spring-identifier.properties"));
            version = properties.getProperty("version");
        } catch (IOException e) {
            LOGGER.warn("Can not get version.");
        }
        return version;
    }
}
