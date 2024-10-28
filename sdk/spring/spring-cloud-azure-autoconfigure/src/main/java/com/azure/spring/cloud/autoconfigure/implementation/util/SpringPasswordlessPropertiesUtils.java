// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.util;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

import static com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider.PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Util class for passwordless properties enhancement.
 */
public final class SpringPasswordlessPropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPasswordlessPropertiesUtils.class);

    private SpringPasswordlessPropertiesUtils() {

    }

    /**
     * Enhance the {@link PasswordlessProperties} implementation into the {@link Properties}.
     * @param applicationContext the application context.
     * @param passwordlessPropertiesPrefix the prefix for the {@link PasswordlessProperties}.
     * @param passwordlessProperties the {@link PasswordlessProperties} implementation.
     * @param properties the {@link Properties} {@link Properties}.
     */
    public static void enhancePasswordlessProperties(GenericApplicationContext applicationContext,
                                                     String passwordlessPropertiesPrefix,
                                                     PasswordlessProperties passwordlessProperties,
                                                     Properties properties) {
        if (!passwordlessProperties.isPasswordlessEnabled()) {
            if (!passwordlessProperties.isPasswordlessEnabled()) {
                LOGGER.debug("Feature passwordless authentication is not enabled({}.passwordless-enabled=false), "
                    + "skip enhancing properties.", passwordlessPropertiesPrefix);
                return;
            }
        }

        String tokenCredentialBeanName = passwordlessProperties.getCredential().getTokenCredentialBeanName();
        if (StringUtils.hasText(tokenCredentialBeanName)) {
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
        } else {
            TokenCredentialProvider tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(properties));
            TokenCredential tokenCredential = tokenCredentialProvider.get();

            tokenCredentialBeanName = PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME + "." + passwordlessPropertiesPrefix;
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
            applicationContext.registerBean(tokenCredentialBeanName, TokenCredential.class, () -> tokenCredential);
        }

        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(properties, SpringTokenCredentialProvider.class.getName());
        AuthProperty.AUTHORITY_HOST.setProperty(properties, passwordlessProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

    }

    /**
     * Enhance the {@link PasswordlessProperties} implementation into the Map.
     * @param applicationContext the application context.
     * @param passwordlessPropertiesPrefix the prefix for the {@link PasswordlessProperties}.
     * @param passwordlessProperties the {@link PasswordlessProperties} implementation.
     * @param result the Map.
     */
    public static void enhancePasswordlessProperties(GenericApplicationContext applicationContext,
                                                     String passwordlessPropertiesPrefix,
                                                     PasswordlessProperties passwordlessProperties,
                                                     Map<String, String> result) {
        Properties properties = new Properties();
        result.forEach(properties::setProperty);
        enhancePasswordlessProperties(applicationContext, passwordlessPropertiesPrefix, passwordlessProperties, properties);
        properties.forEach((key, value) -> result.put((String) key, (String) value));
    }
}
