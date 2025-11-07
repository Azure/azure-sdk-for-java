// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication.KafkaAuthenticationStrategy;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.authentication.KafkaOAuth2AuthenticationStrategy;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.VERSION;

/**
 * Abstract base class for Kafka properties BeanPostProcessors.
 * <p>
 * This class provides common functionality for configuring Kafka authentication for Azure Event Hubs
 * using different strategies. It uses the Strategy pattern to delegate authentication configuration
 * to pluggable {@link KafkaAuthenticationStrategy} implementations.
 * </p>
 * <p>
 * The processor intercepts Kafka properties beans during Spring bean initialization and applies
 * authentication configuration to producer, consumer, and admin properties based on the configured
 * strategy.
 * </p>
 * <p>
 * <b>Authentication Flow:</b>
 * <ol>
 *   <li>Bean post processor detects Kafka properties beans during initialization</li>
 *   <li>Retrieves AzureGlobalProperties for credential configuration</li>
 *   <li>For each set of properties (producer/consumer/admin):
 *     <ul>
 *       <li>Checks if authentication strategy should be applied via {@link KafkaAuthenticationStrategy#shouldApply}</li>
 *       <li>If applicable, applies authentication via {@link KafkaAuthenticationStrategy#applyAuthentication}</li>
 *       <li>Configures Kafka user agent for telemetry</li>
 *       <li>Clears Azure-specific properties from raw properties map</li>
 *     </ul>
 *   </li>
 * </ol>
 * </p>
 * <p>
 * By default, uses {@link KafkaOAuth2AuthenticationStrategy} for OAuth2/Microsoft Entra ID authentication.
 * Subclasses can provide alternative strategies via the constructor.
 * </p>
 *
 * @param <T> the type of Kafka properties bean to process (e.g., {@link KafkaProperties})
 * @see KafkaAuthenticationStrategy
 * @see KafkaOAuth2AuthenticationStrategy
 */
abstract class AbstractKafkaPropertiesBeanPostProcessor<T> implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractKafkaPropertiesBeanPostProcessor.class);
    protected ApplicationContext applicationContext;
    protected static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();

    private AzureGlobalProperties azureGlobalProperties;
    private final KafkaAuthenticationStrategy authenticationStrategy;

    /**
     * Constructor that initializes with the default OAuth2 authentication strategy.
     */
    protected AbstractKafkaPropertiesBeanPostProcessor() {
        this(new KafkaOAuth2AuthenticationStrategy());
    }

    /**
     * Constructor that allows injection of a custom authentication strategy.
     *
     * @param authenticationStrategy the authentication strategy to use
     */
    protected AbstractKafkaPropertiesBeanPostProcessor(KafkaAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (needsPostProcess(bean)) {
            ObjectProvider<AzureGlobalProperties> beanProvider = applicationContext.getBeanProvider(AzureGlobalProperties.class);
            azureGlobalProperties = beanProvider.getIfAvailable();
            if (azureGlobalProperties == null) {
                LOGGER.debug("Cannot find a bean of type AzureGlobalProperties, "
                    + "Spring Cloud Azure will skip performing authentication configuration on the {} bean.", beanName);
                return bean;
            }

            T properties = (T) bean;
            applyAuthentication(getMergedProducerProperties(properties), getRawProducerProperties(properties));
            applyAuthentication(getMergedConsumerProperties(properties), getRawConsumerProperties(properties));
            applyAuthentication(getMergedAdminProperties(properties), getRawAdminProperties(properties));
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Process Kafka Spring properties for any customized operations.
     * @param properties the Kafka Spring properties
     */
    protected void customizeProcess(T properties) {
    }


    protected void clearAzureProperties(Map<String, String> properties) {
        authenticationStrategy.clearAzureProperties(properties);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> invokeBuildKafkaProperties(KafkaProperties kafkaProperties, String buildMethodName) {
        try {
            try {
                Method buildPropertiesMethod = KafkaProperties.class.getDeclaredMethod(buildMethodName,
                    Class.forName("org.springframework.boot.ssl.SslBundles"));
                //noinspection unchecked
                return (Map<String, Object>) buildPropertiesMethod.invoke(kafkaProperties, (Object) null);
            } catch (NoSuchMethodException | ClassNotFoundException ignored) {

            }
            // The following logic is to be compatible with Spring Boot 3.0 and 3.1.
            try {
                //noinspection unchecked
                return (Map<String, Object>) KafkaProperties.class.getDeclaredMethod(buildMethodName).invoke(kafkaProperties);
            } catch (NoSuchMethodException ignored) {

            }
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
        throw new IllegalStateException("Failed to call " + buildMethodName + " method of KafkaProperties.");
    }

    /**
     * Applies authentication configuration to the Kafka properties.
     * This method uses the configured authentication strategy to:
     * 1. Apply authentication settings if the strategy determines it should
     * 2. Configure Kafka user agent
     * 3. Clear any Azure-specific properties from the raw properties map
     *
     * @param mergedProperties the merged Kafka properties which can contain Azure properties
     * @param rawPropertiesMap the raw Kafka properties Map to configure authentication to
     */
    private void applyAuthentication(Map<String, Object> mergedProperties, Map<String, String> rawPropertiesMap) {
        if (authenticationStrategy.shouldApply(mergedProperties)) {
            authenticationStrategy.applyAuthentication(mergedProperties, rawPropertiesMap, azureGlobalProperties);
            configureKafkaUserAgent();
        }
        authenticationStrategy.clearAzureProperties(rawPropertiesMap);
    }

    /**
     * Check if SASL OAuth authentication should be configured.
     * This method delegates to the authentication strategy.
     *
     * @param sourceProperties the source kafka properties for admin/consumer/producer to detect
     * @return whether OAuth authentication should be configured
     */
    boolean needConfigureSaslOAuth(Map<String, Object> sourceProperties) {
        return authenticationStrategy.shouldApply(sourceProperties);
    }

    /**
     * Configure Spring Cloud Azure user-agent for Kafka client. This method is idempotent to avoid configuring UA repeatedly.
     */
    static synchronized void configureKafkaUserAgent() {
        ApiVersionsRequestData apiVersionsRequestData = null;
        // In kafka-clients 3.9.1 and later the ApiVersionsRequestData is duplicated for each new ApiVersionsRequest.
        // Need to mutate the shared field that gets duplicated.
        Field defaultDataField = ReflectionUtils.findField(ApiVersionsRequest.Builder.class, "DEFAULT_DATA");
        if (defaultDataField != null) {
            ReflectionUtils.makeAccessible(defaultDataField);
            apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.getField(defaultDataField, null);
        } else {
            Method dataMethod = ReflectionUtils.findMethod(ApiVersionsRequest.class, "data");
            if (dataMethod != null) {
                ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
                apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(dataMethod,
                    apiVersionsRequest);
            }
        }

        if (apiVersionsRequestData != null) {
            String clientSoftwareName = apiVersionsRequestData.clientSoftwareName();
            if (clientSoftwareName != null && !clientSoftwareName.contains(
                AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH)) {
                apiVersionsRequestData.setClientSoftwareName(
                    apiVersionsRequestData.clientSoftwareName() + AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH);
                apiVersionsRequestData.setClientSoftwareVersion(VERSION);
            }
        }
    }

}
