// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.aad.implementation.constants.AuthorityPrefix;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for Azure Active Directory B2C.
 */
@ConfigurationProperties("azure.activedirectory.resource-server")
public class AADResourceServerProperties implements InitializingBean {

    public static final Map<String, String> DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

    static {
        Map<String, String> claimAuthorityMap = new HashMap<>();
        claimAuthorityMap.put(AADTokenClaim.SCP, AuthorityPrefix.SCOPE);
        claimAuthorityMap.put(AADTokenClaim.ROLES, AuthorityPrefix.APP_ROLE);
        DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP = Collections.unmodifiableMap(claimAuthorityMap);
    }

    /**
     * <pre>
     * Configure which claim in access token be returned in AuthenticatedPrincipal#getName.
     * Default value is "sub".
     *
     * Example:
     * If use the default value, and the access_token's "sub" scope value is "testValue",
     * then AuthenticatedPrincipal#getName will return "testValue".
     * </pre>
     * @see org.springframework.security.core.AuthenticatedPrincipal#getName
     */
    private String principalClaimName;
    /**
     * <pre>
     * Configure which claim will be used to build GrantedAuthority, and prefix of the GrantedAuthority's string value.
     * Default value is: "scp" -> "SCOPE_", "roles" -> "APPROLE_".
     *
     * Example:
     * If use the default value, and the access_token's "scp" scope value is "testValue",
     * then GrantedAuthority with "SCOPE_testValue" will be created..
     * </pre>
     * @see org.springframework.security.core.GrantedAuthority
     */
    private Map<String, String> claimToAuthorityPrefixMap;

    public String getPrincipalClaimName() {
        return principalClaimName;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        this.principalClaimName = principalClaimName;
    }

    public Map<String, String> getClaimToAuthorityPrefixMap() {
        return claimToAuthorityPrefixMap;
    }

    public void setClaimToAuthorityPrefixMap(Map<String, String> claimToAuthorityPrefixMap) {
        this.claimToAuthorityPrefixMap = claimToAuthorityPrefixMap;
    }

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(principalClaimName)) {
            principalClaimName = AADTokenClaim.SUB;
        }
        if (claimToAuthorityPrefixMap == null || claimToAuthorityPrefixMap.isEmpty()) {
            claimToAuthorityPrefixMap = DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
        }
    }

}
