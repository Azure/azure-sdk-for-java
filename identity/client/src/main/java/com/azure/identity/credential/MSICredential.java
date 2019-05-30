// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.TokenCredential;
import com.azure.identity.credential.msi.MSIResourceType;

public abstract class MSICredential extends TokenCredential {
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
}
