// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides helper methods for sas.
 *
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
     * @param dateTime The SAS date time.
     * @return A String representing the SAS date time.
     */
    public static String formatQueryParameterDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return Constants.ISO_8601_UTC_DATE_FORMATTER.format(dateTime);
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
                keyValues = new String[]{value};
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
}
