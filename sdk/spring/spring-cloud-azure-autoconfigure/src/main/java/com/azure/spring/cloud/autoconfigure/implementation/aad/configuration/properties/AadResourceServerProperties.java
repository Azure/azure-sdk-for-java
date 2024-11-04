// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AuthorityPrefix;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AadResourceServerProperties implements InitializingBean {

    /**
     * Properties prefix.
     */
    public static final String PREFIX = "spring.cloud.azure.active-directory.resource-server";

    /**
     * Default claim to authority prefix map
     */
    public static final Map<String, String> DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

    static {
        Map<String, String> claimAuthorityMap = new HashMap<>();
        claimAuthorityMap.put(AadJwtClaimNames.SCP, AuthorityPrefix.SCOPE);
        claimAuthorityMap.put(AadJwtClaimNames.ROLES, AuthorityPrefix.APP_ROLE);
        DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP = Collections.unmodifiableMap(claimAuthorityMap);
    }

    /**
     *
     * Configure which claim in access token be returned in AuthenticatedPrincipal#getName. Example: If use the default value, and the access_token's "sub" scope value is "testValue", then AuthenticatedPrincipal#getName will return "testValue". The default value is `"sub"`.
     */
    private String principalClaimName;

    /**
     * Configure which claim will be used to build GrantedAuthority, and prefix of the GrantedAuthority's string value. Example: If use the default value, and the access_token's "scp" scope value is "testValue", then GrantedAuthority with "SCOPE_testValue" will be created. The default value is `"scp" -> "SCOPE_", "roles" -> "APPROLE_"`.
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
            principalClaimName = AadJwtClaimNames.SUB;
        }
        if (claimToAuthorityPrefixMap == null || claimToAuthorityPrefixMap.isEmpty()) {
            claimToAuthorityPrefixMap = DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
        }
    }

}
