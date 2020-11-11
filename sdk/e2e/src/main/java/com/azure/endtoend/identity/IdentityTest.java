// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.util.Configuration;

import java.util.Locale;

/**
 * Runs Identity end to end test on specified Azure Platform.
 */
public class IdentityTest {
    private static final String AZURE_IDENTITY_TEST_PLATFORM = "AZURE_IDENTITY_TEST_PLATFORM";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();

    /**
     * Runs the Web jobs identity tests
     * @param args the command line arguments
     * @throws IllegalStateException if AZURE_IDENTITY_TEST_PLATFORM is not set to "user" or "system"
     */
    public static void main(String[] args) throws IllegalStateException {
        try {
            String platform = CONFIGURATION.get(AZURE_IDENTITY_TEST_PLATFORM, "").toLowerCase(Locale.ENGLISH);
            switch (platform) {
                case "webjobs":
                    WebJobsIdentityTest webJobsIdentityTest = new WebJobsIdentityTest();
                    webJobsIdentityTest.run();
                    break;
                case "multitenant":
                    MultiTenantTest multiTenantTest = new MultiTenantTest();
                    multiTenantTest.run();
                    break;
                default:
                    ManagedIdentityCredentialTest managedIdentityCredentialTest = new ManagedIdentityCredentialTest();
                    managedIdentityCredentialTest.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
