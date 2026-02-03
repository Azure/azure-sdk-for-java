// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.util.Configuration;

/**
 * Set of environment variables shared in confidential ledger tests.
 */
public class ConfidentialLedgerEnvironment {

    private static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    public static String getConfidentialLedgerName() {
        return GLOBAL_CONFIGURATION.get("CONFIDENTIALLEDGER_NAME", "java-sdk-live-tests-ledger");
    }

    public static String getConfidentialLedgerUrl() {
        return GLOBAL_CONFIGURATION.get("CONFIDENTIALLEDGER_URL",
            "https://" + getConfidentialLedgerName() + ".confidential-ledger.azure.com");
    }

    public static String getConfidentialLedgerIdentityUrl() {
        return GLOBAL_CONFIGURATION.get("CONFIDENTIALLEDGER_IDENTITY_URL",
            "https://identity.confidential-ledger.core.azure.com");
    }

    public static String getConfidentialLedgerAdminOid() {
        return GLOBAL_CONFIGURATION.get("CONFIDENTIALLEDGER_CLIENT_OBJECTID", "46db960a-85e3-447f-b143-590ebea1d752");
    }
}
