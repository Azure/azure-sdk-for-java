// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for Azure Active Directory Authentication.
 */
@Validated
@ConfigurationProperties("azure.activedirectory")
public class AADAuthenticationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADAuthenticationProperties.class);
    private static final String DEFAULT_SERVICE_ENVIRONMENT = "global";
    private static final long DEFAULT_JWK_SET_CACHE_LIFESPAN = TimeUnit.MINUTES.toMillis(5);

    /**
     * Default UserGroup configuration.
     */
    private UserGroupProperties userGroup = new UserGroupProperties();


    /**
     * Azure service environment/region name, e.g., cn, global
     */
    private String environment = DEFAULT_SERVICE_ENVIRONMENT;
    /**
     * Registered application ID in Azure AD.
     * Must be configured when OAuth2 authentication is done in front end
     */
    private String clientId;
    /**
     * API Access Key of the registered application.
     * Must be configured when OAuth2 authentication is done in front end
     */
    private String clientSecret;

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
     * Azure Tenant ID.
     */
    private String tenantId;

    /**
     * If Telemetry events should be published to Azure AD.
     */
    private boolean allowTelemetry = true;

    /**
     * If <code>true</code> activates the stateless auth filter {@link AADAppRoleStatelessAuthenticationFilter}.
     * The default is <code>false</code> which activates {@link AADAuthenticationFilter}.
     */
    private Boolean sessionStateless = false;

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

        /**
         * Key of the JSON Node to get from the Azure AD response object that will be checked to contain the {@code
         * azure.activedirectory.user-group.value}  to signify that this node is a valid {@code UserGroup}.
         */
        @NotEmpty
        private String key = "objectType";

        /**
         * Value of the JSON Node identified by the {@code azure.activedirectory.user-group.key} to validate the JSON
         * Node is a UserGroup.
         */
        @NotEmpty
        private String value = Constants.OBJECT_TYPE_GROUP;

        /**
         * Key of the JSON Node containing the Azure Object ID for the {@code UserGroup}.
         */
        @NotEmpty
        private String objectIDKey = "objectId";

        public List<String> getAllowedGroups() {
            return allowedGroups;
        }

        public void setAllowedGroups(List<String> allowedGroups) {
            this.allowedGroups = allowedGroups;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getObjectIDKey() {
            return objectIDKey;
        }

        public void setObjectIDKey(String objectIDKey) {
            this.objectIDKey = objectIDKey;
        }

        @Override
        public String toString() {
            return "UserGroupProperties{"
                +  "allowedGroups=" + allowedGroups
                +  ", key='" + key + '\''
                +  ", value='" + value + '\''
                +  ", objectIDKey='" + objectIDKey + '\''
                +  '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserGroupProperties that = (UserGroupProperties) o;
            return Objects.equals(allowedGroups, that.allowedGroups)
                && Objects.equals(key, that.key)
                && Objects.equals(value, that.value)
                && Objects.equals(objectIDKey, that.objectIDKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowedGroups, key, value, objectIDKey);
        }
    }


    /**
     * Validates at least one of the user group properties are populated.
     */
    @PostConstruct
    public void validateUserGroupProperties() {
        if (this.sessionStateless) {
            if (!this.getUserGroup().getAllowedGroups().isEmpty()) {
                LOGGER.warn("Group names are not supported if you set 'sessionSateless' to 'true'.");
            }
        } else if (this.getUserGroup().getAllowedGroups().isEmpty()) {
            throw new IllegalStateException("One of the User Group Properties must be populated. "
                + "Please populate azure.activedirectory.user-group.allowed-groups");
        }
    }

    public UserGroupProperties getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroupProperties userGroup) {
        this.userGroup = userGroup;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
}
