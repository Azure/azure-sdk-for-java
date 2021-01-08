// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for Azure Active Directory Authentication.
 */
@Validated
@ConfigurationProperties("azure.activedirectory")
public class AADAuthenticationProperties implements InitializingBean {

    private static final long DEFAULT_JWK_SET_CACHE_LIFESPAN = TimeUnit.MINUTES.toMillis(5);
    private static final long DEFAULT_JWK_SET_CACHE_REFRESH_TIME = DEFAULT_JWK_SET_CACHE_LIFESPAN;

    /**
     * Default UserGroup configuration.
     */
    private UserGroupProperties userGroup = new UserGroupProperties();

    /**
     * Registered application ID in Azure AD. Must be configured when OAuth2 authentication is done in front end
     */
    private String clientId;

    /**
     * API Access Key of the registered application. Must be configured when OAuth2 authentication is done in front end
     */
    private String clientSecret;

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
     * Connection Timeout for the JWKSet Remote URL call.
     */
    private int jwtConnectTimeout = RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT; /* milliseconds */

    /**
     * Read Timeout for the JWKSet Remote URL call.
     */
    private int jwtReadTimeout = RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT; /* milliseconds */

    /**
     * Size limit in Bytes of the JWKSet Remote URL call.
     */
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT; /* bytes */

    /**
     * The lifespan of the cached JWK set before it expires, default is 5 minutes.
     */
    private long jwkSetCacheLifespan = DEFAULT_JWK_SET_CACHE_LIFESPAN;

    /**
     * The refresh time of the cached JWK set before it expires, default is 5 minutes.
     */
    private long jwkSetCacheRefreshTime = DEFAULT_JWK_SET_CACHE_REFRESH_TIME;

    /**
     * Azure Tenant ID.
     */
    private String tenantId = "common";

    private String postLogoutRedirectUri;

    /**
     * If Telemetry events should be published to Azure AD.
     */
    private boolean allowTelemetry = true;

    /**
     * If <code>true</code> activates the stateless auth filter {@link AADAppRoleStatelessAuthenticationFilter}. The
     * default is <code>false</code> which activates {@link AADAuthenticationFilter}.
     */
    private Boolean sessionStateless = false;

    private String baseUri;

    private String graphBaseUri;

    private String graphMembershipUri;

    private Map<String, AuthorizationClientProperties> authorizationClients = new HashMap<>();

    @DeprecatedConfigurationProperty(
        reason = "Configuration moved to UserGroup class to keep UserGroup properties together",
        replacement = "azure.activedirectory.user-group.allowed-groups")
    public List<String> getActiveDirectoryGroups() {
        return userGroup.getAllowedGroups();
    }

    /**
     * Properties dedicated to changing the behavior of how the groups are mapped from the Azure AD response. Depending
     * on the graph API used the object will not be the same.
     */
    public static class UserGroupProperties {

        /**
         * Expected UserGroups that an authority will be granted to if found in the response from the MemeberOf Graph
         * API Call.
         */
        private List<String> allowedGroups = new ArrayList<>();

        public List<String> getAllowedGroups() {
            return allowedGroups;
        }

        public void setAllowedGroups(List<String> allowedGroups) {
            this.allowedGroups = allowedGroups;
        }

    }

    public boolean allowedGroupsConfigured() {
        return Optional.of(this)
                       .map(AADAuthenticationProperties::getUserGroup)
                       .map(AADAuthenticationProperties.UserGroupProperties::getAllowedGroups)
                       .map(allowedGroups -> !allowedGroups.isEmpty())
                       .orElse(false);
    }

    public boolean isResourceServer() {
        return ClassUtils.isPresent(
            "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken",
            this.getClass().getClassLoader());
    }

    public boolean isWebApplication() {
        return ClassUtils.isPresent(
            "org.springframework.security.oauth2.client.registration.ClientRegistrationRepository",
            this.getClass().getClassLoader());
    }

    public UserGroupProperties getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroupProperties userGroup) {
        this.userGroup = userGroup;
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

    public String getRedirectUriTemplate() {
        return redirectUriTemplate;
    }

    public void setRedirectUriTemplate(String redirectUriTemplate) {
        this.redirectUriTemplate = redirectUriTemplate;
    }

    @Deprecated
    public void setActiveDirectoryGroups(List<String> activeDirectoryGroups) {
        this.userGroup.setAllowedGroups(activeDirectoryGroups);
    }

    public String getAppIdUri() {
        return appIdUri;
    }

    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }

    public int getJwtConnectTimeout() {
        return jwtConnectTimeout;
    }

    public void setJwtConnectTimeout(int jwtConnectTimeout) {
        this.jwtConnectTimeout = jwtConnectTimeout;
    }

    public int getJwtReadTimeout() {
        return jwtReadTimeout;
    }

    public void setJwtReadTimeout(int jwtReadTimeout) {
        this.jwtReadTimeout = jwtReadTimeout;
    }

    public int getJwtSizeLimit() {
        return jwtSizeLimit;
    }

    public void setJwtSizeLimit(int jwtSizeLimit) {
        this.jwtSizeLimit = jwtSizeLimit;
    }

    public long getJwkSetCacheLifespan() {
        return jwkSetCacheLifespan;
    }

    public void setJwkSetCacheLifespan(long jwkSetCacheLifespan) {
        this.jwkSetCacheLifespan = jwkSetCacheLifespan;
    }

    public long getJwkSetCacheRefreshTime() {
        return jwkSetCacheRefreshTime;
    }

    public void setJwkSetCacheRefreshTime(long jwkSetCacheRefreshTime) {
        this.jwkSetCacheRefreshTime = jwkSetCacheRefreshTime;
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

    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    public Boolean getSessionStateless() {
        return sessionStateless;
    }

    public void setSessionStateless(Boolean sessionStateless) {
        this.sessionStateless = sessionStateless;
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

    public String getGraphMembershipUri() {
        return graphMembershipUri;
    }

    public void setGraphMembershipUri(String graphMembershipUri) {
        this.graphMembershipUri = graphMembershipUri;
    }

    public Map<String, AuthorizationClientProperties> getAuthorizationClients() {
        return authorizationClients;
    }

    public void setAuthorizationClients(Map<String, AuthorizationClientProperties> authorizationClients) {
        this.authorizationClients = authorizationClients;
    }

    public boolean isAllowedGroup(String group) {
        return Optional.ofNullable(getUserGroup())
                       .map(UserGroupProperties::getAllowedGroups)
                       .orElseGet(Collections::emptyList)
                       .contains(group);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (!StringUtils.hasText(baseUri)) {
            baseUri = "https://login.microsoftonline.com/";
        } else {
            baseUri = addSlash(baseUri);
        }

        if (!StringUtils.hasText(graphBaseUri)) {
            graphBaseUri = "https://graph.microsoft.com/";
        } else {
            graphBaseUri = addSlash(graphBaseUri);
        }

        if (!StringUtils.hasText(graphMembershipUri)) {
            graphMembershipUri = graphBaseUri + "v1.0/me/memberOf";
        }

        if (!graphMembershipUri.startsWith(graphBaseUri)) {
            throw new IllegalStateException("azure.activedirectory.graph-base-uri should be "
                + "the prefix of azure.activedirectory.graph-membership-uri. "
                + "azure.activedirectory.graph-base-uri = " + graphBaseUri + ", "
                + "azure.activedirectory.graph-membership-uri = " + graphMembershipUri + ".");
        }
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
