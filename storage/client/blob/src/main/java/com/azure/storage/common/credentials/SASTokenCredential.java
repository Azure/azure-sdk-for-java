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
    private static final String SIGNED_RESOURCE = "sr";

    // Optional SAS token pieces
    private static final String SIGNED_START = "st";
    private static final String SIGNED_PROTOCOL = "spr";
    private static final String SIGNED_IP = "sip";

    private static final String CACHE_CONTROL = "rscc";
    private static final String CONTENT_DISPOSITION = "rscd";
    private static final String CONTENT_ENCODING = "rsce";
    private static final String CONTENT_LANGUAGE = "rscl";
    private static final String CONTENT_TYPE = "rsct";

    // Possible User Delegation Key pieces
    private static final String SIGNED_KEY_O_ID_ = "skoid";
    private static final String SIGNED_KEY_T_ID_ = "sktid";
    private static final String SIGNED_KEY_START = "skt";
    private static final String SIGNED_KEY_EXPIRY = "ske";
    private static final String SIGNED_KEY_SERVICE = "sks";
    private static final String SIGNED_KEY_VERSION = "skv";

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

        /* Because ServiceSAS only requires expiry and permissions, both of which could be on the container
         acl, the only guaranteed indication of a SAS is the signature. We'll let the service validate
         the other query parameters. */
        if (!queryParams.containsKey(SIGNATURE)) {
            return null;
        }

        StringBuilder sasTokenBuilder = new StringBuilder();

        if (queryParams.containsKey(SIGNED_VERSION)) {
            sasTokenBuilder.append(queryParams.get(SIGNED_VERSION));
        }

        if (queryParams.containsKey(SIGNED_SERVICES)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_SERVICES));
        }

        if (queryParams.containsKey(SIGNED_RESOURCE_TYPES)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_RESOURCE_TYPES));
        }

        if (queryParams.containsKey(SIGNED_PERMISSIONS)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_PERMISSIONS));
        }

        if (queryParams.containsKey(SIGNED_RESOURCE)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_RESOURCE));
        }

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

        if (queryParams.containsKey(CACHE_CONTROL)) {
            sasTokenBuilder.append("&").append(queryParams.get(CACHE_CONTROL));
        }

        if (queryParams.containsKey(CONTENT_DISPOSITION)) {
            sasTokenBuilder.append("&").append(queryParams.get(CONTENT_DISPOSITION));
        }

        if (queryParams.containsKey(CONTENT_ENCODING)) {
            sasTokenBuilder.append("&").append(queryParams.get(CONTENT_ENCODING));
        }

        if (queryParams.containsKey(CONTENT_LANGUAGE)) {
            sasTokenBuilder.append("&").append(queryParams.get(CONTENT_LANGUAGE));
        }

        if (queryParams.containsKey(CONTENT_TYPE)) {
            sasTokenBuilder.append("&").append(queryParams.get(CONTENT_TYPE));
        }

        // User Delegation Key Parameters
        if (queryParams.containsKey(SIGNED_KEY_O_ID_)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_O_ID_));
        }

        if (queryParams.containsKey(SIGNED_KEY_T_ID_)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_T_ID_));
        }

        if (queryParams.containsKey(SIGNED_KEY_START)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_START));
        }

        if (queryParams.containsKey(SIGNED_KEY_EXPIRY)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_EXPIRY));
        }

        if (queryParams.containsKey(SIGNED_KEY_SERVICE)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_SERVICE));
        }

        if (queryParams.containsKey(SIGNED_KEY_VERSION)) {
            sasTokenBuilder.append("&").append(queryParams.get(SIGNED_KEY_VERSION));
        }

        sasTokenBuilder.append("&").append(queryParams.get(SIGNATURE));

        return new SASTokenCredential(sasTokenBuilder.toString());
    }
}
