// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.VERSION;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertConfigMapToAzureProperties;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.OAUTHBEARER_MECHANISM;
import static org.springframework.util.StringUtils.delimitedListToStringArray;

public final class AzureKafkaConfigurationUtils {

    public static final Map<String, String> KAFKA_OAUTH_CONFIGS;
    public static final String SECURITY_PROTOCOL_CONFIG_SASL = SASL_SSL.name();
    public static final String SASL_MECHANISM_OAUTH = OAUTHBEARER_MECHANISM;
    public static final String SASL_JAAS_CONFIG_OAUTH =
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    public static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH =
        KafkaOAuth2AuthenticateCallbackHandler.class.getName();

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKafkaConfigurationUtils.class);
    //TODO(yiliuTo): add reference doc here for the log.
    private static final String LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION = "Currently {} authentication mechanism is used, recommend to use Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication"
        + " which supports various Azure Identity credentials. To leverage the auto-configuration for OAuth2, you can just remove all your security, sasl and credential configurations of Kafka and Event Hubs."
        + " And configure Kafka bootstrap servers instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE = "Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication will be loaded to configure your Kafka security and sasl properties to support Azure Identity credentials.";
    private static final String LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE = "OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.";
    private static final String KAFKA_OAUTH2_USER_AGENT = "." + AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;

    static {
        Map<String, String> configs = new HashMap<>();
        configs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        configs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        configs.put(SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
        configs.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
        KAFKA_OAUTH_CONFIGS = Collections.unmodifiableMap(configs);
    }

    private AzureKafkaConfigurationUtils() {
    }

    /**
     * Configure Spring Cloud Azure user-agent for Kafka client. This method is idempotent to avoid configuring UA repeatedly.
     */
    public static synchronized void configureKafkaUserAgent() {
        Method dataMethod = ReflectionUtils.findMethod(ApiVersionsRequest.class, "data");
        if (dataMethod != null) {
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
            ApiVersionsRequestData apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(dataMethod, apiVersionsRequest);
            if (apiVersionsRequestData != null) {
                String clientSoftwareName = apiVersionsRequestData.clientSoftwareName();
                if (clientSoftwareName != null && !clientSoftwareName.contains(KAFKA_OAUTH2_USER_AGENT)) {
                    apiVersionsRequestData.setClientSoftwareName(apiVersionsRequestData.clientSoftwareName()
                        + KAFKA_OAUTH2_USER_AGENT);
                    apiVersionsRequestData.setClientSoftwareVersion(VERSION);
                }
            }
        }
    }

    /**
     * Detect whether we need to configure SASL/OAUTHBEARER properties for {@link KafkaProperties}. Will configure when
     * the security protocol is not configured, or it's set as SASL_SSL with sasl mechanism as null or OAUTHBEAR.
     *
     * @param sourceProperties the source kafka properties for admin/consumer/producer to detect
     * @return whether we need to configure with Spring Cloud Azure MSI support or not.
     */
    public static boolean needConfigureSaslOAuth(Map<String, Object> sourceProperties) {
        return meetAzureBootstrapServerConditions(sourceProperties) && meetSaslOAuthConditions(sourceProperties);
    }

    private static boolean meetSaslOAuthConditions(Map<String, Object> sourceProperties) {
        String securityProtocol = (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
        String saslMechanism = (String) sourceProperties.get(SASL_MECHANISM);
        if (securityProtocol == null || (SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol)
                && (saslMechanism == null || SASL_MECHANISM_OAUTH.equalsIgnoreCase(saslMechanism)))) {
            return true;
        }
        LOGGER.info(LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION, saslMechanism);
        return false;
    }

    private static boolean meetAzureBootstrapServerConditions(Map<String, Object> sourceProperties) {
        Object bootstrapServers = sourceProperties.get(BOOTSTRAP_SERVERS_CONFIG);
        List<String> serverList;
        if (bootstrapServers instanceof String) {
            serverList = Arrays.asList(delimitedListToStringArray((String) bootstrapServers, ","));
        } else if (bootstrapServers instanceof Iterable<?>) {
            serverList = new ArrayList<>();
            for (Object obj : (Iterable) bootstrapServers) {
                if (obj instanceof String) {
                    serverList.add((String) obj);
                } else {
                    LOGGER.debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
                    return false;
                }
            }
        } else {
            LOGGER.debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
            return false;
        }

        return serverList.size() == 1 && serverList.get(0).endsWith(":9093");
    }

    /**
     * Configure necessary OAuth properties for kafka properties.
     *
     * @param propertiesToConfigure kafka properties to be customized
     */
    public static void configureOAuthProperties(Map<String, String> propertiesToConfigure) {
        propertiesToConfigure.putAll(AzureKafkaConfigurationUtils.KAFKA_OAUTH_CONFIGS);
    }

    /**
     * Configure necessary OAuth properties for kafka properties and log for the changes.
     */
    public static void logConfigureOAuthProperties() {
        LOGGER.info(LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
        LOGGER.debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS,
            SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }

    /**
     * Build {@link AzurePasswordlessProperties} from Kafka custom properties and {@link AzureGlobalProperties}.
     *
     * @param kafkaProperties the kafka custom property map
     * @param azureGlobalProperties Azure global properties
     * @return a {@link AzurePasswordlessProperties}
     */
    public static AzurePasswordlessProperties buildAzureProperties(Map<String, Object> kafkaProperties,
                                                                   AzureGlobalProperties azureGlobalProperties) {
        AzurePasswordlessProperties azurePasswordlessProperties = new AzurePasswordlessProperties();
        copyPropertiesIgnoreNull(azureGlobalProperties.getProfile(), azurePasswordlessProperties.getProfile());
        copyPropertiesIgnoreNull(azureGlobalProperties.getCredential(), azurePasswordlessProperties.getCredential());
        convertConfigMapToAzureProperties(kafkaProperties, azurePasswordlessProperties);
        return azurePasswordlessProperties;
    }
}
