// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.jaas.Jaas;
import com.azure.spring.cloud.service.implementation.jaas.JaasResolver;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import com.azure.spring.cloud.service.implementation.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.VERSION;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.OAUTHBEARER_MECHANISM;
import static org.springframework.util.StringUtils.delimitedListToStringArray;

abstract class AbstractKafkaPropertiesBeanPostProcessor<T> implements BeanPostProcessor {

    static final String SECURITY_PROTOCOL_CONFIG_SASL = SASL_SSL.name();
    static final String SASL_MECHANISM_OAUTH = OAUTHBEARER_MECHANISM;
    static final String AZURE_CONFIGURED_JAAS_OPTIONS_KEY = "azure.configured";
    static final String AZURE_CONFIGURED_JAAS_OPTIONS_VALUE = "true";
    static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH =
        KafkaOAuth2AuthenticateCallbackHandler.class.getName();
    protected static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    private static final Map<String, String> KAFKA_OAUTH_CONFIGS;
    private static final String LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE = "OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE = "Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication will be loaded to configure your Kafka security and sasl properties to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION = "Currently {} authentication mechanism is used, recommend to use Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication"
        + " which supports various Azure Identity credentials. To leverage the auto-configuration for OAuth2, you can just remove all your security, sasl and credential configurations of Kafka and Event Hubs."
        + " And configure Kafka bootstrap servers instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.";

    static {
        Map<String, String> configs = new HashMap<>();
        configs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        configs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        configs.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
        KAFKA_OAUTH_CONFIGS = Collections.unmodifiableMap(configs);
    }

    private final AzureGlobalProperties azureGlobalProperties;

    AbstractKafkaPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (needsPostProcess(bean)) {
            T properties = (T) bean;

            replaceAzurePropertiesWithJaas(getMergedProducerProperties(properties), getRawProducerProperties(properties));
            replaceAzurePropertiesWithJaas(getMergedConsumerProperties(properties), getRawConsumerProperties(properties));
            replaceAzurePropertiesWithJaas(getMergedAdminProperties(properties), getRawAdminProperties(properties));
            customizeProcess(properties);
        }
        return bean;
    }

    /**
     * Create a map of the merged Kafka producer properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka producer properties
     */
    protected abstract Map<String, Object> getMergedProducerProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores producer-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing producer-specific properties
     */
    protected abstract Map<String, String> getRawProducerProperties(T properties);

    /**
     * Create a map of the merged Kafka consumer properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka consumer properties
     */
    protected abstract Map<String, Object> getMergedConsumerProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores consumer-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing consumer-specific properties
     */
    protected abstract Map<String, String> getRawConsumerProperties(T properties);

    /**
     * Create a map of the merged Kafka admin properties from the Kafka Spring properties.
     * @param properties the Kafka Spring properties
     * @return a Map containing all Kafka admin properties
     */
    protected abstract Map<String, Object> getMergedAdminProperties(T properties);

    /**
     * Get the raw {@link Map} object from the Kafka Spring properties that stores admin-specific properties.
     * @param properties the Kafka Spring properties
     * @return the map from Kafka properties storing admin-specific properties
     */
    protected abstract Map<String, String> getRawAdminProperties(T properties);

    protected abstract boolean needsPostProcess(Object bean);

    protected abstract Logger getLogger();

    /**
     * Process Kafka Spring properties for any customized operations.
     * @param properties the Kafka Spring properties
     */
    protected void customizeProcess(T properties) {
    }


    protected void clearAzureProperties(Map<String, String> properties) {
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(properties::remove);
    }

    /**
     * This method executes two operations:
     * <p>
     * 1. When this configuration meets Azure Kafka passwordless startup requirements, convert all Azure properties
     * in Kafka to {@link Jaas}, and configure the JAAS configuration back to Kafka.
     * </p>
     * <p>
     * 2. Clear any Azure properties in Kafka properties.
     * </p>
     * @param mergedProperties the merged Kafka properties which can contain Azure properties to resolve JAAS from
     * @param rawPropertiesMap the raw Kafka properties Map to configure JAAS to and remove Azure Properties from
     */
    private void replaceAzurePropertiesWithJaas(Map<String, Object> mergedProperties, Map<String, String> rawPropertiesMap) {
        resolveJaasForAzure(mergedProperties)
            .ifPresent(jaas -> {
                configJaasToKafkaRawProperties(jaas, rawPropertiesMap);
                logConfigureOAuthProperties();
                configureKafkaUserAgent();
            });
        clearAzureProperties(rawPropertiesMap);
    }

    private Optional<Jaas> resolveJaasForAzure(Map<String, Object> mergedProperties) {
        if (needConfigureSaslOAuth(mergedProperties)) {
            JaasResolver resolver = new JaasResolver();
            Jaas jaas = resolver.resolve((String) mergedProperties.get(SASL_JAAS_CONFIG))
                .orElse(new Jaas(OAuthBearerLoginModule.class.getName()));
            setAzurePropertiesToJaasOptionsIfAbsent(azureGlobalProperties, jaas);
            setKafkaPropertiesToJaasOptions(mergedProperties, jaas);
            jaas.getOptions().put(AZURE_CONFIGURED_JAAS_OPTIONS_KEY, AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
            return Optional.of(jaas);
        } else {
            return Optional.empty();
        }
    }

    private void configJaasToKafkaRawProperties(Jaas jaas, Map<String, String> rawPropertiesMap) {
        rawPropertiesMap.putAll(KAFKA_OAUTH_CONFIGS);
        rawPropertiesMap.put(SASL_JAAS_CONFIG, jaas.toString());
    }

    /**
     * Configure necessary OAuth properties for kafka properties and log for the changes.
     */
    private void logConfigureOAuthProperties() {
        getLogger().info(LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_JAAS_CONFIG, "***the value involves credentials and will not be logged***");
        getLogger().debug(LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS,
            SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }


    private void setKafkaPropertiesToJaasOptions(Map<String, ?> properties, Jaas jaas) {
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(k -> PROPERTY_MAPPER.from(properties.get(k)).to(p -> jaas.getOptions().put(k, (String) p)));
    }

    private void setAzurePropertiesToJaasOptionsIfAbsent(AzureProperties azureProperties, Jaas jaas) {
        convertAzurePropertiesToMap(azureProperties)
            .forEach((k, v) -> jaas.getOptions().putIfAbsent(k, v));
    }

    private Map<String, String> convertAzurePropertiesToMap(AzureProperties properties) {
        Map<String, String> configs = new HashMap<>();
        for (AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping m : AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.values()) {
            PROPERTY_MAPPER.from(m.getter().apply(properties)).to(p -> configs.put(m.propertyKey(), p));
        }
        return configs;
    }

    /**
     * Configure Spring Cloud Azure user-agent for Kafka client. This method is idempotent to avoid configuring UA repeatedly.
     */
    static synchronized void configureKafkaUserAgent() {
        Method dataMethod = ReflectionUtils.findMethod(ApiVersionsRequest.class, "data");
        if (dataMethod != null) {
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
            ApiVersionsRequestData apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(dataMethod, apiVersionsRequest);
            if (apiVersionsRequestData != null) {
                String clientSoftwareName = apiVersionsRequestData.clientSoftwareName();
                if (clientSoftwareName != null && !clientSoftwareName.contains(AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH)) {
                    apiVersionsRequestData.setClientSoftwareName(apiVersionsRequestData.clientSoftwareName()
                        + AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH);
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
    boolean needConfigureSaslOAuth(Map<String, Object> sourceProperties) {
        return meetAzureBootstrapServerConditions(sourceProperties) && meetSaslOAuthConditions(sourceProperties);
    }

    private boolean meetSaslOAuthConditions(Map<String, Object> sourceProperties) {
        String securityProtocol = (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
        String saslMechanism = (String) sourceProperties.get(SASL_MECHANISM);
        String jaasConfig = (String) sourceProperties.get(SASL_JAAS_CONFIG);
        if (meetSaslProtocolConditions(securityProtocol) && meetSaslOAuth2MechanismConditions(saslMechanism)
            && meetJaasConditions(jaasConfig)) {
            return true;
        }
        getLogger().info(LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION, saslMechanism);
        return false;
    }

    private boolean meetSaslProtocolConditions(String securityProtocol) {
        return securityProtocol == null || SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol);
    }

    private boolean meetSaslOAuth2MechanismConditions(String saslMechanism) {
        return saslMechanism == null || SASL_MECHANISM_OAUTH.equalsIgnoreCase(saslMechanism);
    }
    private boolean meetJaasConditions(String jaasConfig) {
        if (jaasConfig == null) {
            return true;
        }
        JaasResolver resolver = new JaasResolver();
        return resolver.resolve(jaasConfig)
                .map(jaas -> AZURE_CONFIGURED_JAAS_OPTIONS_VALUE.equals(
                        jaas.getOptions().get(AZURE_CONFIGURED_JAAS_OPTIONS_KEY)))
                .orElse(false);
    }

    private boolean meetAzureBootstrapServerConditions(Map<String, Object> sourceProperties) {
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
                    getLogger().debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
                    return false;
                }
            }
        } else {
            getLogger().debug("Kafka bootstrap server configuration doesn't meet passwordless requirements.");
            return false;
        }

        return serverList.size() == 1 && serverList.get(0).endsWith(":9093");
    }

}
