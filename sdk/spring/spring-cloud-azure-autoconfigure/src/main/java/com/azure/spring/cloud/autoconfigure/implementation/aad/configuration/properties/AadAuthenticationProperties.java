// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AuthorizationClientProperties;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.RESOURCE_SERVER;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.RESOURCE_SERVER_WITH_OBO;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.WEB_APPLICATION;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.inferApplicationTypeByDependencies;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants.AZURE_DELEGATED;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants.ON_BEHALF_OF;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT;

public class AadAuthenticationProperties implements InitializingBean {

    /**
     * Properties prefix.
     */
    public static final String PREFIX = "spring.cloud.azure.active-directory";

    private static final Logger LOGGER = LoggerFactory.getLogger(AadAuthenticationProperties.class);

    private static final String UNMATCHING_OAUTH_GRANT_TYPE_FROMAT = "When 'spring.cloud.azure.active-directory"
        + ".application-type=%s', 'spring.cloud.azure.active-directory.authorization-clients.%s"
        + ".authorization-grant-type' can not be '%s'.";

    /**
     * Profile of Azure cloud environment.
     */
    @NestedConfigurationProperty
    private final AadProfileProperties profile = new AadProfileProperties();

    /**
     * Properties used for authorize.
     */
    @NestedConfigurationProperty
    private final AadCredentialProperties credential = new AadCredentialProperties();


    /**
     * Default UserGroup configuration.
     */
    private final UserGroupProperties userGroup = new UserGroupProperties();

    /**
     * Decide which claim to be principal's name.
     */
    private String userNameAttribute;

    /**
     * Redirection Endpoint: Used by the authorization server to return responses containing authorization credentials
     * to the client via the resource owner user-agent.
     */
    private String redirectUriTemplate = "{baseUrl}/login/oauth2/code/";

    /**
     * App ID URI which might be used in the "aud" claim of an id_token. For instance, 'api://{applicationId}'.
     * See Microsoft doc about APP ID URL for more details: https://learn.microsoft.com/azure/active-directory/develop/security-best-practices-for-app-registration#application-id-uri
     */
    private String appIdUri;

    /**
     * Additional parameters above the standard parameters defined in the OAuth 2.0 Authorization Framework. Would be added to the Authorization URL for customizing the Authorization Request. For instance, 'prompt: login'.
     * See Microsoft doc about more additional parameters information: https://learn.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-authorization-code
     */
    private final Map<String, Object> authenticateAdditionalParameters = new HashMap<>();

