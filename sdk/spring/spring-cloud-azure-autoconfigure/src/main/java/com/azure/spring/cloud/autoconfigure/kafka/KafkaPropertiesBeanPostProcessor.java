// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.kafka.properties.AzureEventHubsKafkaProperties;
import com.azure.spring.cloud.service.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;

/**
 * {@link BeanPostProcessor} to apply {@link AzureEventHubsKafkaProperties} and Kafka OAuth properties
 * to {@link KafkaProperties}.
 */
class KafkaPropertiesBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPropertiesBeanPostProcessor.class);
    private static final String LOG_PROPERTIES_CONFIGURE = "Property %s will be configured as %s.";
    static final String SECURITY_PROTOCOL_CONFIG_SASL = "SASL_SSL";
    static final String SASL_MECHANISM_OAUTH = "OAUTHBEARER";
    static final String SASL_JAAS_CONFIG_OAUTH = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH = KafkaOAuth2AuthenticateCallbackHandler.class.getName();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof KafkaProperties) {
            KafkaProperties kafkaProperties = (KafkaProperties) bean;
            configureOAuthPropertiesIfNeed(kafkaProperties.buildAdminProperties(), kafkaProperties.getAdmin().getProperties());
            configureOAuthPropertiesIfNeed(kafkaProperties.buildConsumerProperties(), kafkaProperties.getConsumer().getProperties());
            configureOAuthPropertiesIfNeed(kafkaProperties.buildProducerProperties(), kafkaProperties.getProducer().getProperties());
        }
        return bean;
    }

    private void configureOAuthPropertiesIfNeed(Map<String, Object> sourceProperties,
                                                Map<String, String> propertiesToConfigure) {
        if (ifSaslOAuthNeedConfigure(sourceProperties)) {
            propertiesToConfigure.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
            LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
            propertiesToConfigure.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
            LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_MECHANISM, SASL_MECHANISM_OAUTH);
            propertiesToConfigure.put(SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
            LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
            propertiesToConfigure.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
            LOGGER.debug(LOG_PROPERTIES_CONFIGURE, SASL_LOGIN_CALLBACK_HANDLER_CLASS, SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH);
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

}
