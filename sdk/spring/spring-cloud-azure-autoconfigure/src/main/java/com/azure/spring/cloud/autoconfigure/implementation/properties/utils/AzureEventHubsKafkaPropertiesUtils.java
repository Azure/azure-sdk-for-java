// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.properties.utils;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

/**
 * Utils class for {@link KafkaProperties} String constants.
 */
public final class AzureEventHubsKafkaPropertiesUtils {

    private AzureEventHubsKafkaPropertiesUtils() {

    }

    public static final String SECURITY_PROTOCOL_CONFIG_SASL = "SASL_SSL";
    public static final String SASL_MECHANISM_OAUTH = "OAUTHBEARER";
    public static final String SASL_JAAS_CONFIG_OAUTH = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    public static final String SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH = "com.azure.spring.cloud.service.kafka.KafkaOAuth2AuthenticateCallbackHandler";
}
