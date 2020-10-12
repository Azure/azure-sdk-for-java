// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserPrincipal implements Serializable {
    private static final long serialVersionUID = -3725690847771476854L;

    private final JWSObject jwsObject;
    private final JWTClaimsSet jwtClaimsSet;
    private List<UserGroup> userGroups = new ArrayList<>();
    private String aadIssuedBearerToken; // id_token or access_token
    private String accessTokenForGraphApi;

    public UserPrincipal(String aadIssuedBearerToken, JWSObject jwsObject, JWTClaimsSet jwtClaimsSet) {
        this.aadIssuedBearerToken = aadIssuedBearerToken;
        this.jwsObject = jwsObject;
        this.jwtClaimsSet = jwtClaimsSet;
    }

    public List<UserGroup> getUserGroups() {
        return this.userGroups;
    }

    public void setUserGroups(List<UserGroup> groups) {
        this.userGroups = groups;
    }

    public String getAadIssuedBearerToken() {
        return aadIssuedBearerToken;
    }

    public void setAadIssuedBearerToken(String aadIssuedBearerToken) {
        this.aadIssuedBearerToken = aadIssuedBearerToken;
    }

    public String getAccessTokenForGraphApi() {
        return accessTokenForGraphApi;
    }

    public void setAccessTokenForGraphApi(String accessTokenForGraphApi) {
        this.accessTokenForGraphApi = accessTokenForGraphApi;
    }

    public boolean isMemberOf(UserGroup group) {
        return Optional.ofNullable(userGroups)
                       .filter(groups -> groups.contains(group))
                       .isPresent();
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

