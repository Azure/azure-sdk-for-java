// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * SharedKey credential policy that is put into a header to authorize requests.
 */
public final class StorageSharedKeyCredential {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSharedKeyCredential.class);

    private static final Context LOG_STRING_TO_SIGN_CONTEXT = new Context(Constants.STORAGE_LOG_STRING_TO_SIGN, true);

    // Pieces of the connection string that are needed.
    private static final String ACCOUNT_NAME = "accountname";
    private static final String ACCOUNT_KEY = "accountkey";

    private final AzureNamedKeyCredential azureNamedKeyCredential;

    /**
     * Initializes a new instance of StorageSharedKeyCredential contains an account's name and its primary or secondary
     * accountKey.
     *
     * @param accountName The account name associated with the request.
     * @param accountKey The account access key used to authenticate the request.
     */
    public StorageSharedKeyCredential(String accountName, String accountKey) {
        Objects.requireNonNull(accountName, "'accountName' cannot be null.");
        Objects.requireNonNull(accountKey, "'accountKey' cannot be null.");
        this.azureNamedKeyCredential = new AzureNamedKeyCredential(accountName, accountKey);
    }

    private StorageSharedKeyCredential(AzureNamedKeyCredential azureNamedKeyCredential) {
        Objects.requireNonNull(azureNamedKeyCredential, "'azureNamedKeyCredential' cannot be null.");
        this.azureNamedKeyCredential = azureNamedKeyCredential;
    }

    /**
     * Creates a SharedKey credential from the passed connection string.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.common.StorageSharedKeyCredential.fromConnectionString#String -->
     * <pre>
     * StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString&#40;connectionString&#41;;
     * </pre>
     * <!-- end com.azure.storage.common.StorageSharedKeyCredential.fromConnectionString#String -->
     *
     * @param connectionString Connection string used to build the SharedKey credential.
     * @return a SharedKey credential if the connection string contains AccountName and AccountKey
     * @throws IllegalArgumentException If {@code connectionString} doesn't have AccountName or AccountKey.
     */
    public static StorageSharedKeyCredential fromConnectionString(String connectionString) {
        HashMap<String, String> connectionStringPieces = new HashMap<>();
        for (String connectionStringPiece : connectionString.split(";")) {
            String[] kvp = connectionStringPiece.split("=", 2);
            connectionStringPieces.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        String accountName = connectionStringPieces.get(ACCOUNT_NAME);
        String accountKey = connectionStringPieces.get(ACCOUNT_KEY);

        if (CoreUtils.isNullOrEmpty(accountName) || CoreUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        return new StorageSharedKeyCredential(accountName, accountKey);
    }

    /**
     * Creates a SharedKey credential from the passed {@link AzureNamedKeyCredential}.
     *
     * @param azureNamedKeyCredential {@link AzureNamedKeyCredential} used to build the SharedKey credential.
     * @return a SharedKey credential converted from {@link AzureNamedKeyCredential}
     * @throws NullPointerException If {@code azureNamedKeyCredential} is null.
     */
    public static StorageSharedKeyCredential fromAzureNamedKeyCredential(
        AzureNamedKeyCredential azureNamedKeyCredential) {
        return new StorageSharedKeyCredential(azureNamedKeyCredential);
    }

    /**
     * Gets the account name associated with the request.
     *
     * @return The account name.
     */
    public String getAccountName() {
        return azureNamedKeyCredential.getAzureNamedKey().getName();
    }

    /**
     * Generates the SharedKey Authorization value from information in the request.
     * @param requestURL URL of the request
     * @param httpMethod HTTP method being used
     * @param headers Headers on the request
     * @return the SharedKey authorization value
     */
    public String generateAuthorizationHeader(URL requestURL, String httpMethod, Map<String, String> headers) {
        return generateAuthorizationHeader(requestURL, httpMethod, headers, false);
    }

    /**
     * Generates the SharedKey Authorization value from information in the request.
     * @param requestURL URL of the request
     * @param httpMethod HTTP method being used
     * @param headers Headers on the request
     * @param logStringToSign Whether to log the string to sign
     * @return the SharedKey authorization value
     */
    public String generateAuthorizationHeader(URL requestURL, String httpMethod, Map<String, String> headers,
        boolean logStringToSign) {
        return generateAuthorizationHeader(requestURL, httpMethod, new HttpHeaders(headers), logStringToSign);
    }

    /**
     * Generates the SharedKey Authorization value from information in the request.
     * @param requestURL URL of the request
     * @param httpMethod HTTP method being used
     * @param headers Headers on the request
     * @param logStringToSign Whether to log the string to sign
     * @return the SharedKey authorization value
     */
    public String generateAuthorizationHeader(URL requestURL, String httpMethod, HttpHeaders headers,
        boolean logStringToSign) {
        String signature = StorageImplUtils.computeHMac256(azureNamedKeyCredential.getAzureNamedKey().getKey(),
            buildStringToSign(requestURL, httpMethod, headers, logStringToSign));
        return "SharedKey " + azureNamedKeyCredential.getAzureNamedKey().getName() + ":" + signature;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     * Package-private because it is used to generate SAS signatures.
     *
     * @param stringToSign The UTF-8-encoded string to sign.
     * @return A {@code String} that contains the HMAC-SHA256-encoded signature.
     * @throws RuntimeException If the HMAC-SHA256 algorithm isn't support, if the key isn't a valid Base64 encoded
     * string, or the UTF-8 charset isn't supported.
     */
    public String computeHmac256(final String stringToSign) {
        return StorageImplUtils.computeHMac256(azureNamedKeyCredential.getAzureNamedKey().getKey(), stringToSign);
    }

    private String buildStringToSign(URL requestURL, String httpMethod, HttpHeaders headers,
        boolean logStringToSign) {
        String contentLength = headers.getValue("Content-Length");
        contentLength = "0".equals(contentLength) ? "" : contentLength;

        // If the x-ms-header exists ignore the Date header
        String dateHeader = (headers.getValue("x-ms-date") != null) ? ""
            : getStandardHeaderValue(headers, "Date");

        String stringToSign =  String.join("\n",
            httpMethod,
            getStandardHeaderValue(headers, "Content-Encoding"),
            getStandardHeaderValue(headers, "Content-Language"),
            contentLength,
            getStandardHeaderValue(headers, "Content-MD5"),
            getStandardHeaderValue(headers, "Content-Type"),
            dateHeader,
            getStandardHeaderValue(headers, "If-Modified-Since"),
            getStandardHeaderValue(headers, "If-Match"),
            getStandardHeaderValue(headers, "If-None-Match"),
            getStandardHeaderValue(headers, "If-Unmodified-Since"),
            getStandardHeaderValue(headers, "Range"),
            getAdditionalXmsHeaders(headers),
            getCanonicalizedResource(requestURL));

        if (logStringToSign) {
            StorageImplUtils.logStringToSign(LOGGER, stringToSign, LOG_STRING_TO_SIGN_CONTEXT);
        }

        return stringToSign;
    }

    /*
     * Returns an empty string if the header value is null or empty.
     */
    private String getStandardHeaderValue(HttpHeaders headers, String headerName) {
        final Header header = headers.get(headerName);
        return header == null ? "" : header.getValue();
    }

    private String getAdditionalXmsHeaders(HttpHeaders headers) {
        // Add only headers that begin with 'x-ms-'
        Map<String, String> sortedXmsHeaders = new TreeMap<>(
            /* Culture-sensitive word sort */
            Collator.getInstance(Locale.ROOT)
        );

        int stringBuilderSize = 0;
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            if (!"x-ms-".regionMatches(true, 0, headerName, 0, 5)) {
                continue;
            }

            String headerValue = header.getValue();
            stringBuilderSize += headerName.length() + headerValue.length();

            sortedXmsHeaders.put(headerName.toLowerCase(Locale.ROOT), headerValue);
        }

        if (sortedXmsHeaders.isEmpty()) {
            return "";
        }

        final StringBuilder canonicalizedHeaders = new StringBuilder(
            stringBuilderSize + (2 * sortedXmsHeaders.size()) - 1);

        sortedXmsHeaders.forEach((name, value) -> {
            if (canonicalizedHeaders.length() > 0) {
                canonicalizedHeaders.append('\n');
            }
            canonicalizedHeaders.append(name)
                .append(':')
                .append(value);
        });

        return canonicalizedHeaders.toString();
    }

    private String getCanonicalizedResource(URL requestURL) {

        // Resource path
        final StringBuilder canonicalizedResource = new StringBuilder("/");
        canonicalizedResource.append(azureNamedKeyCredential.getAzureNamedKey().getName());

        // Note that AbsolutePath starts with a '/'.
        if (requestURL.getPath().length() > 0) {
            canonicalizedResource.append(requestURL.getPath());
        } else {
            canonicalizedResource.append('/');
        }

        // check for no query params and return
        if (requestURL.getQuery() == null) {
            return canonicalizedResource.toString();
        }

        // The URL object's query field doesn't include the '?'. The QueryStringDecoder expects it.
        // The Map returned is already sorted with all keys lower cased.
        Map<String, String[]> queryParams = StorageImplUtils.parseQueryStringSplitValues(requestURL.getQuery());

        for (Map.Entry<String, String[]> queryParam : queryParams.entrySet()) {
            String[] queryParamValues = queryParam.getValue();
            Arrays.sort(queryParamValues);
            String queryParamValuesStr = String.join(",", queryParamValues);
            canonicalizedResource.append('\n')
                .append(queryParam.getKey())
                .append(':')
                .append(queryParamValuesStr);
        }

        // append to main string builder the join of completed params with new line
        return canonicalizedResource.toString();
    }

    /**
     * Searches for a {@link StorageSharedKeyCredential} in the passed {@link HttpPipeline}.
     *
     * @param httpPipeline Pipeline being searched
     * @return a StorageSharedKeyCredential if the pipeline contains one, otherwise null.
     */
    public static StorageSharedKeyCredential getSharedKeyCredentialFromPipeline(HttpPipeline httpPipeline) {
        for (int i = 0; i < httpPipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy httpPipelinePolicy = httpPipeline.getPolicy(i);
            if (httpPipelinePolicy instanceof StorageSharedKeyCredentialPolicy) {
                StorageSharedKeyCredentialPolicy storageSharedKeyCredentialPolicy =
                    (StorageSharedKeyCredentialPolicy) httpPipelinePolicy;
                return storageSharedKeyCredentialPolicy.sharedKeyCredential();
            }
        }
        return null;
    }

}
