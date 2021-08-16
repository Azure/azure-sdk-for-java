// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.AuthenticationRecord;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITenantProfile;

import java.util.Map;

public class MsalAuthenticationAccount implements IAccount {
    private static final long serialVersionUID = 7563908089175663756L;
    private transient AuthenticationRecord authenticationRecord;
    private Map<String, ITenantProfile> tenantProfiles;
    private String homeAccountId;
    private String environment;
    private String username;


    public MsalAuthenticationAccount(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
        this.homeAccountId = authenticationRecord.getHomeAccountId();
        this.environment = authenticationRecord.getAuthority();
        this.username = authenticationRecord.getUsername();
    }

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

    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
    }
}
