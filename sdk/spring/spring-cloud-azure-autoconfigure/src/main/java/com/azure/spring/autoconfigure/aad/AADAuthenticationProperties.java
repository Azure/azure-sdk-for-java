// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADApplicationType;
import com.azure.spring.aad.AADAuthorizationGrantType;
import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.aad.AADApplicationType.inferApplicationTypeByDependencies;
import static com.azure.spring.aad.AADAuthorizationGrantType.AUTHORIZATION_CODE;
import static com.azure.spring.aad.AADAuthorizationGrantType.AZURE_DELEGATED;
import static com.azure.spring.aad.AADAuthorizationGrantType.ON_BEHALF_OF;
import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * Configuration properties for Azure Active Directory Authentication.
 */
@Validated
@ConfigurationProperties(AADAuthenticationProperties.PREFIX)
public class AADAuthenticationProperties implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADAuthenticationProperties.class);
    public static final String PREFIX = "azure.activedirectory";

    /**
     * Registered application ID in Azure AD. Must be configured when OAuth2 authentication is done in front end
     */
    private String clientId;

    /**
     * API Access Key of the registered application. Must be configured when OAuth2 authentication is done in front end
     */
    private String clientSecret;

    /**
     * Decide which claim to be principal's name..
     */
    private String userNameAttribute;

    /**
     * Redirection Endpoint: Used by the authorization server to return responses containing authorization credentials
     * to the client via the resource owner user-agent.
     */
    private String redirectUriTemplate;

    /**
     * App ID URI which might be used in the <code>"aud"</code> claim of an <code>id_token</code>.
     */
    private String appIdUri;

    /**
     * Add additional parameters to the Authorization URL.
     */
    private Map<String, Object> authenticateAdditionalParameters;

    /**
     * Azure Tenant ID.
     */
    private String tenantId;

    private String postLogoutRedirectUri;

    private String baseUri;

    private String graphBaseUri;

    private Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    private AADApplicationType applicationType;

    public AADApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(AADApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getRedirectUriTemplate() {
        return redirectUriTemplate;
    }

    public void setRedirectUriTemplate(String redirectUriTemplate) {
        this.redirectUriTemplate = redirectUriTemplate;
    }

    public String getAppIdUri() {
        return appIdUri;
    }

    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }

    public Map<String, Object> getAuthenticateAdditionalParameters() {
        return authenticateAdditionalParameters;
    }

    public void setAuthenticateAdditionalParameters(Map<String, Object> authenticateAdditionalParameters) {
        this.authenticateAdditionalParameters = authenticateAdditionalParameters;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getGraphBaseUri() {
        return graphBaseUri;
    }

    public void setGraphBaseUri(String graphBaseUri) {
        this.graphBaseUri = graphBaseUri;
    }

    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    public void setAuthorizationClients(Map<String, AuthorizationClientProperties> authorizationClients) {
        this.authorizationClients = authorizationClients;
    }

    @Override
    public void afterPropertiesSet() {

        if (!StringUtils.hasText(baseUri)) {
            baseUri = "https://login.microsoftonline.com/";
        } else {
            baseUri = addSlash(baseUri);
        }

        if (!StringUtils.hasText(redirectUriTemplate)) {
            redirectUriTemplate = "{baseUrl}/login/oauth2/code/";
        }

        if (!StringUtils.hasText(graphBaseUri)) {
            graphBaseUri = "https://graph.microsoft.com/";
        } else {
            graphBaseUri = addSlash(graphBaseUri);
        }

        validateTenantId();
        validateApplicationType(); // This must before validateAuthorizationClients().
        validateAuthorizationClients();
    }

    private void validateAuthorizationClients() {
        authorizationClients.forEach(this::validateAuthorizationClientProperties);
    }

    private void validateTenantId() {
        if (!StringUtils.hasText(tenantId)) {
            tenantId = "common";
        }

    }

    /**
     * Validate configured application type or set default value.
     *
     * @throws IllegalStateException Invalid property 'azure.activedirectory.application-type'
     */
    private void validateApplicationType() {
        AADApplicationType inferred = inferApplicationTypeByDependencies();
        if (applicationType != null) {
            if (!isValidApplicationType(applicationType, inferred)) {
                throw new IllegalStateException(
                    "Invalid property 'azure.activedirectory.application-type', the configured value is '"
                        + applicationType.getValue() + "', " + "but the inferred value is '"
                        + inferred.getValue() + "'.");
            }
        } else {
            applicationType = inferred;
        }
    }

    private boolean isValidApplicationType(AADApplicationType configured, AADApplicationType inferred) {
        return inferred == configured || inferred == AADApplicationType.RESOURCE_SERVER_WITH_OBO;
    }

    private void validateAuthorizationClientProperties(String registrationId,
                                                       AuthorizationClientProperties properties) {
        String grantType = Optional.of(properties)
                                   .map(AuthorizationClientProperties::getAuthorizationGrantType)
                                   .map(AADAuthorizationGrantType::getValue)
                                   .orElse(null);
        if (null == grantType) {
            // Set default value for authorization grant grantType
            switch (applicationType) {
                case WEB_APPLICATION:
                    if (properties.isOnDemand()) {
                        properties.setAuthorizationGrantType(AUTHORIZATION_CODE);
                    } else {
                        properties.setAuthorizationGrantType(AZURE_DELEGATED);
                    }
                    LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to "
                        + "'authorization_code'.", registrationId);
                    break;
                case RESOURCE_SERVER:
                case RESOURCE_SERVER_WITH_OBO:
                    properties.setAuthorizationGrantType(AADAuthorizationGrantType.ON_BEHALF_OF);
                    LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to "
                        + "'on_behalf_of'.", registrationId);
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                    throw new IllegalStateException("azure.activedirectory.authorization-clients." + registrationId
                        + ".authorization-grant-grantType must be configured. ");
                default:
                    throw new IllegalStateException("Unsupported authorization grantType " + applicationType.getValue());
            }
        } else {
            // Validate authorization grant grantType
            switch (applicationType) {
                case WEB_APPLICATION:
                    if (ON_BEHALF_OF.getValue().equals(grantType)) {
                        throw new IllegalStateException("When 'azure.activedirectory.application-type=web_application',"
                            + " 'azure.activedirectory.authorization-clients." + registrationId
                            + ".authorization-grant-type' can not be 'on_behalf_of'.");
                    }
                    break;
                case RESOURCE_SERVER:
                    if (AUTHORIZATION_CODE.getValue().equals(grantType)) {
                        throw new IllegalStateException("When 'azure.activedirectory.application-type=resource_server',"
                            + " 'azure.activedirectory.authorization-clients." + registrationId
                            + ".authorization-grant-type' can not be 'authorization_code'.");
                    }
                    if (ON_BEHALF_OF.getValue().equals(grantType)) {
                        throw new IllegalStateException("When 'azure.activedirectory.application-type=resource_server',"
                            + " 'azure.activedirectory.authorization-clients." + registrationId
                            + ".authorization-grant-type' can not be 'on_behalf_of'.");
                    }
                    break;
                case RESOURCE_SERVER_WITH_OBO:
                    if (AUTHORIZATION_CODE.getValue().equals(grantType)) {
                        throw new IllegalStateException("When 'azure.activedirectory"
                            + ".application-type=resource_server_with_obo',"
                            + " 'azure.activedirectory.authorization-clients." + registrationId
                            + ".authorization-grant-type' can not be 'authorization_code'.");
                    }
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                default:
                    LOGGER.debug("'azure.activedirectory.authorization-clients." + registrationId
                        + ".authorization-grant-type' is valid.");
            }

            if (properties.isOnDemand()
                && !AUTHORIZATION_CODE.getValue().equals(grantType)) {
                throw new IllegalStateException("onDemand only support authorization_code grant grantType. Please set "
                    + "'azure.activedirectory.authorization-clients." + registrationId
                    + ".authorization-grant-grantType=authorization_code'"
                    + " or 'azure.activedirectory.authorization-clients." + registrationId + ".on-demand=false'.");
            }

            if (AZURE_CLIENT_REGISTRATION_ID.equals(registrationId)
                && !AUTHORIZATION_CODE.equals(properties.getAuthorizationGrantType())) {
                throw new IllegalStateException("azure.activedirectory.authorization-clients."
                    + AZURE_CLIENT_REGISTRATION_ID
                    + ".authorization-grant-grantType must be configured to 'authorization_code'.");
            }
        }

        // Validate scopes.
        List<String> scopes = properties.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalStateException(
                "'azure.activedirectory.authorization-clients." + registrationId + ".scopes' must be configured");
        }
        // Add necessary scopes for authorization_code clients.
        // https://docs.microsoft.com/en-us/graph/permissions-reference#remarks-17
        // https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-permissions-and-consent#openid-connect-scopes
        if (properties.getAuthorizationGrantType().getValue().equals(AUTHORIZATION_CODE.getValue())) {
            if (!scopes.contains("openid")) {
                scopes.add("openid"); // "openid" allows to request an ID token.
            }
            if (!scopes.contains("profile")) {
                scopes.add("profile"); // "profile" allows to return additional claims in the ID token.
            }
            if (!scopes.contains("offline_access")) {
                scopes.add("offline_access"); // "offline_access" allows to request a refresh token.
            }
        }
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