    /**
     * Connection Timeout(duration) for the JWKSet Remote URL call. The default value is `500s`.
     * @deprecated If you want to configure this, please provide a RestOperations bean.
     */
    @Deprecated
    private Duration jwtConnectTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT);

    /**
     * Read Timeout(duration) for the JWKSet Remote URL call. The default value is `500s`.
     * @deprecated If you want to configure this, please provide a RestOperations bean.
     */
    @Deprecated
    private Duration jwtReadTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT);

    /**
     * Size limit in Bytes of the JWKSet Remote URL call. The default value is `51200`.
     * @deprecated If you want to configure this, please provide a RestOperations bean.
     */
    @Deprecated
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT; /* bytes */

    /**
     * The lifespan(duration) of the cached JWK set before it expires. The default value is `5m`.
     */
    private Duration jwkSetCacheLifespan = Duration.ofMinutes(5);

    /**
     * The refresh time(duration) of the cached JWK set before it expires. The default value is `5m`.
     */
    private Duration jwkSetCacheRefreshTime = Duration.ofMinutes(5);

    /**
     * The redirect uri after logout. For instance, 'http://localhost:8080/'.
     * See Microsoft doc about Redirect URI for more details: https://learn.microsoft.com/azure/active-directory/develop/security-best-practices-for-app-registration#redirect-uri
     */
    private String postLogoutRedirectUri;

    /**
     * If true activates the stateless auth filter AADAppRoleStatelessAuthenticationFilter. The default is false which
     * activates AADAuthenticationFilter.
     */
    private Boolean sessionStateless = false;

    /**
     * The OAuth2 authorization clients, contains the authorization grant type, client authentication method and scope.
     * The clients will be converted to OAuth2 ClientRegistration, the other ClientRegistration information(such as client id, client secret) inherits from the delegated OAuth2 login client 'azure'.
     * For instance,'
     * authorization-clients.webapi.authorization-grant-type=on_behalf_of,
     * authorization-clients.webapi.client-authentication-method=client_secret_post,
     * authorization-clients.webapi.scopes[0]={WEB_API_APP_ID_URL}/WebApi.ExampleScope1,
     * authorization-clients.webapi.scopes[0]={WEB_API_APP_ID_URL}/WebApi.ExampleScope2
     * '.
     */
    private final Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    /**
     * Type of the Azure AD application. Supported types are: WEB_APPLICATION, RESOURCE_SERVER, RESOURCE_SERVER_WITH_OBO, WEB_APPLICATION_AND_RESOURCE_SERVER. The value can be inferred by dependencies, only 'web_application_and_resource_server' must be configured manually.
     */
    private AadApplicationType applicationType;

    private static final Map<AadApplicationType, Set<AuthorizationGrantType>> NON_COMPATIBLE_APPLICATION_TYPE_AND_GRANT_TYPES = initCompatibleApplicationTypeAndGrantTypes();

    private static Map<AadApplicationType, Set<AuthorizationGrantType>> initCompatibleApplicationTypeAndGrantTypes() {
        Map<AadApplicationType, Set<AuthorizationGrantType>> nonCompatibleApplicationTypeAndGrantTypes =
            new HashMap<>();
        nonCompatibleApplicationTypeAndGrantTypes.put(WEB_APPLICATION,
            Stream.of(ON_BEHALF_OF, JWT_BEARER).collect(Collectors.toSet()));
        nonCompatibleApplicationTypeAndGrantTypes.put(RESOURCE_SERVER,
            Stream.of(AUTHORIZATION_CODE, ON_BEHALF_OF, JWT_BEARER).collect(Collectors.toSet()));
        nonCompatibleApplicationTypeAndGrantTypes.put(RESOURCE_SERVER_WITH_OBO,
            Stream.of(AUTHORIZATION_CODE).collect(Collectors.toSet()));

        return nonCompatibleApplicationTypeAndGrantTypes;
    }

    public AadProfileProperties getProfile() {
        return profile;
    }

    public AadCredentialProperties getCredential() {
        return credential;
    }

    public AadApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(AadApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    /**
     * Properties dedicated to changing the behavior of how the groups are mapped from the Azure AD response. Depending
     * on the graph API used the object will not be the same.
     */
    public static class UserGroupProperties {

        /**
         * The group names can be used to construct GrantedAuthority.
         */
        private List<String> allowedGroupNames = new ArrayList<>();

        /**
         * The group IDs can be used to construct GrantedAuthority.
         */
        private Set<String> allowedGroupIds = new HashSet<>();

        /**
         * Whether to use transitive way to get members. If "true", use "v1.0/me/transitiveMemberOf" to get members. Otherwise, use "v1.0/me/memberOf". The default value is `false`.
         */
        private boolean useTransitiveMembers = false;

        public Set<String> getAllowedGroupIds() {
            return allowedGroupIds;
        }

        public void setAllowedGroupIds(Set<String> allowedGroupIds) {
            this.allowedGroupIds = allowedGroupIds;
        }

        public List<String> getAllowedGroupNames() {
            return allowedGroupNames;
        }

        public void setAllowedGroupNames(List<String> allowedGroupNames) {
            this.allowedGroupNames = allowedGroupNames;
        }

        public boolean isUseTransitiveMembers() {
            return useTransitiveMembers;
        }

        public void setUseTransitiveMembers(boolean useTransitiveMembers) {
            this.useTransitiveMembers = useTransitiveMembers;
        }

    }

    public boolean isAllowedGroupNamesConfigured() {
        return Optional.of(this.getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupNames)
                       .map(allowedGroupNames -> !allowedGroupNames.isEmpty())
                       .orElse(false);
    }

    public boolean isAllowedGroupIdsConfigured() {
        return Optional.of(this.getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupIds)
                       .map(allowedGroupIds -> !allowedGroupIds.isEmpty())
                       .orElse(false);
    }

    public UserGroupProperties getUserGroup() {
        return userGroup;
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

    public Duration getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    public void setJwtConnectTimeout(Duration jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    public Duration getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    public void setJwtReadTimeout(Duration jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    public Duration getJwkSetCacheLifespan() {
        return jwkSetCacheLifespan;
    }

    public void setJwkSetCacheLifespan(Duration jwkSetCacheLifespan) {
        this.jwkSetCacheLifespan = jwkSetCacheLifespan;
    }

    public Duration getJwkSetCacheRefreshTime() {
        return jwkSetCacheRefreshTime;
    }

    public void setJwkSetCacheRefreshTime(Duration jwkSetCacheRefreshTime) {
        this.jwkSetCacheRefreshTime = jwkSetCacheRefreshTime;
    }

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public Boolean getSessionStateless() {
        return sessionStateless;
    }

    public void setSessionStateless(Boolean sessionStateless) {
        this.sessionStateless = sessionStateless;
    }

    public String getGraphMembershipUri() {
        return getProfile().getEnvironment().getMicrosoftGraphEndpoint()
            + (getUserGroup().isUseTransitiveMembers()
            ? "v1.0/me/transitiveMemberOf"
            : "v1.0/me/memberOf");
    }

    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    public boolean isAllowedGroup(String group) {
        return Optional.ofNullable(getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupNames)
                       .orElseGet(Collections::emptyList)
                       .contains(group)
            || Optional.ofNullable(getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupIds)
                       .orElseGet(Collections::emptySet)
                       .contains(group);
    }

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(getProfile().getTenantId())) {
            this.getProfile().setTenantId("common");
        }
        validateProperties();
    }

    private void validateProperties() {

        Set<String> allowedGroupIds = userGroup.getAllowedGroupIds();
        if (allowedGroupIds.size() > 1 && allowedGroupIds.contains("all")) {
            throw new IllegalStateException("When spring.cloud.azure.active-directory.user-group.allowed-group-ids "
                + "contains 'all', no other group ids can be configured. "
                + "But actually spring.cloud.azure.active-directory.user-group.allowed-group-ids="
                + allowedGroupIds);
        }

        validateTenantId();
        validateApplicationType(); // This must before validateAuthorizationClients().
        validateAuthorizationClients();
    }

    private void validateAuthorizationClients() {
        authorizationClients.forEach(this::validateAuthorizationClientProperties);
    }

    private void validateTenantId() {
        if (isMultiTenantsApplication(getProfile().getTenantId()) && !userGroup.getAllowedGroupNames().isEmpty()) {
            throw new IllegalStateException("When spring.cloud.azure.active-directory.profile.tenant-id is "
                + "'common/organizations/consumers', "
                + "spring.cloud.azure.active-directory.user-group.allowed-group-names should be empty. "
                + "But actually spring.cloud.azure.active-directory.profile.tenant-id=" + getProfile().getTenantId()
                + ", and spring.cloud.azure.active-directory.user-group.allowed-group-names="
                + userGroup.getAllowedGroupNames());
        }

        if (isMultiTenantsApplication(getProfile().getTenantId()) && !userGroup.getAllowedGroupIds().isEmpty()) {
            throw new IllegalStateException("When spring.cloud.azure.active-directory.profile.tenant-id is "
                + "'common/organizations/consumers', "
                + "spring.cloud.azure.active-directory.user-group.allowed-group-ids should be empty. "
                + "But actually spring.cloud.azure.active-directory.profile.tenant-id=" + getProfile().getTenantId()
                + ", and spring.cloud.azure.active-directory.user-group.allowed-group-ids=" + userGroup.getAllowedGroupIds());
        }
    }

    private void validateApplicationType() {
        AadApplicationType inferred = inferApplicationTypeByDependencies();
        if (applicationType != null) {
            if (!isValidApplicationType(applicationType, inferred)) {
                throw new IllegalStateException(
                    "Invalid property 'spring.cloud.azure.active-directory.application-type', the configured value is '"
                        + applicationType.getValue() + "', " + "but the inferred value is '"
                        + inferred.getValue() + "'.");
            }
        } else {
            applicationType = inferred;
        }
    }

    private boolean isValidApplicationType(AadApplicationType configured, AadApplicationType inferred) {
        return inferred == configured || inferred == RESOURCE_SERVER_WITH_OBO;
    }

    private void validateAuthorizationClientProperties(String registrationId,
                                                       AuthorizationClientProperties properties) {
        if (CLIENT_SECRET_JWT.equals(properties.getClientAuthenticationMethod())) {
            throw new IllegalStateException("The client authentication method of '"
                + registrationId + "' is not supported.");
        }

        AuthorizationGrantType grantType = properties.getAuthorizationGrantType();
        if (grantType != null) {
            validateAuthorizationGrantType(registrationId, grantType);
        } else {
            grantType = decideDefaultGrantTypeFromApplicationType(registrationId, applicationType);
            properties.setAuthorizationGrantType(grantType);

            LOGGER.debug("The client '{}' sets the default value of AuthorizationGrantType to '{}'.", grantType,
                registrationId);
        }

        // Extract validated scopes from properties
        List<String> scopes = extractValidatedScopes(registrationId, properties);
        addNecessaryScopesForAuthorizationCodeClients(registrationId, properties, scopes);
    }

    /**
     * Add necessary scopes for authorization_code clients.
     *
     * https://docs.microsoft.com/graph/permissions-reference#remarks-17
     * https://docs.microsoft.com/azure/active-directory/develop/v2-permissions-and-consent#openid-connect-scopes
     *
     * "openid" : allows to request an ID token.
     * "profile" : allows returning additional claims in the ID token.
     * "offline_access" : allows to request a refresh token.
     * @param registrationId client ID
     * @param properties AuthorizationClientProperties
     * @param scopes scopes for authorization_code clients.
     */
    private void addNecessaryScopesForAuthorizationCodeClients(String registrationId,
                                                               AuthorizationClientProperties properties,
                                                               List<String> scopes) {
        if (AZURE_CLIENT_REGISTRATION_ID.equals(registrationId) && (scopes == null || scopes.isEmpty())) {
            return;
        }

        if (properties.getAuthorizationGrantType().equals(AUTHORIZATION_CODE)) {
            String[] scopesNeeded = new String[] { "openid", "profile", "offline_access" };
            for (String scope : scopesNeeded) {
                if (!scopes.contains(scope)) {
                    scopes.add(scope);
                }
            }
        }
    }

    private List<String> extractValidatedScopes(String registrationId, AuthorizationClientProperties properties) {
        List<String> scopes = properties.getScopes();
        if (!AZURE_CLIENT_REGISTRATION_ID.equals(registrationId) && (scopes == null || scopes.isEmpty())) {
            throw new IllegalStateException(
                "'spring.cloud.azure.active-directory.authorization-clients." + registrationId + ".scopes' must be "
                    + "configured");
        }
        return scopes;
    }

    private void validateAuthorizationGrantType(String registrationId, AuthorizationGrantType grantType) {
        if (NON_COMPATIBLE_APPLICATION_TYPE_AND_GRANT_TYPES.containsKey(applicationType)) {
            if (NON_COMPATIBLE_APPLICATION_TYPE_AND_GRANT_TYPES.get(applicationType).contains(grantType)) {
                throw new IllegalStateException(String.format(UNMATCHING_OAUTH_GRANT_TYPE_FROMAT,
                    applicationType.getValue(), registrationId, grantType.getValue()));
            }
            LOGGER.debug("'spring.cloud.azure.active-directory.authorization-clients.{}.authorization-grant-type'"
                + " is valid.", registrationId);
        }

        if (AZURE_CLIENT_REGISTRATION_ID.equals(registrationId) && !AUTHORIZATION_CODE.equals(grantType)) {
            throw new IllegalStateException("spring.cloud.azure.active-directory.authorization-clients."
                + AZURE_CLIENT_REGISTRATION_ID
                + ".authorization-grant-type must be configured to 'authorization_code'.");
        }
    }

    /**
     * Decide the default grant type from application type.
     * @param registrationId client ID
     * @param appType AadApplicationType
     * @return default grant type
     */
    private AuthorizationGrantType decideDefaultGrantTypeFromApplicationType(String registrationId,
                                                                             AadApplicationType appType) {
        AuthorizationGrantType grantType;
        switch (appType) {
            case WEB_APPLICATION:
                if (registrationId.equals(AZURE_CLIENT_REGISTRATION_ID)) {
                    grantType = AUTHORIZATION_CODE;
                } else {
                    grantType = AZURE_DELEGATED;
                }
                break;
            case RESOURCE_SERVER:
            case RESOURCE_SERVER_WITH_OBO:
                grantType = JWT_BEARER;
                break;
            case WEB_APPLICATION_AND_RESOURCE_SERVER:
                throw new IllegalStateException("spring.cloud.azure.active-directory.authorization-clients." + registrationId
                    + ".authorization-grant-grantType must be configured. ");
            default:
                throw new IllegalStateException("Unsupported authorization grantType " + appType.getValue());
        }

        return grantType;
    }

    private boolean isMultiTenantsApplication(String tenantId) {
        return "common".equals(tenantId) || "organizations".equals(tenantId) || "consumers".equals(tenantId);
    }
}
