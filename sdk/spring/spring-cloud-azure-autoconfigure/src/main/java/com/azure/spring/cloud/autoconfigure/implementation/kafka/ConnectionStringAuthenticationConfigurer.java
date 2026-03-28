// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.slf4j.Logger;

import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;

/**
 * Configures connection string-based (SASL_PLAIN) authentication for Kafka using Event Hubs connection strings.
 * <p>
 * This configurer handles the deprecated connection string authentication method. It extracts the connection
 * string from the {@link ServiceConnectionStringProvider} and configures SASL_PLAIN authentication.
 * </p>
 *
 * @deprecated This authentication method is deprecated in favor of OAuth2 authentication.
 *             Use {@link OAuth2AuthenticationConfigurer} instead.
 */
@Deprecated
public class ConnectionStringAuthenticationConfigurer extends AbstractKafkaAuthenticationConfigurer {

    private static final String SASL_MECHANISM_PLAIN = "PLAIN";
    private static final String SASL_JAAS_CONFIG_TEMPLATE = 
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"%s\";%s";

    private final ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider;

    public ConnectionStringAuthenticationConfigurer(
            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider,
            Logger logger) {
        super(logger);
        this.connectionStringProvider = connectionStringProvider;
    }

    @Override
    protected boolean meetAuthenticationConditions(Map<String, Object> sourceProperties) {
        // Connection string authentication requires a connection string provider
        if (connectionStringProvider == null) {
            return false;
        }

        String securityProtocol = getSecurityProtocol(sourceProperties);
        String saslMechanism = getSaslMechanism(sourceProperties);

        // Connection string auth works with SASL_SSL protocol and PLAIN mechanism
        // or when these are not configured (we'll set them)
        boolean protocolMatch = meetSaslProtocolConditions(securityProtocol);
        boolean mechanismMatch = saslMechanism == null || SASL_MECHANISM_PLAIN.equalsIgnoreCase(saslMechanism);

        if (protocolMatch && mechanismMatch) {
            return true;
        }

        logger.debug("Connection string authentication cannot be applied. Security protocol: {}, SASL mechanism: {}",
            securityProtocol, saslMechanism);
        return false;
    }

    @Override
    public void configure(Map<String, Object> mergedProperties, Map<String, String> rawProperties) {
        String connectionString = connectionStringProvider.getConnectionString();
        
        // Configure SASL_PLAIN authentication with connection string
        rawProperties.put(SECURITY_PROTOCOL_CONFIG, SASL_SSL.name());
        rawProperties.put(SASL_MECHANISM, SASL_MECHANISM_PLAIN);
        rawProperties.put(SASL_JAAS_CONFIG, 
            String.format(SASL_JAAS_CONFIG_TEMPLATE, connectionString, System.getProperty("line.separator")));

        logConfiguration();
    }

    private void logConfiguration() {
        logger.warn("Autoconfiguration for Event Hubs for Kafka on connection string/Azure Resource Manager"
            + " has been deprecated, please migrate to OAuth2 authentication with Azure Identity credentials."
            + " To leverage the OAuth2 authentication, you can delete all your Event Hubs for Kafka credential "
            + "configurations, and configure Kafka bootstrap servers instead, which can be set as "
            + "spring.kafka.bootstrap-servers=EventHubsNamespacesFQDN:9093.");
        logger.debug("Connection string authentication property {} will be configured as {}.",
            SECURITY_PROTOCOL_CONFIG, SASL_SSL.name());
        logger.debug("Connection string authentication property {} will be configured as {}.",
            SASL_MECHANISM, SASL_MECHANISM_PLAIN);
        logger.debug("Connection string authentication property {} will be configured (value not logged for security).",
            SASL_JAAS_CONFIG);
    }
}
