// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides helper methods for sas.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public class SasImplUtils {
    /**
     * Extracts the {@link StorageSharedKeyCredential} from a {@link HttpPipeline}
     * @param pipeline {@link HttpPipeline}
     * @return a {@link StorageSharedKeyCredential}
     */
    public static StorageSharedKeyCredential extractSharedKeyCredential(HttpPipeline pipeline) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i) instanceof StorageSharedKeyCredentialPolicy) {
                StorageSharedKeyCredentialPolicy policy = (StorageSharedKeyCredentialPolicy) pipeline.getPolicy(i);
                return policy.sharedKeyCredential();
            }
        }
        return null;
    }

    /**
     * Shared helper method to append a SAS query parameter.
     *
     * @param sb The {@code StringBuilder} to append to.
     * @param param The {@code String} parameter to append.
     * @param value The value of the parameter to append.
     */
    public static void tryAppendQueryParameter(StringBuilder sb, String param, Object value) {
        if (value != null) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append(Utility.urlEncode(param)).append('=').append(Utility.urlEncode(value.toString()));
        }
    }

    /**
     * Formats date time SAS query parameters.
     *
     * @param timeAndFormat The SAS date time.
     * @return A String representing the SAS date time.
     */
    public static String formatQueryParameterDate(TimeAndFormat timeAndFormat) {
        if (timeAndFormat == null || timeAndFormat.getDateTime() == null) {
            return null;
        } else {
            if (timeAndFormat.getFormatter() == null) {
                return Constants.ISO_8601_UTC_DATE_FORMATTER.format(timeAndFormat.getDateTime());
            } else {
                return timeAndFormat.getFormatter().format(timeAndFormat.getDateTime());
            }
        }
    }

    /**
     * Parses a query string into a one to many TreeMap.
     *
     * @param queryParams The string of query params to parse.
     * @return A {@code HashMap<String, String[]>} of the key values.
     */
    public static Map<String, String[]> parseQueryString(String queryParams) {
        final TreeMap<String, String[]> retVals = new TreeMap<>(Comparator.naturalOrder());

        if (CoreUtils.isNullOrEmpty(queryParams)) {
            return retVals;
        }

        // trim leading ? if present.
        if (queryParams.startsWith("?")) {
            queryParams = queryParams.substring(1);
        }

        // split name value pairs by splitting on the '&' character
        final String[] valuePairs = queryParams.split("&");

        // for each field value pair parse into appropriate map entries
        for (String valuePair : valuePairs) {
            // Getting key and value for a single query parameter
            final int equalDex = valuePair.indexOf("=");
            String key = Utility.urlDecode(valuePair.substring(0, equalDex)).toLowerCase(Locale.ROOT);
            String value = Utility.urlDecode(valuePair.substring(equalDex + 1));

            // add to map
            String[] keyValues = retVals.get(key);

            // check if map already contains key
            if (keyValues == null) {
                // map does not contain this key
                keyValues = new String[] { value };
            } else {
                // map contains this key already so append
                final String[] newValues = new String[keyValues.length + 1];
                System.arraycopy(keyValues, 0, newValues, 0, keyValues.length);

                newValues[newValues.length - 1] = value;
                keyValues = newValues;
            }
            retVals.put(key, keyValues);
        }

        return retVals;
    }

    /**
     * Formats request headers for SAS signing.
     *
     * @param requestHeaders The map of request headers to format.
     * @param includeKeyValues Whether to include the values of the query parameters in the formatted string.
     * If false, only the keys will be included, separated by commas.
     * @return A formatted string with or without values depending on the includeKeyValues parameter.
     * @see
     * <a href="https://learn.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas#version-2026-04-06-and-later-blob-storage-and-data-lake-storage">
     *     Version 2026-04-06 and later (Blob Storage and Data Lake Storage)</a>
     */
    public static String formatRequestHeaders(Map<String, String> requestHeaders, boolean includeKeyValues) {
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            return null;
        }

        // Ensure deterministic ordering by header name for SAS signing.
        List<String> sortedKeys = new ArrayList<>(requestHeaders.keySet());
        sortedKeys.sort(String::compareTo);

        if (includeKeyValues) {
            StringBuilder sb = new StringBuilder();
            for (String key : sortedKeys) {
                String value = requestHeaders.get(key);
                sb.append(key).append(':').append(value).append('\n');
            }
            return sb.toString();
        }

        return String.join(",", sortedKeys);
    }

    /**
     * Formats request query parameters for SAS signing.
     *
     * @param requestQueryParameters The map of request query parameters to format.
     * @param includeKeyValues Whether to include the values of the query parameters in the formatted string.
     * If false, only the keys will be included, separated by commas.
     * @return A formatted string with or without values depending on the includeKeyValues parameter.
     * @see
     * <a href="https://learn.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas#version-2026-04-06-and-later-blob-storage-and-data-lake-storage">
     *     Version 2026-04-06 and later (Blob Storage and Data Lake Storage)</a>
     */
    public static String formatRequestQueryParameters(Map<String, String> requestQueryParameters,
        boolean includeKeyValues) {
        if (requestQueryParameters == null || requestQueryParameters.isEmpty()) {
            return null;
        }

        // Ensure deterministic ordering by parameter name for SAS signing.
        List<String> sortedKeys = new ArrayList<>(requestQueryParameters.keySet());
        sortedKeys.sort(String::compareTo);

        if (includeKeyValues) {
            StringBuilder sb = new StringBuilder();
            for (String key : sortedKeys) {
                String value = requestQueryParameters.get(key);
                sb.append('\n').append(key).append(':').append(value);
            }
            return sb.toString();
        }

        return String.join(",", sortedKeys);
    }

    /**
     * Formats a list of keys into a comma separated string.
     *
     * @param listOfKeys The list of keys to format.
     * @return A comma separated string of the keys, or null if the list is null/empty.
     */
    public static String formatKeyList(List<String> listOfKeys) {
        if (listOfKeys == null || listOfKeys.isEmpty()) {
            return null;
        }
        return String.join(",", listOfKeys);
    }

    /**
    * Parses a comma separated string of keys into a list. The values for the keys are never present at this point.
    *
    * @param rawString The comma separated string of keys to parse.
    * @return A list of the keys, or null if the string is null/empty.
    */
    public static List<String> parseRequestHeadersAndQueryParameterString(String rawString) {
        if (CoreUtils.isNullOrEmpty(rawString)) {
            return null;
        }
        String[] keys = rawString.split(",");
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            if (!CoreUtils.isNullOrEmpty(key)) {
                keyList.add(key.trim());
            }
        }
        return keyList;
    }
}
