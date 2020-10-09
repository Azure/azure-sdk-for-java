// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.AuthenticationRecord;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.ITenantProfile;

import java.util.Map;

public class MsalAuthenticationAccount implements IAccount {
    private AuthenticationRecord authenticationRecord;
    private Map<String, ITenantProfile> tenantProfiles;

    public MsalAuthenticationAccount(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
    }

    public MsalAuthenticationAccount(AuthenticationRecord authenticationRecord,
                                     Map<String, ITenantProfile> tenantProfiles) {
        this.authenticationRecord = authenticationRecord;
        this.tenantProfiles = tenantProfiles;
    }

    @Override
    public String homeAccountId() {
        return authenticationRecord.getHomeAccountId();
    }

    @Override
    public String environment() {
        return authenticationRecord.getAuthority();
    }

    @Override
    public String username() {
        return authenticationRecord.getUsername();
    }

    @Override
    public Map<String, ITenantProfile> getTenantProfiles() {
        return tenantProfiles;
    }

    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
    }
}
