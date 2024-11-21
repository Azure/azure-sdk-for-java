// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.data.tables.TableAzureNamedKeyCredentialPolicy;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * This class provides helper methods used when generating SAS.
 */
public class TableSasUtils {
    private static final String STRING_TO_SIGN_LOG_INFO_MESSAGE = "The string to sign computed by the SDK is: {}{}";
    private static final String STRING_TO_SIGN_LOG_WARNING_MESSAGE = "Please remember to disable '{}' before going "
        + "to production as this string can potentially contain PII.";

    /**
     * Shared helper method to append a SAS query parameter.
     *
     * @param sb The {@link StringBuilder} to append to.
     * @param param The {@code String} parameter to append.
     * @param value The value of the parameter to append.
     */
    public static void tryAppendQueryParameter(StringBuilder sb, String param, Object value) {
        if (value != null) {
            if (sb.length() != 0) {
                sb.append('&');
            }

            sb.append(TableUtils.urlEncode(param)).append('=').append(TableUtils.urlEncode(value.toString()));
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
            return StorageConstants.ISO_8601_UTC_DATE_FORMATTER.format(dateTime);
        }
    }

    /**
     * Extracts the {@link AzureNamedKeyCredential} from a {@link HttpPipeline}
     *
     * @param pipeline An {@link HttpPipeline} to extract an {@link AzureNamedKeyCredential} from.
     *
     * @return The extracted {@link AzureNamedKeyCredential}.
     */
    public static AzureNamedKeyCredential extractNamedKeyCredential(HttpPipeline pipeline) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i) instanceof TableAzureNamedKeyCredentialPolicy) {
                TableAzureNamedKeyCredentialPolicy policy = (TableAzureNamedKeyCredentialPolicy) pipeline.getPolicy(i);

                return policy.getCredential();
            }
        }

        return null;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     *
     * @param base64Key Base64 encoded key used to sign the string
     * @param stringToSign UTF-8 encoded string to sign
     *
     * @return the HMAC-SHA256 encoded signature
     *
     * @throws RuntimeException If the HMAC-SHA256 algorithm isn't support, if the key isn't a valid Base64 encoded
     * string, or the UTF-8 charset isn't supported.
     */
    public static String computeHmac256(final String base64Key, final String stringToSign) {
        try {
            byte[] key = Base64.getDecoder().decode(base64Key);
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] utf8Bytes = stringToSign.getBytes(StandardCharsets.UTF_8);

            return Base64.getEncoder().encodeToString(hmacSHA256.doFinal(utf8Bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }
}
