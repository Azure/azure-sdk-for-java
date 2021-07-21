// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.b2c.AADB2CProperties;
import com.azure.spring.autoconfigure.cosmos.CosmosProperties;
import com.azure.spring.autoconfigure.jms.AzureServiceBusJMSProperties;
import com.azure.spring.autoconfigure.storage.StorageProperties;
import com.azure.spring.keyvault.KeyVaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Map legacy property to current spring properties.
 */
public class AzurePropertyEnvironmentPostProcessor {

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;

    private static final String LEGACY_AAD_PREFIX = "azure.activedirectory";
    private static final String LEGACY_AAD_B2C_PREFIX = "azure.activedirectory.b2c";
    private static final String LEGACY_COSMOS_PREFIX = "azure.cosmos";
    private static final String LEGACY_JMS_PREFIX = "spring.jms.servicebus";
    private static final String LEGACY_STORAGE_PREFIX = "azure.storage";
    private static final String LEGACY_KEYVAULT_PREFIX = "azure.keyvault";

    private static final Logger LOGGER = LoggerFactory.getLogger(AzurePropertyEnvironmentPostProcessor.class);

    private static final Map<String, String> LEGACY_TO_CURRENT = new HashMap<String, String>() {
        {
            put(LEGACY_AAD_PREFIX + ".clientId", AADAuthenticationProperties.PREFIX + ".credential.clientId");
            put(LEGACY_AAD_PREFIX + ".clientSecret", AADAuthenticationProperties.PREFIX + ".credential.clientSecret");
            put(LEGACY_AAD_PREFIX + ".tenantId", AADAuthenticationProperties.PREFIX + ".credential.tenantId");
            put(LEGACY_AAD_PREFIX + ".baseUri", AADAuthenticationProperties.PREFIX + ".environment.authorityHost");
            put(LEGACY_AAD_PREFIX + ".graphBaseUri", AADAuthenticationProperties.PREFIX + ".environment.graphBaseUri");
            put(LEGACY_AAD_PREFIX + ".userGroup.allowedGroupNames", AADAuthenticationProperties.PREFIX + ".userGroup"
                + ".allowedGroupNames");
            put(LEGACY_AAD_PREFIX + ".userGroup.allowedGroupIds", AADAuthenticationProperties.PREFIX + ".userGroup"
                + ".allowedGroupIds");
            put(LEGACY_AAD_PREFIX + ".userGroup.enableFullList", AADAuthenticationProperties.PREFIX + ".userGroup"
                + ".enableFullList");
            put(LEGACY_AAD_PREFIX + ".userNameAttribute", AADAuthenticationProperties.PREFIX
                + ".userNameAttribute");
            put(LEGACY_AAD_PREFIX + ".redirectUriTemplate", AADAuthenticationProperties.PREFIX
                + ".redirectUriTemplate");
            put(LEGACY_AAD_PREFIX + ".appIdUri", AADAuthenticationProperties.PREFIX + ".appIdUri");
            put(LEGACY_AAD_PREFIX + ".authenticateAdditionalParameters",
                AADAuthenticationProperties.PREFIX + ".authenticateAdditionalParameters");
            put(LEGACY_AAD_PREFIX + ".jwtConnectTimeout", AADAuthenticationProperties.PREFIX + ".jwtConnectTimeout");
            put(LEGACY_AAD_PREFIX + ".jwtReadTimeout", AADAuthenticationProperties.PREFIX + ".jwtReadTimeout");
            put(LEGACY_AAD_PREFIX + ".jwtSizeLimit", AADAuthenticationProperties.PREFIX + ".jwtSizeLimit");
            put(LEGACY_AAD_PREFIX + ".jwkSetCacheLifespan", AADAuthenticationProperties.PREFIX
                + ".jwkSetCacheLifespan");
            put(LEGACY_AAD_PREFIX + ".jwkSetCacheRefreshTime", AADAuthenticationProperties.PREFIX
                + ".jwkSetCacheRefreshTime");
            put(LEGACY_AAD_PREFIX + ".postLogoutRedirectUri", AADAuthenticationProperties.PREFIX
                + ".postLogoutRedirectUri");
            put(LEGACY_AAD_PREFIX + ".allowTelemetry", AADAuthenticationProperties.PREFIX + ".allowTelemetry");
            put(LEGACY_AAD_PREFIX + ".sessionStateless", AADAuthenticationProperties.PREFIX + ".sessionStateless");
            put(LEGACY_AAD_PREFIX + ".graphMembershipUri", AADAuthenticationProperties.PREFIX + ".graphMembershipUri");
            put(LEGACY_AAD_PREFIX + ".authorizationClients", AADAuthenticationProperties.PREFIX
                + ".authorizationClients");

            put(LEGACY_AAD_B2C_PREFIX + ".clientId", AADB2CProperties.PREFIX + ".credential.clientId");
            put(LEGACY_AAD_B2C_PREFIX + ".clientSecret", AADB2CProperties.PREFIX + ".credential.clientSecret");
            put(LEGACY_AAD_B2C_PREFIX + ".tenantId", AADB2CProperties.PREFIX + ".credential.tenantId");
            put(LEGACY_AAD_B2C_PREFIX + ".baseUri", AADB2CProperties.PREFIX + ".environment.authorityHost");
            put(LEGACY_AAD_B2C_PREFIX + ".tenant", AADB2CProperties.PREFIX + ".tenant");
            put(LEGACY_AAD_B2C_PREFIX + ".appIdUri", AADB2CProperties.PREFIX + ".appIdUri");
            put(LEGACY_AAD_B2C_PREFIX + ".jwtConnectTimeout", AADB2CProperties.PREFIX + ".jwtConnectTimeout");
            put(LEGACY_AAD_B2C_PREFIX + ".jwtReadTimeout", AADB2CProperties.PREFIX + ".jwtReadTimeout");
            put(LEGACY_AAD_B2C_PREFIX + ".jwtSizeLimit", AADB2CProperties.PREFIX + ".jwtSizeLimit");
            put(LEGACY_AAD_B2C_PREFIX + ".logoutSuccessUrl", AADB2CProperties.PREFIX + ".logoutSuccessUrl");
            put(LEGACY_AAD_B2C_PREFIX + ".authenticateAdditionalParameters", AADB2CProperties.PREFIX
                + ".authenticateAdditionalParameters");
            put(LEGACY_AAD_B2C_PREFIX + ".userNameAttributeName", AADB2CProperties.PREFIX + ".userNameAttributeName");
            put(LEGACY_AAD_B2C_PREFIX + ".allowTelemetry", AADB2CProperties.PREFIX + ".allowTelemetry");
            put(LEGACY_AAD_B2C_PREFIX + ".replyUrl", AADB2CProperties.PREFIX + ".replyUrl");
            put(LEGACY_AAD_B2C_PREFIX + ".loginFlow", AADB2CProperties.PREFIX + ".loginFlow");
            put(LEGACY_AAD_B2C_PREFIX + ".userFlows", AADB2CProperties.PREFIX + ".userFlows");
            put(LEGACY_AAD_B2C_PREFIX + ".authorizationClients", AADB2CProperties.PREFIX + ".authorizationClients");


            put(LEGACY_COSMOS_PREFIX + ".uri", CosmosProperties.PREFIX + ".uri");
            put(LEGACY_COSMOS_PREFIX + ".key", CosmosProperties.PREFIX + ".key");
            put(LEGACY_COSMOS_PREFIX + ".consistencyLevel", CosmosProperties.PREFIX + ".consistencyLevel");
            put(LEGACY_COSMOS_PREFIX + ".database", CosmosProperties.PREFIX + ".database");
            put(LEGACY_COSMOS_PREFIX + ".populateQueryMetrics", CosmosProperties.PREFIX + ".populateQueryMetrics");
            put(LEGACY_COSMOS_PREFIX + ".allowTelemetry", CosmosProperties.PREFIX + ".allowTelemetry");
            put(LEGACY_COSMOS_PREFIX + ".connectionMode", CosmosProperties.PREFIX + ".connectionMode");
            put(LEGACY_COSMOS_PREFIX + ".responseDiagnosticsProcessor", CosmosProperties.PREFIX
                + ".responseDiagnosticsProcessor");

            put(LEGACY_JMS_PREFIX + ".connectionString", AzureServiceBusJMSProperties.PREFIX + ".connectionString");
            put(LEGACY_JMS_PREFIX + ".topicClientId", AzureServiceBusJMSProperties.PREFIX + ".topicClientId");
            put(LEGACY_JMS_PREFIX + ".idleTimeout", AzureServiceBusJMSProperties.PREFIX + ".idleTimeout");
            put(LEGACY_JMS_PREFIX + ".pricingTier", AzureServiceBusJMSProperties.PREFIX + ".pricingTier");

            put(LEGACY_STORAGE_PREFIX + ".accountName", StorageProperties.PREFIX + ".accountName");
            put(LEGACY_STORAGE_PREFIX + ".blobEndpoint", StorageProperties.PREFIX + ".blobEndpoint");
            put(LEGACY_STORAGE_PREFIX + ".fileEndpoint", StorageProperties.PREFIX + ".fileEndpoint");
            put(LEGACY_STORAGE_PREFIX + ".accountKey", StorageProperties.PREFIX + ".accountKey");

            put(LEGACY_KEYVAULT_PREFIX + ".clientId", KeyVaultProperties.PREFIX + ".credential.clientId");
            put(LEGACY_KEYVAULT_PREFIX + ".clientKey", KeyVaultProperties.PREFIX + ".credential.clientSecret");
            put(LEGACY_KEYVAULT_PREFIX + ".tenantId", KeyVaultProperties.PREFIX + ".credential.tenantId");
            put(LEGACY_KEYVAULT_PREFIX + ".certificatePath", KeyVaultProperties.PREFIX + ".credential.certificatePath");
            put(LEGACY_KEYVAULT_PREFIX + ".certificatePassword", KeyVaultProperties.PREFIX + ".credential"
                + ".certificatePassword");
            put(LEGACY_KEYVAULT_PREFIX + ".order", KeyVaultProperties.PREFIX + ".order");
            put(LEGACY_KEYVAULT_PREFIX + ".uri", KeyVaultProperties.PREFIX + ".uri");
            put(LEGACY_KEYVAULT_PREFIX + ".enabled", KeyVaultProperties.PREFIX + ".enabled");
            put(LEGACY_KEYVAULT_PREFIX + ".secretKeys", KeyVaultProperties.PREFIX + ".secretKeys");
            put(LEGACY_KEYVAULT_PREFIX + ".refreshInterval", KeyVaultProperties.PREFIX + ".refreshInterval");
            put(LEGACY_KEYVAULT_PREFIX + ".allowTelemetry", KeyVaultProperties.PREFIX + ".allowTelemetry");
            put(LEGACY_KEYVAULT_PREFIX + ".caseSensitiveKeys", KeyVaultProperties.PREFIX + ".caseSensitiveKeys");

        }
    };

    private int order = DEFAULT_ORDER;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        Properties properties = new Properties();

        for (Map.Entry<String, String> e : LEGACY_TO_CURRENT.entrySet()) {
            Object value = Binder.get(environment)
                                 .bind(e.getKey(), Bindable.of(Object.class))
                                 .orElse(null);
            if (null != value) {
                properties.put(e.getValue(), value);
                LOGGER.info(e.getKey() + " property detected! Use the {} instead!", e.getValue());
            }
        }

        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource propertiesPropertySource =
                new PropertiesPropertySource(AzurePropertyEnvironmentPostProcessor.class.getName(), properties);
            environment.getPropertySources().addLast(propertiesPropertySource);
        }
    }
}
