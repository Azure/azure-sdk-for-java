// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

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
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(AZURE_IDENTITY_TEST_PLATFORM))) {
            throw new IllegalStateException("Identity Test platform is not set. Set environemnt "
                                                               + "variable AZURE_IDENTITY_TEST_PLATFORM to webjobs");
        }

        String platform = CONFIGURATION.get(AZURE_IDENTITY_TEST_PLATFORM).toLowerCase(Locale.ENGLISH);
        switch (platform) {
            case "webjobs":
                WebJobsIdentityTest webJobsIdentityTest  = new WebJobsIdentityTest();
                webJobsIdentityTest.run();
                break;
            case "multitenant":
                MultiTenantTest multiTenantTest  = new MultiTenantTest();
                multiTenantTest.run();
                break;
            default:
                throw (new IllegalStateException("Invalid Test Platform is configured for AZURE_IDENTITY_TEST_PLATFORM."
                                                                               + "Possible value is webjobs."));
        }
    }
}
