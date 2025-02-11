// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.util;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * Util class for passwordless properties enhancement.
 */
public final class SpringPasswordlessPropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPasswordlessPropertiesUtils.class);

    private SpringPasswordlessPropertiesUtils() {

    }

    /**
     * Enhance the {@link PasswordlessProperties} implementation into the {@link Properties}.
     * @param passwordlessPropertiesPrefix the prefix for the {@link PasswordlessProperties}.
     * @param passwordlessProperties the {@link PasswordlessProperties} implementation.
     * @param properties the {@link Properties} {@link Properties}.
     */
    public static void enhancePasswordlessProperties(String passwordlessPropertiesPrefix,
                                                     PasswordlessProperties passwordlessProperties,
                                                     Properties properties) {
        if (!passwordlessProperties.isPasswordlessEnabled()) {
            LOGGER.debug("Feature passwordless authentication is not enabled({}.passwordless-enabled=false), "
                + "skip enhancing properties.", passwordlessPropertiesPrefix);
            return;
        }

        String tokenCredentialBeanName = passwordlessProperties.getCredential().getTokenCredentialBeanName();
        if (StringUtils.hasText(tokenCredentialBeanName)) {
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(properties, SpringTokenCredentialProvider.class.getName());
        }
    }

    /**
     * Enhance the {@link PasswordlessProperties} implementation into the Map.
     * @param passwordlessPropertiesPrefix the prefix for the {@link PasswordlessProperties}.
     * @param passwordlessProperties the {@link PasswordlessProperties} implementation.
     * @param result the Map.
     */
    public static void enhancePasswordlessProperties(String passwordlessPropertiesPrefix,
                                                     PasswordlessProperties passwordlessProperties,
                                                     Map<String, String> result) {
        Properties properties = passwordlessProperties.toPasswordlessProperties();
        result.forEach(properties::setProperty);
        enhancePasswordlessProperties(passwordlessPropertiesPrefix, passwordlessProperties, properties);
        properties.forEach((key, value) -> result.put((String) key, (String) value));
    }
}
