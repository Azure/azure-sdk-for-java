// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredential;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;

import java.util.Locale;

public class CliToolsTest {
    private static final String AZURE_CLI_TOOLS_TEST_MODE = "AZURE_CLI_TOOLS_TEST_MODE";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();
    private static final ClientLogger logger = new ClientLogger(CliToolsTest.class);
    private static final String errorString = "Test mode is not set. Set environment variable AZURE_CLI_TOOLS_TEST_MODE to azd, azcli, or azps";
    public void run() {

        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(AZURE_CLI_TOOLS_TEST_MODE))) {
            throw logger.logExceptionAsError(new IllegalStateException(errorString));
        }


        String mode = CONFIGURATION.get(AZURE_CLI_TOOLS_TEST_MODE).toLowerCase(Locale.ENGLISH);
        switch (mode) {
            case "azd":
                testAzd();
                break;
            case "azcli":
                testAzCli();
                break;
            case "azps":
                testAzPs();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalStateException(errorString));
        }
    }

    private void testAzd() {
        AzureDeveloperCliCredential credential = new AzureDeveloperCliCredentialBuilder().build();
        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");
        AccessToken tokenCredential = credential.getToken(tokenRequestContext).block();
        if (tokenCredential == null) {
            System.out.println("Failed to get token from AzureDeveloperCliCredential");
        } else {
            System.out.println("Successfully got token from AzureDeveloperCliCredential");
        }
    }

    private void testAzCli() {
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");
        AccessToken tokenCredential = credential.getToken(tokenRequestContext).block();
        if (tokenCredential == null) {
            System.out.println("Failed to get token from AzureCliCredential");
        } else {
            System.out.println("Successfully got token from AzureCliCredential");
        }
    }

    private void testAzPs() {
        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");
        AccessToken tokenCredential = credential.getToken(tokenRequestContext).block();
        if (tokenCredential == null) {
            System.out.println("Failed to get token from AzurePowerShellCredential");
        } else {
            System.out.println("Successfully got token from AzurePowerShellCredential");
        }
    }
}
