/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;

import java.io.IOException;

public class AzureTestCredentials extends ApplicationTokenCredentials {
    public AzureTestCredentials() {
        super("", "", "", AzureEnvironment.AZURE);
    }

    @Override
    public String getToken(String resource) throws IOException {
        if (!MockIntegrationTestBase.IS_MOCKED) {
            super.getToken(resource);
        }
        return "https:/asdd.com";
    }
}
