/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements Serializable {
    private static final long serialVersionUID = -3725690847771476854L;

    private JWSObject jwsObject;
    private JWTClaimsSet jwtClaimsSet;
    private List<UserGroup> userGroups = new ArrayList<>();

    public UserPrincipal(JWSObject jwsObject, JWTClaimsSet jwtClaimsSet) {
        this.jwsObject = jwsObject;
        this.jwtClaimsSet = jwtClaimsSet;
    }

    // claimset
    public String getIssuer() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getIssuer();
    }

    public String getSubject() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getSubject();
    }

    public Map<String, Object> getClaims() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaims();
    }

    public Object getClaim() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaim("tid");
    }

    public Object getClaim(String name) {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaim(name);
    }

    public String getUpn() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("upn");
    }

    public String getUniqueName() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("unique_name");
    }

    public String getName() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("name");
    }

    // header
    public String getKid() {
        return jwsObject == null ? null : jwsObject.getHeader().getKeyID();
    }

    public void setUserGroups(List<UserGroup> groups) {
        this.userGroups = groups;
    }

    public List<UserGroup> getUserGroups() {
        return this.userGroups;
    }

    public boolean isMemberOf(UserGroup group) {
        return !(userGroups == null || userGroups.isEmpty()) && userGroups.contains(group);
    }
}

