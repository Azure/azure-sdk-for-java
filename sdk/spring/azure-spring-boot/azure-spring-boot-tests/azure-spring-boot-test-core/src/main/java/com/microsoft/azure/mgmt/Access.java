/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.credentials.AzureTokenCredentials;

public interface Access {

    AzureTokenCredentials credentials();
    
    String subscription();
    
    String servicePrincipal();

}
