// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.kafka.properties.AzureEventHubsKafkaProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureEventHubsKafkaPropertiesUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureEventHubsKafkaPropertiesUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureEventHubsKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureEventHubsKafkaPropertiesUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;

/**
 * {@link BeanPostProcessor} to apply {@link AzureEventHubsKafkaProperties} and Kafka OAuth properties
 * to {@link KafkaProperties}.
 */
class KafkaPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final AzureEventHubsKafkaProperties azureEventHubsKafkaProperties;

    KafkaPropertiesBeanPostProcessor(AzureEventHubsKafkaProperties azureEventHubsKafkaProperties) {
        this.azureEventHubsKafkaProperties = azureEventHubsKafkaProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof KafkaProperties) {
            KafkaProperties kafkaProperties = (KafkaProperties) bean;
            //only map to common properties instead of upper admin/consumer/producer properties given these are Spring Cloud Azure
            //self-defined properties
            mapAzurePropertiesToKafkaCommonProperties(kafkaProperties);
            
            configureOAuthPropertiesIfNeed(kafkaProperties.buildAdminProperties(), kafkaProperties.getAdmin().getProperties());
            configureOAuthPropertiesIfNeed(kafkaProperties.buildConsumerProperties(), kafkaProperties.getConsumer().getProperties());
            configureOAuthPropertiesIfNeed(kafkaProperties.buildProducerProperties(), kafkaProperties.getProducer().getProperties());
        }
        return bean;
    }

    private void configureOAuthPropertiesIfNeed(Map<String, Object> sourceProperties,
                                                Map<String, String> propertiesToConfigure) {
        if (ifSaslOAuthNeedConfigure(sourceProperties)) {
            propertiesToConfigure.put(SECURITY_PROTOCOL_CONFIG,
                    SECURITY_PROTOCOL_CONFIG_SASL);
            propertiesToConfigure.put(SASL_MECHANISM,
                    SASL_MECHANISM_OAUTH);
            propertiesToConfigure.put(SASL_JAAS_CONFIG,
                    SASL_JAAS_CONFIG_OAUTH);
            propertiesToConfigure.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS,
                    SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
        }
    }

    /**
     * Detect whether we need to configure SASL/OAUTHBEARER properties for {@link KafkaProperties}. Will configure
     * when the security protocol is not configured, or it's set as SASL_SSL with sasl mechanism as null or OAUTHBEAR.
     *
     * @param sourceProperties the source kafka properties for admin/consumer/producer to detect
     * @return whether we need to configure with Spring Cloud Azure MSI support or not.
     */
    private boolean ifSaslOAuthNeedConfigure(Map<String, Object> sourceProperties) {
        String securityProtocol = (String) sourceProperties.get(SECURITY_PROTOCOL_CONFIG);
        String saslMechanism = (String) sourceProperties.get(SASL_MECHANISM);
        return securityProtocol == null || (SECURITY_PROTOCOL_CONFIG_SASL.equalsIgnoreCase(securityProtocol)
                && (saslMechanism == null || SASL_MECHANISM_OAUTH.equalsIgnoreCase(saslMechanism)));
    }

    private void mapAzurePropertiesToKafkaCommonProperties(KafkaProperties kafkaProperties) {
        PropertyMapper map = new PropertyMapper();
        map.from(azureEventHubsKafkaProperties.getCredential().getClientId())
                .to(id -> kafkaProperties.getProperties().put(AzureKafkaConfigs.CLIENT_ID_CONFIG, id));
        map.from(azureEventHubsKafkaProperties.getProfile().getTenantId())
                .to(id -> kafkaProperties.getProperties().put(AzureKafkaConfigs.TENANT_ID_CONFIG, id));
        map.from(azureEventHubsKafkaProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint())
                .to(host -> kafkaProperties.getProperties().put(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG, host));
    }
}
