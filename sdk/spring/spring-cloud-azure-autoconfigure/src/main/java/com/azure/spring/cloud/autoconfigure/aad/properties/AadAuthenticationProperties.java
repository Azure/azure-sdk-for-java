// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.RESOURCE_SERVER;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.RESOURCE_SERVER_WITH_OBO;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.WEB_APPLICATION;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.inferApplicationTypeByDependencies;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType.AUTHORIZATION_CODE;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType.AZURE_DELEGATED;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType.ON_BEHALF_OF;

/**
 * Configuration properties for Azure Active Directory Authentication.
 *
 * @see InitializingBean
 */
public class AadAuthenticationProperties implements InitializingBean {

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
     * App ID URI which might be used in the "aud" claim of an id_token.
     */
    private String appIdUri;

    /**
     * Add additional parameters to the Authorization URL.
     */
    private final Map<String, Object> authenticateAdditionalParameters = new HashMap<>();

    /**
     * Connection Timeout for the JWKSet Remote URL call.
     */
    private Duration jwtConnectTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT);

    /**
     * Read Timeout for the JWKSet Remote URL call.
     */
    private Duration jwtReadTimeout = Duration.ofMillis(RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT);

    /**
     * Size limit in Bytes of the JWKSet Remote URL call.
     */
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT; /* bytes */

    /**
     * The lifespan of the cached JWK set before it expires, default is 5 minutes.
     */
    private Duration jwkSetCacheLifespan = Duration.ofMinutes(5);

    /**
     * The refresh time of the cached JWK set before it expires, default is 5 minutes.
     */
    private Duration jwkSetCacheRefreshTime = Duration.ofMinutes(5);

    /**
     * The redirect uri after logout.
     */
    private String postLogoutRedirectUri;

    /**
     * If true activates the stateless auth filter AADAppRoleStatelessAuthenticationFilter. The default is false which
     * activates AADAuthenticationFilter.
     */
    private Boolean sessionStateless = false;

    /**
     * The OAuth2 authorization clients.
     */
    private final Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    /**
     * Type of the AAD application.
     */
    private AadApplicationType applicationType;

    private final Map<AadApplicationType, List<String>> grantTypeValidateMap = new HashMap<>();

    public AadAuthenticationProperties() {
        initGrantTypeValidateMap();
    }

    private void initGrantTypeValidateMap() {
        grantTypeValidateMap.put(WEB_APPLICATION, Arrays.asList(ON_BEHALF_OF.getValue()));
        grantTypeValidateMap.put(RESOURCE_SERVER, Arrays.asList(AUTHORIZATION_CODE.getValue(),
            ON_BEHALF_OF.getValue()));
        grantTypeValidateMap.put(RESOURCE_SERVER_WITH_OBO, Arrays.asList(AUTHORIZATION_CODE.getValue()));
    }


    /**
     * @return The AADProfileProperties.
     */
    public AadProfileProperties getProfile() {
        return profile;
    }

    /**
     * @return The AADCredentialProperties.
     */
    public AadCredentialProperties getCredential() {
        return credential;
    }

    /**
     * Gets the AADApplicationType.
     *
     * @return the AADApplicationType
     */
    public AadApplicationType getApplicationType() {
        return applicationType;
    }

    /**
     * Sets the AADApplicationType.
     *
     * @param applicationType the AADApplicationType
     */
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
         * The group ids can be used to construct GrantedAuthority.
         */
        private Set<String> allowedGroupIds = new HashSet<>();

        /**
         * If "true", use "v1.0/me/transitiveMemberOf" to get members. Otherwise, use "v1.0/me/memberOf".
         */
        private boolean useTransitiveMembers = false;

        /**
         * Gets the set of allowed group IDs.
         *
         * @return the set of allowed group IDs
         */
        public Set<String> getAllowedGroupIds() {
            return allowedGroupIds;
        }

        /**
         * Set the allowed group ids.
         *
         * @param allowedGroupIds Allowed group ids.
         */
        public void setAllowedGroupIds(Set<String> allowedGroupIds) {
            this.allowedGroupIds = allowedGroupIds;
        }

        /**
         * Gets the list of allowed group names.
         *
         * @return the list of allowed group names
         */
        public List<String> getAllowedGroupNames() {
            return allowedGroupNames;
        }

        /**
         * Sets the list of allowed group names.
         *
         * @param allowedGroupNames the list of allowed group names
         */
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

    /**
     * Whether allowed group names is configured.
     *
     * @return whether allowed group names is configured
     */
    public boolean isAllowedGroupNamesConfigured() {
        return Optional.of(this.getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupNames)
                       .map(allowedGroupNames -> !allowedGroupNames.isEmpty())
                       .orElse(false);
    }

    /**
     * Whether allowed group IDs is configured.
     *
     * @return whether allowed group IDs is configured
     */
    public boolean isAllowedGroupIdsConfigured() {
        return Optional.of(this.getUserGroup())
                       .map(UserGroupProperties::getAllowedGroupIds)
                       .map(allowedGroupIds -> !allowedGroupIds.isEmpty())
                       .orElse(false);
    }

    /**
     * Gets the user group properties.
     *
     * @return the user group properties
     */
    public UserGroupProperties getUserGroup() {
        return userGroup;
    }

    /**
     * Gets the username attribute.
     *
     * @return the username attribute
     */
    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    /**
     * Sets the username attribute.
     *
     * @param userNameAttribute the username attribute
     */
    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    /**
     * Gets the redirect URI template.
     *
     * @return the redirect URI template
     */
    public String getRedirectUriTemplate() {
        return redirectUriTemplate;
    }

    /**
     * Sets the redirect URI template.
     *
     * @param redirectUriTemplate the redirect URI template
     */
    public void setRedirectUriTemplate(String redirectUriTemplate) {
        this.redirectUriTemplate = redirectUriTemplate;
    }

    /**
     * Gets the App ID URI.
     *
     * @return the App ID URI
     */
    public String getAppIdUri() {
        return appIdUri;
    }

    /**
     * Sets the App ID URI.
     *
     * @param appIdUri the App ID URI
     */
    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }

    /**
     * Gets the additional authenticate parameters.
     *
     * @return the additional authenticate parameters
     */
    public Map<String, Object> getAuthenticateAdditionalParameters() {
        return authenticateAdditionalParameters;
    }

    /**
     * Gets the JWT connect timeout.
     *
     * @return the JWT connect timeout
     */
    public Duration getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    /**
     * Sets the JWT connect timeout.
     *
     * @param jwtConnectTimeout the JWT connect timeout
     */
    public void setJwtConnectTimeout(Duration jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    /**
     * Gets the JWT read timeout.
     *
     * @return the JWT read timeout
     */
    public Duration getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    /**
     * Sets the JWT read timeout.
     *
     * @param jwtReadTimeout the JWT read timeout
     */
    public void setJwtReadTimeout(Duration jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    /**
     * Gets the JWT size limit.
     *
     * @return the JWT size limit
     */
    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    /**
     * Sets the JWT size limit.
     *
     * @param jwtSizeLimit the JWT size limit
     */
    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    /**
     * Gets the JWK set cache lifespan.
     *
     * @return the JWK set cache lifespan
     */
    public Duration getJwkSetCacheLifespan() {
        return jwkSetCacheLifespan;
    }

    /**
     * Sets the JWK set cache lifespan.
     *
     * @param jwkSetCacheLifespan the JWT set cache lifespan
     */
    public void setJwkSetCacheLifespan(Duration jwkSetCacheLifespan) {
        this.jwkSetCacheLifespan = jwkSetCacheLifespan;
    }

    /**
     * Gets the JWK set cache refresh time.
     *
     * @return the JWK set cache refresh time
     */
    public Duration getJwkSetCacheRefreshTime() {
        return jwkSetCacheRefreshTime;
    }

    /**
     * Sets the JWK set cache refresh time.
     *
     * @param jwkSetCacheRefreshTime the JWK set cache refresh time
     */
    public void setJwkSetCacheRefreshTime(Duration jwkSetCacheRefreshTime) {
        this.jwkSetCacheRefreshTime = jwkSetCacheRefreshTime;
    }

    /**
     * Gets the post logout redirect URI.
     *
     * @return the post logout redirect URI
     */
    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    /**
     * Set the post logout redirect URI.
     *
     * @param postLogoutRedirectUri the post logout redirect URI
     */
    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    /**
     * Whether the session is stateless.
     *
     * @return whether the session is stateless
     */
    public Boolean getSessionStateless() {
        return sessionStateless;
    }

    /**
     * Sets whether the session is stateless.
     *
     * @param sessionStateless whether the session is stateless
     */
    public void setSessionStateless(Boolean sessionStateless) {
        this.sessionStateless = sessionStateless;
    }

    /**
     * @return Graph membership uri.
     */
    public String getGraphMembershipUri() {
        return getProfile().getEnvironment().getMicrosoftGraphEndpoint()
            + (getUserGroup().isUseTransitiveMembers()
            ? "v1.0/me/transitiveMemberOf"
            : "v1.0/me/memberOf");
    }

    /**
     * Gets the authorization clients.
     *
     * @return the authorization clients
     */
    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    /**
     * Whether the group is allowed.
     *
     * @param group the group
     * @return whether the group is allowed
     */
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

    /**
     * Set after properties.
     */
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

    /**
     * Validate configured application type or set default value.
     *
     * @throws IllegalStateException Invalid property 'spring.cloud.azure.active-directory.application-type'
     */
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
        String grantType = decideGrantType(registrationId, properties, applicationType);
        if (grantType != null) {
            // Validate authorization grant grantType
            validateAuthorizationGrantType(registrationId, grantType, properties);
        }

        // Validate scopes.
        List<String> scopes = validateScopes(registrationId, properties);

        addNecessaryScopesForAuhtorizationCodeClients(properties, scopes);
    }

    /**
     * Add necessary scopes for authorization_code clients.
     *
     * https://docs.microsoft.com/graph/permissions-reference#remarks-17
     * https://docs.microsoft.com/azure/active-directory/develop/v2-permissions-and-consent#openid-connect-scopes
     *
     * @param properties AuthorizationClientProperties
     * @param scopes scopes for authorization_code clients.
     */
    private void addNecessaryScopesForAuhtorizationCodeClients(AuthorizationClientProperties properties,
                                                               List<String> scopes) {
        if (properties.getAuthorizationGrantType().getValue().equals(AUTHORIZATION_CODE.getValue())) {
            if (!scopes.contains("openid")) {
                scopes.add("openid"); // "openid" allows to request an ID token.
            }
            if (!scopes.contains("profile")) {
                scopes.add("profile"); // "profile" allows returning additional claims in the ID token.
            }
            if (!scopes.contains("offline_access")) {
                scopes.add("offline_access"); // "offline_access" allows to request a refresh token.
            }
        }
    }

    private List<String> validateScopes(String registrationId, AuthorizationClientProperties properties) {
        List<String> scopes = properties.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalStateException(
                "'spring.cloud.azure.active-directory.authorization-clients." + registrationId + ".scopes' must be "
                    + "configured");
        }
        return scopes;
    }



    private void validateAuthorizationGrantType(String registrationId, String grantType,
                                                AuthorizationClientProperties properties) {

        if (grantTypeValidateMap.containsKey(applicationType)) {
            if (grantTypeValidateMap.get(applicationType).contains(grantType)) {
                throw new IllegalStateException(String.format(UNMATCHING_OAUTH_GRANT_TYPE_FROMAT,
                    applicationType.getValue(), registrationId, grantType));
            }
            LOGGER.debug("'spring.cloud.azure.active-directory.authorization-clients.{}.authorization-grant-type'"
                + " is valid.", registrationId);
        }

        if (AZURE_CLIENT_REGISTRATION_ID.equals(registrationId)
            && AUTHORIZATION_CODE != properties.getAuthorizationGrantType()) {
            throw new IllegalStateException("spring.cloud.azure.active-directory.authorization-clients."
                + AZURE_CLIENT_REGISTRATION_ID
                + ".authorization-grant-type must be configured to 'authorization_code'.");
        }
    }

    private String decideGrantType(String registrationId,
                                   AuthorizationClientProperties properties, AadApplicationType appType) {
        String grantType = Optional.of(properties)
                                   .map(AuthorizationClientProperties::getAuthorizationGrantType)
                                   .map(AadAuthorizationGrantType::getValue)
                                   .orElse(null);
        if (grantType == null) {
            switch (appType) {
                case WEB_APPLICATION:
                    if (registrationId.equals(AZURE_CLIENT_REGISTRATION_ID)) {
                        properties.setAuthorizationGrantType(AUTHORIZATION_CODE);
                    } else {
                        properties.setAuthorizationGrantType(AZURE_DELEGATED);
                    }
                    LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to "
                        + "'authorization_code'.", registrationId);
                    break;
                case RESOURCE_SERVER:
                case RESOURCE_SERVER_WITH_OBO:
                    properties.setAuthorizationGrantType(AadAuthorizationGrantType.ON_BEHALF_OF);
                    LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to 'on_behalf_of'.", registrationId);
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                    throw new IllegalStateException("spring.cloud.azure.active-directory.authorization-clients." + registrationId
                        + ".authorization-grant-grantType must be configured. ");
                default:
                    throw new IllegalStateException("Unsupported authorization grantType " + appType.getValue());
            }
        }

        return grantType;
    }

    private boolean isMultiTenantsApplication(String tenantId) {
        return "common".equals(tenantId) || "organizations".equals(tenantId) || "consumers".equals(tenantId);
    }
}
