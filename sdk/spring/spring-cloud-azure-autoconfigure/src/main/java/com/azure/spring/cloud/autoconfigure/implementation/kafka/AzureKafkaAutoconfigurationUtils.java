// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import com.azure.spring.cloud.service.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.convertConfigMapToAzureProperties;
import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.OAUTHBEARER_MECHANISM;

public final class AzureKafkaAutoconfigurationUtils {

    public static final Map<String, String> KAFKA_OAUTH_CONFIGS;
    public static final String SECURITY_PROTOCOL_CONFIG_SASL = SASL_SSL.name();
    public static final String SASL_MECHANISM_OAUTH = OAUTHBEARER_MECHANISM;
    public static final String SASL_JAAS_CONFIG_OAUTH =
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    public static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH =
        KafkaOAuth2AuthenticateCallbackHandler.class.getName();

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKafkaAutoconfigurationUtils.class);
    private static final String LOG_PROPERTIES_CONFIGURE = "Property %s will be configured as %s.";

    static {
        KAFKA_OAUTH_CONFIGS = new HashMap<>();
        KAFKA_OAUTH_CONFIGS.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        KAFKA_OAUTH_CONFIGS.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        KAFKA_OAUTH_CONFIGS.put(SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
        KAFKA_OAUTH_CONFIGS.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }

    private AzureKafkaAutoconfigurationUtils() {
    }

    /**
     * Detect whether we need to configure SASL/OAUTHBEARER properties for {@link KafkaProperties}. Will configure when
     * the security protocol is not configured, or it's set as SASL_SSL with sasl mechanism as null or OAUTHBEAR.
     *
     * @param sourceProperties the source kafka properties for admin/consumer/producer to detect
     * @return whether we need to configure with Spring Cloud Azure MSI support or not.
     */
    public static boolean needConfigureSaslOAuth(Map<String, Object> sourceProperties) {
        String securityProtocol = (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
        String saslMechanism = (String) sourceProperties.get(SASL_MECHANISM);
        return securityProtocol == null || (SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol)
            && (saslMechanism == null || SASL_MECHANISM_OAUTH.equalsIgnoreCase(saslMechanism)));
    }

    /**
     * Configure necessary OAuth properties for kafka properties and log for the changes.
     *
     * @param sourceProperties raw kafka properties
     * @param propertiesToConfigure kafka properties to be customized
     */
    public static void configureOAuthProperties(Map<String, String> propertiesToConfigure) {
        propertiesToConfigure.putAll(AzureKafkaAutoconfigurationUtils.KAFKA_OAUTH_CONFIGS);
        LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
        LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS,
            SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }

    /**
     * Build {@link AzureThirdPartyServiceProperties} from Kafka custom properties and {@link AzureGlobalProperties}.
     *
     * @param kafkaProperties the kafka custom property map
     * @param azureGlobalProperties Azure global properties
     * @return a {@link AzureThirdPartyServiceProperties}
     */
    public static AzureThirdPartyServiceProperties buildAzureProperties(Map<String, Object> kafkaProperties,
                                                                        AzureGlobalProperties azureGlobalProperties) {
        AzureThirdPartyServiceProperties azureKafkaProperties = new AzureThirdPartyServiceProperties();
        copyPropertiesIgnoreNull(azureGlobalProperties.getProfile(), azureKafkaProperties.getProfile());
        copyPropertiesIgnoreNull(azureGlobalProperties.getCredential(), azureKafkaProperties.getCredential());
        convertConfigMapToAzureProperties(kafkaProperties, azureKafkaProperties);
        return azureKafkaProperties;
    }
}
