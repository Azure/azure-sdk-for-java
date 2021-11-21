// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

/**
 * Entity class of AADOAuth2AuthenticatedPrincipal
 */
public class AADOAuth2AuthenticatedPrincipal implements OAuth2AuthenticatedPrincipal, Serializable {

    private static final long serialVersionUID = -3625690847771476854L;

    private static final String PERSONAL_ACCOUNT_TENANT_ID = "9188040d-6c67-4c5b-b112-36a304b66dad";

    private final Collection<GrantedAuthority> authorities;

    private final Map<String, Object> headers;

    private final Map<String, Object> attributes;

    private final String tokenValue;

    private JWTClaimsSet jwtClaimsSet;

    private final String name;

    public AADOAuth2AuthenticatedPrincipal(Map<String, Object> headers,
                                           Map<String, Object> attributes,
                                           Collection<GrantedAuthority> authorities,
                                           String tokenValue,
                                           String name) {
        Assert.notEmpty(attributes, "attributes cannot be empty");
        Assert.notEmpty(headers, "headers cannot be empty");
        this.headers = headers;
        this.tokenValue = tokenValue;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.authorities = authorities == null ? NO_AUTHORITIES : Collections.unmodifiableCollection(authorities);
        this.name = name;
        toJwtClaimsSet(attributes);
    }

    private void toJwtClaimsSet(Map<String, Object> attributes) {
        JWTClaimsSet.Builder builder = new Builder();
        for (Entry<String, Object> entry : attributes.entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        this.jwtClaimsSet = builder.build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public JWTClaimsSet getJwtClaimsSet() {
        return jwtClaimsSet;
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

    public String getTenantId() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("tid");
    }

    public boolean isPersonalAccount() {
        return PERSONAL_ACCOUNT_TENANT_ID.equals(getTenantId());
    }

}
