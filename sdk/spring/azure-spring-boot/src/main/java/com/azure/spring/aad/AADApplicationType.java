// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * AAD application type.
 * <p>Provides some common methods to determine the application type according to the {@link AADAuthenticationProperties} properties.</p>
 */
public enum AADApplicationType {

    WEB_APPLICATION("web_application"),
    RESOURCE_SERVER("resource_server"),
    RESOURCE_SERVER_WITH_OBO("resource_server_with_obo"),
    WEB_APPLICATION_AND_RESOURCE_SERVER("web_application_and_resource_server");

    private String applicationType;

    /**
     * AADApplicationType constructor.
     *
     * <table border="1">
     *   <tr>
     *     <th> Application type </th> <th> Has dependency: spring-security-oauth2-client</th> <th> Has dependency: spring-security-oauth2-resource-server </th> <th> Set azure.activedirectory.application-type </th>
     *   </tr>
     *   <tr>
     *     <td> Web application </td> <td> Yes </td> <td> No </td> <td> Not required, it can be set to 'web_application' </td>
     *   </tr>
     *   <tr>
     *     <td> Resource Server </td> <td> No </td> <td> Yes </td> <td> Not required, it can be set to 'resource_server' </td>
     *   </tr>
     *   <tr>
     *     <td> Resource Server with OBO function </td> <td> Yes </td> <td> Yes </td> <td> Not required, it can be set to 'resource_server_with_obo' </td>
     *   </tr>
     *   <tr>
     *     <td> Web Application and Resource Server </td> <td> Yes </td> <td> Yes </td> <td> Required, set to 'web_application_and_resource_server' </td>
     *   </tr>
     * </table>
     * @param applicationType application type
     */
    AADApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getValue() {
        return applicationType;
    }

    public static final String BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME =
        "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken";
    public static final String ENABLE_WEB_SECURITY_CLASS_NAME =
        "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity";
    public static final String CLIENT_REGISTRATION_CLASS_NAME =
        "org.springframework.security.oauth2.client.registration.ClientRegistration";

    /**
     * Detect the application type when the application-type is messing.
     * @param properties AADAuthenticationProperties
     * @return AADApplicationType
     * @throws IllegalStateException Unrecognized application type
     */
    public static AADApplicationType defaultApplicationType(final AADAuthenticationProperties properties) {
        AADApplicationType appType;
        if (isOAuth2ClientAvailable()
            && !isResourceServerAvailable()
            && StringUtils.hasText(properties.getClientId())) {
            appType = AADApplicationType.WEB_APPLICATION;
        } else if (!isOAuth2ClientAvailable()
            && isResourceServerAvailable()) {
            appType = AADApplicationType.RESOURCE_SERVER;
        } else if (isOAuth2ClientAvailable()
            && isResourceServerAvailable()) {
            appType = AADApplicationType.RESOURCE_SERVER_WITH_OBO;
        } else {
            throw new IllegalStateException("Unrecognized application type. "
                + "Please confirm dependencies or explicitly configure 'azure.activedirectory.application-type'.");
        }
        return appType;
    }

    public static boolean validateApplicationType(final AADAuthenticationProperties properties) {
        AADApplicationType configured = properties.getApplicationType();
        if (isOAuth2ClientAvailable()
            && !isResourceServerAvailable()) {
            return AADApplicationType.WEB_APPLICATION == configured;
        } else if (!isOAuth2ClientAvailable()
            && isResourceServerAvailable()) {
            return AADApplicationType.RESOURCE_SERVER == configured;
        } else if (isOAuth2ClientAvailable()
            && isResourceServerAvailable()) {
            return AADApplicationType.RESOURCE_SERVER_WITH_OBO == configured
                || AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER == configured;
        }
        return false;
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
        return isPresent(ENABLE_WEB_SECURITY_CLASS_NAME) && isPresent(CLIENT_REGISTRATION_CLASS_NAME);
    }

    private static boolean isResourceServerAvailable() {
        return isPresent(BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME);
    }

    private static boolean isPresent(String className) {
        return ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }
}
