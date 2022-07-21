// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import com.azure.spring.cloud.autoconfigure.aad.AadResourceServerWebSecurityConfigurerAdapter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

/**
 * Entity class of AADOAuth2AuthenticatedPrincipal
 *
 * @deprecated use the default converter {@link JwtAuthenticationConverter} instead in {@link AadResourceServerWebSecurityConfigurerAdapter}.
 */
@Deprecated
public class AadOAuth2AuthenticatedPrincipal implements OAuth2AuthenticatedPrincipal, Serializable {

    private static final long serialVersionUID = -3625690847771476854L;

    private static final String PERSONAL_ACCOUNT_TENANT_ID = "9188040d-6c67-4c5b-b112-36a304b66dad";

    /**
     * The authorities
     */
    private final Collection<GrantedAuthority> authorities;

    /**
     * The headers
     */
    private final Map<String, Object> headers;

    /**
     * The attributes
     */
    private final Map<String, Object> attributes;

    /**
     * The token value
     */
    private final String tokenValue;

    /**
     * The JWT claims set
     */
    private JWTClaimsSet jwtClaimsSet;

    /**
     * The name
     */
    private final String name;

    /**
     * Creates a new instance of {@link AadOAuth2AuthenticatedPrincipal}.
     *
     * @param headers the headers
     * @param attributes the attributes
     * @param authorities the authorities
     * @param tokenValue the token value
     * @param name the name
     */
    public AadOAuth2AuthenticatedPrincipal(Map<String, Object> headers,
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
        Builder builder = new Builder();
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

    /**
     * Gets the token value.
     *
     * @return the token value
     */
    public String getTokenValue() {
        return tokenValue;
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * Gets the JWT claims set.
     *
     * @return the JWT claims set
     */
    public JWTClaimsSet getJwtClaimsSet() {
        return jwtClaimsSet;
    }

    /**
     * Gets the issuer.
     *
     * @return the issuer
     */
    public String getIssuer() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getIssuer();
    }

    /**
     * Gets the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getSubject();
    }

    /**
     * Gets the claims.
     *
     * @return the claims
     */
    public Map<String, Object> getClaims() {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaims();
    }

    /**
     * Gets a claim.
     *
     * @param name the name of the claim
     * @return a claim
     */
    public Object getClaim(String name) {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaim(name);
    }

    /**
     * Gets the tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("tid");
    }

    /**
     * Whether the principal is a personal account.
     *
     * @return whether the principal is a personal account
     */
    public boolean isPersonalAccount() {
        return PERSONAL_ACCOUNT_TENANT_ID.equals(getTenantId());
    }

}
