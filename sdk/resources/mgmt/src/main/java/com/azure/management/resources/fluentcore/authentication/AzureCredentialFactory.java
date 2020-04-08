// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.authentication;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for authentication.
 */
public class AzureCredentialFactory {

    private AzureCredentialFactory() {
    }

    /**
     * Creates the credential by environment variables
     * @return the credential.
     */
    public static AzureTokenCredential fromEnvironment() {
        return new AzureTokenCredentialBuilder()
            .build();
    }

    /**
     * Creates the credential by running the installed Azure CLI
     * @return the credential.
     */
    public static AzureTokenCredential fromAzureCLI() {
        //TODO check if it is required to override AzureCliCredential
        return null;
    }

    /**
     * Creates the credential by the authentication file
     * @return the credential.
     */
    public static AzureTokenCredential fromFile(File authFile) throws IOException {
        return AuthFile.parse(authFile).generateCredential();
    }
}
