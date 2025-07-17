// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import com.azure.v2.identity.models.AuthenticationRecord;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITenantProfile;

import java.util.Map;

/**
 * Represents the Msal authentication account and offers support to hold the authentication record details
 * in memory for public client flows.
 */
public class MsalAuthenticationAccount implements IAccount {
    private static final long serialVersionUID = 7563908089175663756L;
    private transient AuthenticationRecord authenticationRecord;
    private final Map<String, ITenantProfile> tenantProfiles;
    private final String homeAccountId;
    private final String environment;
    private final String username;

    /**
     * Creates an instance of the authentication record.
     *
     * @param authenticationRecord the authentication record
     */
    public MsalAuthenticationAccount(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
        this.homeAccountId = authenticationRecord.getHomeAccountId();
        this.environment = authenticationRecord.getAuthority();
        this.username = authenticationRecord.getUsername();
        this.tenantProfiles = null;
    }

    /**
     * Creates an instance of Msal authentication account.
     *
     * @param authenticationRecord the authentication record
     * @param tenantProfiles the tenant profiles
     */
    public MsalAuthenticationAccount(AuthenticationRecord authenticationRecord,
        Map<String, ITenantProfile> tenantProfiles) {
        this.authenticationRecord = authenticationRecord;
        this.tenantProfiles = tenantProfiles;
        this.homeAccountId = authenticationRecord.getHomeAccountId();
        this.environment = authenticationRecord.getAuthority();
        this.username = authenticationRecord.getUsername();
    }

    @Override
    public String homeAccountId() {
        return homeAccountId;
    }

    @Override
    public String environment() {
        return environment;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public Map<String, ITenantProfile> getTenantProfiles() {
        return tenantProfiles;
    }

    /**
     * Gets the authentication record.
     *
     * @return the authentication record
     */
    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
    }
}
