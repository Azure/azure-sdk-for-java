// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.management;

import com.microsoft.azure.credentials.AzureTokenCredentials;

public interface Access {

    AzureTokenCredentials credentials();

    String subscription();

    String tenantId();

}
