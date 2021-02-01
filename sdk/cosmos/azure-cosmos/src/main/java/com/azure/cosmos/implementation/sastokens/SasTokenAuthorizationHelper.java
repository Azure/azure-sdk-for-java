// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.sastokens;

import com.azure.cosmos.implementation.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used internally and act as a helper in authorization of
 * resources from SAS tokens.
 *
 */
public class SasTokenAuthorizationHelper {
    private static final Logger logger = LoggerFactory.getLogger(SasTokenAuthorizationHelper.class);

    /**
     * This method help to differentiate between master key and SAS token.
     *
     * @param token SAS token provided.
     * @return Whether given token is a SAS token or not.
     */
    public static boolean isSasToken(String token) {
        int typeSeparatorPosition = token.indexOf('&');
        if (typeSeparatorPosition == -1) {
            return false;
        }
        String authType = token.substring(0, typeSeparatorPosition);
        int typeKeyValueSepartorPosition = authType.indexOf('=');
        if (typeKeyValueSepartorPosition == -1 || !authType.substring(0, typeKeyValueSepartorPosition)
            .equalsIgnoreCase(Constants.Properties.AUTH_SCHEMA_TYPE)) {
            return false;
        }

        String authTypeValue = authType.substring(typeKeyValueSepartorPosition + 1);

        return authTypeValue.equalsIgnoreCase(Constants.Properties.SAS_TOKEN);
    }
}
