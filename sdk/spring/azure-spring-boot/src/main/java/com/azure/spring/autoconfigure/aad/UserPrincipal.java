// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * entity class of UserPrincipal
 */
public class UserPrincipal implements Serializable {
    private static final long serialVersionUID = -3725690847771476854L;

    private String aadIssuedBearerToken; // id_token or access_token

    private final JWSObject jwsObject;

    private final JWTClaimsSet jwtClaimsSet;

    /**
     * All groups in aadIssuedBearerToken. Including the ones not exist in aadAuthenticationProperties.getUserGroup()
     * .getAllowedGroups()
     */
    private Set<String> groups;

    /**
     * All roles in aadIssuedBearerToken.
     */
    private Set<String> roles;

    private String accessTokenForGraphApi;

    public UserPrincipal(String aadIssuedBearerToken, JWSObject jwsObject, JWTClaimsSet jwtClaimsSet) {
        this.aadIssuedBearerToken = aadIssuedBearerToken;
        this.jwsObject = jwsObject;
        this.jwtClaimsSet = jwtClaimsSet;
    }

    public String getAadIssuedBearerToken() {
        return aadIssuedBearerToken;
    }

    public void setAadIssuedBearerToken(String aadIssuedBearerToken) {
        this.aadIssuedBearerToken = aadIssuedBearerToken;
    }

    public Set<String> getGroups() {
        return this.groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getAccessTokenForGraphApi() {
        return accessTokenForGraphApi;
    }

    public void setAccessTokenForGraphApi(String accessTokenForGraphApi) {
        this.accessTokenForGraphApi = accessTokenForGraphApi;
    }

    public boolean isMemberOf(AADAuthenticationProperties aadAuthenticationProperties, String group) {
        return aadAuthenticationProperties.isAllowedGroup(group)
            && Optional.of(groups)
                       .map(g -> g.contains(group))
                       .orElse(false);
    }

    public String getKid() {
        return jwsObject == null ? null : jwsObject.getHeader().getKeyID();
    }

    public String getIssuer() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getIssuer();
    }

    public String getSubject() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getSubject();
    }

    public Map<String, Object> getClaims() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaims();
    }

    public Object getClaim(String name) {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaim(name);
    }
}

