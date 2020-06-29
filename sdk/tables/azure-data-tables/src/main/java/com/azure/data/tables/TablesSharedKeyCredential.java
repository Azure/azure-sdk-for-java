// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.net.URL;
import java.util.*;

/**
 * A Class which helps generate the shared key credentials for a given storage account to create a Http requests to
 * access Azure Tables
 */
public class TablesSharedKeyCredential {

    private static final String AUTHORIZATION_HEADER_FORMAT = "SharedKeyLite %s:%s";
    private final String accountName;
    private final String accountKey;

    /**
     * Constructor for TableSharedKeyCredential Class
     *
     * @param accountName name of the storage account
     * @param accountKey  key to the storage account
     */
    public TablesSharedKeyCredential(String accountName, String accountKey) {
        Objects.requireNonNull(accountName, "'accountName' cannot be null.");
        Objects.requireNonNull(accountKey, "'accountKey' cannot be null.");
        this.accountName = accountName;
        this.accountKey = accountKey;
    }

    /**
     * Generates the Auth Headers
     *
     * @param requestURL the URL which the request is going to
     * @param headers    the headers of the request
     * @return the auth header
     */
    public String generateAuthorizationHeader(URL requestURL, Map<String, String> headers) {
        String signature = StorageImplUtils.computeHMac256(this.accountKey, this.buildStringToSign(requestURL,
            headers));
        return String.format(AUTHORIZATION_HEADER_FORMAT, this.accountName, signature);
    }

    /**
     * creates the String to Sign
     *
     * @param requestURL the URL which the request is going to
     * @param headers    the headers of the request
     * @return a string to sign for the request
     */
    private String buildStringToSign(URL requestURL, Map<String, String> headers) {
        String dateHeader = headers.containsKey("x-ms-date") ? "" : this.getStandardHeaderValue(headers,
            "Date");
        return String.join("\n",
            dateHeader,  //date
            this.getCanonicalizedResource(requestURL)); //Canonicalized resource
    }

    /**
     * gets necessary headers if the request does not already contain them
     *
     * @param headers    a map of the headers which the request has
     * @param headerName the name of the header to get the standard header for
     * @return the standard header for the given name
     */
    private String getStandardHeaderValue(Map<String, String> headers, String headerName) {
        String headerValue = headers.get(headerName);
        return headerValue == null ? "" : headerValue;
    }


    /**
     * returns the canonicalized resource needed for a request
     *
     * @param requestURL the url of the request
     * @return the string that is the canonicalized resource
     */
    private String getCanonicalizedResource(URL requestURL) {
        StringBuilder canonicalizedResource = new StringBuilder("/");
        canonicalizedResource.append(this.accountName);
        if (requestURL.getPath().length() > 0) {
            canonicalizedResource.append(requestURL.getPath());
        } else {
            canonicalizedResource.append('/');
        }

        if (requestURL.getQuery() != null) {
            Map<String, String[]> queryParams = StorageImplUtils.parseQueryStringSplitValues(requestURL.getQuery());
            ArrayList<String> queryParamNames = new ArrayList<>(queryParams.keySet());
            Collections.sort(queryParamNames);

            for (String queryParamName : queryParamNames) {
                String[] queryParamValues = queryParams.get(queryParamName);
                Arrays.sort(queryParamValues);
                String queryParamValuesStr = String.join(",", queryParamValues);
                if (queryParamName.equals("comp")) {
                    canonicalizedResource.append("?").append(queryParamName.toLowerCase(Locale.ROOT)).append("=")
                        .append(queryParamValuesStr);
                }
            }
        }
        return canonicalizedResource.toString();
    }
}
