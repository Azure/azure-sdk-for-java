// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

/**
 * Utility class for validating AAD JWT issuer claims.
 * Provides centralized issuer validation logic to prevent duplication and ensure consistency.
 */
final class AadIssuerValidator {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private AadIssuerValidator() {
        // Utility class, not instantiable
    }

    /**
     * Checks if the given issuer is a valid AAD issuer.
     *
     * @param issuer the issuer string to validate
     * @return true if the issuer is a valid AAD issuer, false otherwise
     */
    static boolean isValidAadIssuer(String issuer) {
        if (issuer == null) {
            return false;
        }
        return issuer.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER)
            || issuer.startsWith(STS_WINDOWS_ISSUER)
            || issuer.startsWith(STS_CHINA_CLOUD_API_ISSUER);
    }
}
