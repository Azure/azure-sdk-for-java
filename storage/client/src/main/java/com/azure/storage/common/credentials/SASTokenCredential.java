// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.core.implementation.util.ImplUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a SAS token used for authenticating requests.
 */
public final class SASTokenCredential {
    // Required SAS token pieces
    private static final String SIGNED_VERSION = "sv";
    private static final String SIGNED_SERVICES = "ss";
    private static final String SIGNED_RESOURCE_TYPES = "srt";
    private static final String SIGNED_PERMISSIONS = "sp";
    private static final String SIGNED_EXPIRY = "se";
    private static final String SIGNATURE = "sig";

    // Optional SAS token pieces
    private static final String SIGNED_START = "st";
    private static final String SIGNED_PROTOCOL = "spr";
    private static final String SIGNED_IP = "sip";

    private final String sasToken;

    /**
     * Creates a SAS token credential from the passed SAS token.
     * @param sasToken SAS token used to authenticate requests with the service.
     */
    public SASTokenCredential(String sasToken) {
        this.sasToken = sasToken;
    }

    /**
     * @return the SAS token
     */
    public String sasToken() {
        return sasToken;
    }

    /**
     * Creates a SAS token credential from the passed URL query string
     * @param query URL query used to build the SAS token
     * @return a SAS token credential if the query param contains all the necessary pieces
     */
    public static SASTokenCredential fromQuery(Map<String, String> query) {
        if (ImplUtils.isNullOrEmpty(query)) {
            return null;
        }

        if (query.size() < 6
            || !query.containsKey(SIGNED_VERSION)
            || !query.containsKey(SIGNED_SERVICES)
            || !query.containsKey(SIGNED_RESOURCE_TYPES)
            || !query.containsKey(SIGNED_PERMISSIONS)
            || !query.containsKey(SIGNED_EXPIRY)
            || !query.containsKey(SIGNATURE)) {
            return null;
        }

        StringBuilder sasTokenBuilder = new StringBuilder(query.get(SIGNED_VERSION))
            .append("&").append(query.get(SIGNED_SERVICES))
            .append("&").append(query.get(SIGNED_RESOURCE_TYPES))
            .append("&").append(query.get(SIGNED_PERMISSIONS));

        // SIGNED_START is optional
        if (query.containsKey(SIGNED_START)) {
            sasTokenBuilder.append("&").append(query.get(SIGNED_START));
        }

        sasTokenBuilder.append("&").append(query.get(SIGNED_EXPIRY));

        // SIGNED_IP is optional
        if (query.containsKey(SIGNED_IP)) {
            sasTokenBuilder.append("&").append(query.get(SIGNED_IP));
        }

        // SIGNED_PROTOCOL is optional
        if (query.containsKey(SIGNED_PROTOCOL)) {
            sasTokenBuilder.append("&").append(query.get(SIGNED_PROTOCOL));
        }

        sasTokenBuilder.append("&").append(query.get(SIGNATURE));

        return new SASTokenCredential(sasTokenBuilder.toString());
    }
}
