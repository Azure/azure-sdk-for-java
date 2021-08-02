package com.azure.spring.aad.webapi;

import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.aad.implementation.constants.AuthorityPrefix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties("azure.activedirectory.resource-server")
public class AADResourceServerProperties implements InitializingBean {

    public static final Map<String, String> DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

    static {
        Map<String, String> claimAuthorityMap = new HashMap<>();
        claimAuthorityMap.put(AADTokenClaim.SCP, AuthorityPrefix.SCOPE);
        claimAuthorityMap.put(AADTokenClaim.ROLES, AuthorityPrefix.APP_ROLE);
        DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP = Collections.unmodifiableMap(claimAuthorityMap);
    }

    private String principalClaimName;
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
