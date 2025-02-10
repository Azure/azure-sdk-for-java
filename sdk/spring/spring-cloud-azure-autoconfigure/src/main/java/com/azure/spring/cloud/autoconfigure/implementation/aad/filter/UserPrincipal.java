// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
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

    private static final String PERSONAL_ACCOUNT_TENANT_ID = "9188040d-6c67-4c5b-b112-36a304b66dad";

    /**
     * The AAD issued bearer token
     */
    private final String aadIssuedBearerToken; // id_token or access_token

    /**
     * The JWS object
     */
    private final JWSObject jwsObject;

    /**
     * The JWT claims set
     */
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

    /**
     * The access token for Graph API
     */
    private String accessTokenForGraphApi;

    /**
     * Creates a new instance of {@link UserPrincipal}.
     *
     * @param aadIssuedBearerToken the AAD issued bearer token
     * @param jwsObject the JWS object
     * @param jwtClaimsSet the JWT claims set
     */
    public UserPrincipal(String aadIssuedBearerToken, JWSObject jwsObject, JWTClaimsSet jwtClaimsSet) {
        this.aadIssuedBearerToken = aadIssuedBearerToken;
        this.jwsObject = jwsObject;
        this.jwtClaimsSet = jwtClaimsSet;
    }

    /**
     * Gets the AAD issued bearer token.
     *
     * @return the AAD issued bearer token
     */
    public String getAadIssuedBearerToken() {
        return aadIssuedBearerToken;
    }

    /**
     * Gets the set of groups.
     *
     * @return the set of groups
     */
    public Set<String> getGroups() {
        return this.groups;
    }

    /**
     * Sets the set of groups.
     *
     * @param groups the set of groups
     */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    /**
     * Gets the set of roles.
     *
     * @return the set of roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Sets the set of roles.
     *
     * @param roles the set of roles
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Gets the access token for Graph API.
     *
     * @return the access token for Graph API
     */
    public String getAccessTokenForGraphApi() {
        return accessTokenForGraphApi;
    }

    /**
     * Sets the access token for Graph API.
     *
     * @param accessTokenForGraphApi the access token for Graph API
     */
    public void setAccessTokenForGraphApi(String accessTokenForGraphApi) {
        this.accessTokenForGraphApi = accessTokenForGraphApi;
    }

    /**
     * Whether the group is a member of the user principal.
     *
     * @param aadAuthenticationProperties the AAD authentication properties
     * @param group the group
     * @return whether the group is a member of the user principal
     */
    public boolean isMemberOf(AadAuthenticationProperties aadAuthenticationProperties, String group) {
        return aadAuthenticationProperties.isAllowedGroup(group)
            && Optional.of(groups)
                       .map(g -> g.contains(group))
                       .orElse(false);
    }

    /**
     * Gets the KID.
     *
     * @return the KID
     */
    public String getKeyId() {
        return jwsObject == null ? null : jwsObject.getHeader().getKeyID();
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
     * @param name the claim name
     * @return a claim
     */
    public Object getClaim(String name) {
        return jwtClaimsSet == null ? null : jwtClaimsSet.getClaim(name);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("name");
    }

    /**
     * gets the tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("tid");
    }

    /**
     * Gets the user principal name.
     *
     * @return the user principal name
     */
    public String getUserPrincipalName() {
        return jwtClaimsSet == null ? null : (String) jwtClaimsSet.getClaim("preferred_username");
    }

    /**
     * Whether the user principal is a personal account.
     *
     * @return whether the user principal is a personal account
     */
    public boolean isPersonalAccount() {
        return PERSONAL_ACCOUNT_TENANT_ID.equals(getTenantId());
    }
}

