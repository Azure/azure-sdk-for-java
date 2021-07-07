// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import static com.azure.spring.aad.AADConditions.BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME;

/**
 * AAD application type.
 * Provides some common methods to determine the application type according to the {@link AADAuthenticationProperties} properties.
 */
public enum AADApplicationType {

    WEB_APPLICATION(),
    RESOURCE_SERVER(),
    RESOURCE_SERVER_WITH_OBO(),
    WEB_APPLICATION_AND_RESOURCE_SERVER();

    private static final Logger LOGGER = LoggerFactory.getLogger(AADApplicationType.class);

    public static AADApplicationType applicationType(AADAuthenticationProperties properties) {
        AADApplicationType appType = null;
        if (isOAuth2ClientAvailable()
            && !isResourceServerAvailable()) {
            appType = AADApplicationType.WEB_APPLICATION;
            LOGGER.debug("The Web Application scenario detected.");
        } else if (!isOAuth2ClientAvailable()
            && isResourceServerAvailable()) {
            appType = AADApplicationType.RESOURCE_SERVER;
            LOGGER.debug("The Resource Server scenario detected.");
        } else if (isOAuth2ClientAvailable()
            && isResourceServerAvailable()
            && !properties.getEnableWebAppAndResourceServer()) {
            appType = AADApplicationType.RESOURCE_SERVER_WITH_OBO;
            LOGGER.debug("The Resource Server with Obo scenario detected.");
        } else if (isOAuth2ClientAvailable() && isResourceServerAvailable()) {
            if (properties.getEnableWebAppAndResourceServer()) {
                appType = AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;
                LOGGER.debug("The Web Application and Resource Server scenario detected.");
            } else {
                LOGGER.warn("You need to explicitly enable the switch "
                    + "'azure.activedirectory.enable-web-app-and-resource-server' "
                    + "to apply Web Application and Resource Server scenario.");
            }
        }
        return appType;
    }

    public static boolean isWebApplicationOnly(AADApplicationType applicationType) {
        return AADApplicationType.WEB_APPLICATION == applicationType;
    }

    public static boolean isResourceServerOnly(AADApplicationType applicationType) {
        return AADApplicationType.RESOURCE_SERVER == applicationType;
    }

    public static boolean isResourceServerWithObo(AADApplicationType applicationType) {
        return AADApplicationType.RESOURCE_SERVER_WITH_OBO == applicationType;
    }

    public static boolean isWebApplicationAndResourceServer(AADApplicationType applicationType) {
        return AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER == applicationType;
    }

    private static boolean isOAuth2ClientAvailable() {
        return isPresent("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
            && isPresent("org.springframework.security.oauth2.client.registration.ClientRegistration");
    }

    private static boolean isResourceServerAvailable() {
        return isPresent(BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME);
    }

    private static boolean isPresent(String className) {
        return ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }
}
