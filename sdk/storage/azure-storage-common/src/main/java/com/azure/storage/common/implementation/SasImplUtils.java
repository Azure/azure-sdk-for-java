// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.time.OffsetDateTime;

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
}
