// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;

/**
 * Abstract base class for Kafka authentication configurers that provides common functionality
 * for checking bootstrap servers and SASL configuration conditions.
 * <p>
 * This class implements the Template Method pattern, where subclasses provide specific
 * authentication mechanism implementations while common validation logic is shared.
 * </p>
 */
abstract class AbstractKafkaAuthenticationConfigurer implements KafkaAuthenticationConfigurer {

    protected final Logger logger;

    protected AbstractKafkaAuthenticationConfigurer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean canConfigure(Map<String, Object> mergedProperties) {
        return meetBootstrapServerConditions(mergedProperties)
            && meetAuthenticationConditions(mergedProperties);
    }

    /**
     * Checks if the bootstrap server configuration meets the requirements for Azure Event Hubs.
     * The bootstrap server must point to an Event Hubs namespace (*.servicebus.windows.net:9093).
     *
     * @param sourceProperties the Kafka properties to check
     * @return true if bootstrap server configuration is valid for Azure Event Hubs
     */
    protected boolean meetBootstrapServerConditions(Map<String, Object> sourceProperties) {
        Object bootstrapServers = sourceProperties.get(BOOTSTRAP_SERVERS_CONFIG);
        List<String> serverList = extractBootstrapServerList(bootstrapServers);

        if (serverList == null) {
            logger.debug("Kafka bootstrap server configuration doesn't meet Azure Event Hubs requirements.");
            return false;
        }

        return serverList.size() == 1 && serverList.get(0).endsWith(":9093");
    }

    /**
     * Extracts bootstrap server list from the configuration value.
     * Handles both String and Iterable configurations.
     *
     * @param bootstrapServers the bootstrap servers configuration value
     * @return list of server addresses, or null if invalid format
     */
    protected List<String> extractBootstrapServerList(Object bootstrapServers) {
        if (bootstrapServers instanceof String) {
            return Arrays.asList(StringUtils.delimitedListToStringArray((String) bootstrapServers, ","));
        } else if (bootstrapServers instanceof Iterable<?>) {
            List<String> serverList = new java.util.ArrayList<>();
            for (Object obj : (Iterable<?>) bootstrapServers) {
                if (obj instanceof String) {
                    serverList.add((String) obj);
                } else {
                    return null;
                }
            }
            return serverList;
        }
        return null;
    }

    /**
     * Checks if the SASL protocol is set to SASL_SSL or not configured.
     *
     * @param securityProtocol the security protocol configuration value
     * @return true if security protocol is compatible
     */
    protected boolean meetSaslProtocolConditions(String securityProtocol) {
        return securityProtocol == null 
            || AbstractKafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol);
    }

    /**
     * Template method for subclasses to implement specific authentication mechanism checks.
     * This method should verify that the Kafka properties are compatible with the specific
     * authentication type (OAuth2, connection string, etc.).
     *
     * @param sourceProperties the Kafka properties to check
     * @return true if this authentication mechanism can be applied
     */
    protected abstract boolean meetAuthenticationConditions(Map<String, Object> sourceProperties);

    /**
     * Gets the security protocol value from properties.
     *
     * @param sourceProperties the Kafka properties
     * @return the security protocol value or null
     */
    protected String getSecurityProtocol(Map<String, Object> sourceProperties) {
        return (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
    }

    /**
     * Gets the SASL mechanism value from properties.
     *
     * @param sourceProperties the Kafka properties
     * @return the SASL mechanism value or null
     */
    protected String getSaslMechanism(Map<String, Object> sourceProperties) {
        return (String) sourceProperties.get(SASL_MECHANISM);
    }

    /**
     * Gets the SASL JAAS config value from properties.
     *
     * @param sourceProperties the Kafka properties
     * @return the SASL JAAS config value or null
     */
    protected String getJaasConfig(Map<String, Object> sourceProperties) {
        return (String) sourceProperties.get(SASL_JAAS_CONFIG);
    }
}
