// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties;

import org.springframework.util.ClassUtils;

/**
 * Azure AD application type.
 * <p>The value can be inferred by dependencies, only 'web_application_and_resource_server' must be configured manually.</p>
 * <pre>
 * | Has dependency: spring-security-oauth2-client | Has dependency: spring-security-oauth2-resource-server | Valid values of application type                                                                       | Default value               |
 * |-----------------------------------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------|-----------------------------|
 * |                      Yes                      |                          No                            |  'web_application'                                                                                     |       'web_application'     |
 * |                      No                       |                          Yes                           |  'resource_server'                                                                                     |       'resource_server'     |
 * |                      Yes                      |                          Yes                           |  'web_application','resource_server','resource_server_with_obo', 'web_application_and_resource_server' | 'resource_server_with_obo'  |
 * </pre>
 */
public enum AadApplicationType {
    /**
     * Web application
     */
    WEB_APPLICATION("web_application"),

    /**
     * Resource server
     */
    RESOURCE_SERVER("resource_server"),

    /**
     * Resource server with OBO
     */
    RESOURCE_SERVER_WITH_OBO("resource_server_with_obo"),

    /**
     * Web application and resource server
     */
    WEB_APPLICATION_AND_RESOURCE_SERVER("web_application_and_resource_server");

    private final String applicationType;

    AadApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    /**
     * Gets the string representation of the enum.
     *
     * @return the string representation of the enum
     */
    public String getValue() {
        return applicationType;
    }

    /**
     * The Spring security OAuth2 client class name
     */
    public static final String SPRING_SECURITY_OAUTH2_CLIENT_CLASS_NAME =
        "org.springframework.security.oauth2.client.registration.ClientRegistration";

    /**
     * The Spring security OAuth2 resource server class name
     */
    public static final String SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_CLASS_NAME =
        "org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken";

    /**
     * Infer application type by dependencies
     *
     * @return AADApplicationType
     */
    public static AadApplicationType inferApplicationTypeByDependencies() {
        AadApplicationType type;
        if (isOAuth2ClientAvailable()) {
            if (isResourceServerAvailable()) {
                type = AadApplicationType.RESOURCE_SERVER_WITH_OBO;
            } else {
                type = AadApplicationType.WEB_APPLICATION;
            }
        } else {
            if (isResourceServerAvailable()) {
                type = AadApplicationType.RESOURCE_SERVER;
            } else {
                type = null;
            }
        }
        return type;
    }

    private static boolean isOAuth2ClientAvailable() {
        return isPresent(SPRING_SECURITY_OAUTH2_CLIENT_CLASS_NAME);
    }

    private static boolean isResourceServerAvailable() {
        return isPresent(SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_CLASS_NAME);
    }

    private static boolean isPresent(String className) {
        return ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }
}
