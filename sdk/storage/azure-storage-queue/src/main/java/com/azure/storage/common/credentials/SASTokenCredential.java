// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.core.implementation.util.ImplUtils;

import java.util.HashMap;

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
    public static SASTokenCredential fromQuery(String query) {
        if (ImplUtils.isNullOrEmpty(query)) {
            return null;
        }

        HashMap<String, String> queryParams = new HashMap<>();
        for (String queryParam : query.split("&")) {
            String key = queryParam.split("=", 2)[0];
            queryParams.put(key, queryParam);
        }

        if (queryParams.size() < 6
            || !queryParams.containsKey(SIGNED_VERSION)
            || !queryParams.containsKey(SIGNED_SERVICES)
            || !queryParams.containsKey(SIGNED_RESOURCE_TYPES)
            || !queryParams.containsKey(SIGNED_PERMISSIONS)
            || !queryParams.containsKey(SIGNED_EXPIRY)
            || !queryParams.containsKey(SIGNATURE)) {
            return null;
        }

        StringBuilder sasTokenBuilder = new StringBuilder(queryParams.get(SIGNED_VERSION))
            .append("&").append(queryParams.get(SIGNED_SERVICES))
            .append("&").append(queryParams.get(SIGNED_RESOURCE_TYPES))
            .append("&").append(queryParams.get(SIGNED_PERMISSIONS));

        // SIGNED_START is optional
        if (queryParams.containsKey(SIGNED_START)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_START));
        }

        sasTokenBuilder.append("&").append(queryParams.get(SIGNED_EXPIRY));

        // SIGNED_IP is optional
        if (queryParams.containsKey(SIGNED_IP)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_IP));
        }

        // SIGNED_PROTOCOL is optional
        if (queryParams.containsKey(SIGNED_PROTOCOL)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_PROTOCOL));
        }

        sasTokenBuilder.append("&").append(queryParams.get(SIGNATURE));

        return new SASTokenCredential(sasTokenBuilder.toString());
    }
}
