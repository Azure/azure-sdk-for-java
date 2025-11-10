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
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * Abstract base class for Kafka properties bean post-processors that configure Azure authentication.
 *
 * <p>This class provides a framework for automatically configuring authentication properties for various
 * Kafka client types (producers, consumers, and admins). It implements a strategy pattern to allow different
 * authentication methods to be plugged in.</p>
 *
 * <h2>Architecture</h2>
 * <p>The post-processor works in three phases:</p>
 * <ol>
 *   <li><strong>Detection</strong>: Identifies beans that need Kafka authentication configuration</li>
 *   <li><strong>Configuration</strong>: Applies authentication settings using a {@link KafkaAuthenticationConfigurer}</li>
 *   <li><strong>Cleanup</strong>: Removes Azure-specific properties that shouldn't be passed to Kafka clients</li>
 * </ol>
 *
 * <h2>Supported Client Types</h2>
 * <p>This processor handles authentication for:</p>
 * <ul>
 *   <li>Kafka Producers</li>
 *   <li>Kafka Consumers</li>
 *   <li>Kafka Admin Clients</li>
 * </ul>
 *
 * <h2>Subclass Implementation</h2>
 * <p>Subclasses must implement methods to:</p>
 * <ul>
 *   <li>Extract merged properties (all configuration sources combined)</li>
 *   <li>Access raw property maps (for modification)</li>
 *   <li>Determine which beans need processing</li>
 * </ul>
 *
 * <h2>Authentication Configuration</h2>
 * <p>The class uses {@link KafkaAuthenticationConfigurer} instances to apply authentication settings.
 * The default implementation uses {@link OAuth2AuthenticationConfigurer} for OAuth2/OAUTHBEARER authentication.</p>
 *
 * @param <T> the type of Kafka properties bean to process
 * @see KafkaAuthenticationConfigurer
 * @see OAuth2AuthenticationConfigurer
 * @see KafkaPropertiesBeanPostProcessor
 * @see KafkaBinderConfigurationPropertiesBeanPostProcessor
 */
abstract class AbstractKafkaPropertiesBeanPostProcessor<T> implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractKafkaPropertiesBeanPostProcessor.class);
    static final String SECURITY_PROTOCOL_CONFIG_SASL = SASL_SSL.name();
    static final String SASL_MECHANISM_OAUTH = OAUTHBEARER_MECHANISM;
    static final String AZURE_CONFIGURED_JAAS_OPTIONS_KEY = "azure.configured";
    static final String AZURE_CONFIGURED_JAAS_OPTIONS_VALUE = "true";
    static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH =
        KafkaOAuth2AuthenticateCallbackHandler.class.getName();
    protected ApplicationContext applicationContext;
    protected static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    private static final Map<String, String> KAFKA_OAUTH_CONFIGS;
    private static final String LOG_OAUTH_DETAILED_PROPERTY_CONFIGURE = "OAUTHBEARER authentication property {} will be configured as {} to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_CONFIGURE = "Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication will be loaded to configure your Kafka security and sasl properties to support Azure Identity credentials.";
    private static final String LOG_OAUTH_AUTOCONFIGURATION_RECOMMENDATION = "Currently {} authentication mechanism is used, recommend to use Spring Cloud Azure auto-configuration for Kafka OAUTHBEARER authentication"
        + " which supports various Azure Identity credentials. To leverage the auto-configuration for OAuth2, you can just remove all your security, sasl and credential configurations of Kafka and Event Hubs."
        + " And configure Kafka bootstrap servers instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.";

    static {
        KAFKA_OAUTH_CONFIGS = Map.of(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL, SASL_MECHANISM,
            SASL_MECHANISM_OAUTH, SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
    }

    private AzureGlobalProperties azureGlobalProperties;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (needsPostProcess(bean)) {
            ObjectProvider<AzureGlobalProperties> beanProvider = applicationContext.getBeanProvider(AzureGlobalProperties.class);
            azureGlobalProperties = beanProvider.getIfAvailable();
            if (azureGlobalProperties == null) {
                LOGGER.debug("Cannot find a bean of type AzureGlobalProperties, "
                    + "Spring Cloud Azure will skip performing JAAS enhancements on the {} bean.", beanName);
                return bean;
            }

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
        AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.getPropertyKeys()
            .forEach(properties::remove);
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
        // Use strategy pattern to configure authentication
        KafkaAuthenticationConfigurer configurer = createAuthenticationConfigurer();
        if (configurer.canConfigure(mergedProperties)) {
            configurer.configure(mergedProperties, rawPropertiesMap);
            configureKafkaUserAgent();
        }
        clearAzureProperties(rawPropertiesMap);
    }

    /**
     * Creates the appropriate authentication configurer based on available Azure properties.
     * Currently supports OAuth2 (OAUTHBEARER) authentication with Azure Identity.
     *
     * @return the authentication configurer to use
     */
    private KafkaAuthenticationConfigurer createAuthenticationConfigurer() {
        return new OAuth2AuthenticationConfigurer(azureGlobalProperties, getLogger());
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
