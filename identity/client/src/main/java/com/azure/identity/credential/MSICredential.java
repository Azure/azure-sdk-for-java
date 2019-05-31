// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.credential.msi.MSIResourceType;
import com.azure.identity.implementation.MSIToken;
import com.azure.identity.implementation.RefreshableTokenCredential;

public abstract class MSICredential extends RefreshableTokenCredential<MSIToken> {
    MSICredential() {
        super();
    }

    public static AppServiceMSICredential appService() {
        return new AppServiceMSICredential();
    }

    public static VirtualMachineMSICredential virtualMachine() {
        return new VirtualMachineMSICredential();
    }

    /**
     * @return the type of the Azure resource this MSI credential is created for.
     */
    public abstract MSIResourceType resourceType();

    @Override
    protected String getTokenFromAuthResult(MSIToken authResult) {
        return authResult.accessToken();
    }

    @Override
    protected boolean isExpired(MSIToken authResult) {
        return authResult.isExpired();
    }
}
