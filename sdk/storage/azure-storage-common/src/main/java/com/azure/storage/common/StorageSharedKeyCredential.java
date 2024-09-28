// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.azure.storage.common.Utility.urlDecode;

/**
 * SharedKey credential policy that is put into a header to authorize requests.
 */
public final class StorageSharedKeyCredential {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSharedKeyCredential.class);

    // Previous design used a constant for the ROOT_COLLATOR. This runs into performance issues as the ROOT Collator
    // can have the comparison method synchronized. In highly threaded environments this can result in threads waiting
    // to enter the synchronized block.
    private static final ThreadLocal<Collator> THREAD_LOCAL_COLLATOR
        = ThreadLocal.withInitial(() -> Collator.getInstance(Locale.ROOT));

    private static final Context LOG_STRING_TO_SIGN_CONTEXT = new Context(Constants.STORAGE_LOG_STRING_TO_SIGN, true);

    // Pieces of the connection string that are needed.
    private static final String ACCOUNT_KEY = "accountkey";
    private static final String ACCOUNT_NAME = "accountname";

    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");

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
        String accountName = null;
        String accountKey = null;

        for (String connectionStringPiece : connectionString.split(";")) {
            String[] kvp = connectionStringPiece.split("=", 2);

            if (kvp.length < 2) {
                continue;
            }

            if (ACCOUNT_NAME.equalsIgnoreCase(kvp[0])) {
                accountName = kvp[1];
            } else if (ACCOUNT_KEY.equalsIgnoreCase(kvp[0])) {
                accountKey = kvp[1];
            }
        }

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
        String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        contentLength = "0".equals(contentLength) ? "" : contentLength;

        // If the x-ms-header exists ignore the Date header
        String dateHeader = (headers.getValue(X_MS_DATE) != null)
            ? "" : getStandardHeaderValue(headers, HttpHeaderName.DATE);

        Collator collator = THREAD_LOCAL_COLLATOR.get();
        String stringToSign =  String.join("\n",
            httpMethod,
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_ENCODING),
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_LANGUAGE),
            contentLength,
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_MD5),
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_TYPE),
            dateHeader,
            getStandardHeaderValue(headers, HttpHeaderName.IF_MODIFIED_SINCE),
            getStandardHeaderValue(headers, HttpHeaderName.IF_MATCH),
            getStandardHeaderValue(headers, HttpHeaderName.IF_NONE_MATCH),
            getStandardHeaderValue(headers, HttpHeaderName.IF_UNMODIFIED_SINCE),
            getStandardHeaderValue(headers, HttpHeaderName.RANGE),
            getAdditionalXmsHeaders(headers, collator),
            getCanonicalizedResource(requestURL, collator));

        if (logStringToSign) {
            StorageImplUtils.logStringToSign(LOGGER, stringToSign, LOG_STRING_TO_SIGN_CONTEXT);
        }

        return stringToSign;
    }

    /*
     * Returns an empty string if the header value is null or empty.
     */
    private String getStandardHeaderValue(HttpHeaders headers, HttpHeaderName headerName) {
        final Header header = headers.get(headerName);
        return header == null ? "" : header.getValue();
    }

    private static String getAdditionalXmsHeaders(HttpHeaders headers, Collator collator) {
        List<Header> xmsHeaders = new ArrayList<>();

        int stringBuilderSize = 0;
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            if (!"x-ms-".regionMatches(true, 0, headerName, 0, 5)) {
                continue;
            }

            String headerValue = header.getValue();
            stringBuilderSize += headerName.length() + headerValue.length();

            xmsHeaders.add(header);
        }

        if (xmsHeaders.isEmpty()) {
            return "";
        }

        final StringBuilder canonicalizedHeaders = new StringBuilder(
            stringBuilderSize + (2 * xmsHeaders.size()) - 1);

        xmsHeaders.sort((o1, o2) -> collator.compare(o1.getName(), o2.getName()));

        for (Header xmsHeader : xmsHeaders) {
            if (canonicalizedHeaders.length() > 0) {
                canonicalizedHeaders.append('\n');
            }
            canonicalizedHeaders.append(xmsHeader.getName().toLowerCase(Locale.ROOT))
                .append(':')
                .append(xmsHeader.getValue());
        }

        return canonicalizedHeaders.toString();
    }

    private String getCanonicalizedResource(URL requestURL, Collator collator) {

        // Resource path
        String resourcePath = azureNamedKeyCredential.getAzureNamedKey().getName();

        // Note that AbsolutePath starts with a '/'.
        String absolutePath = requestURL.getPath();
        if (CoreUtils.isNullOrEmpty(absolutePath)) {
            absolutePath = "/";
        }

        // check for no query params and return
        String query = requestURL.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            return "/" + resourcePath + absolutePath;
        }

        int stringBuilderSize = 1 + resourcePath.length() + absolutePath.length() + query.length();

        // First split by comma and decode each piece to prevent confusing legitimate separate query values from query
        // values that contain a comma.
        //
        // Example 1: prefix=a%2cb => prefix={decode(a%2cb)} => prefix={"a,b"}
        // Example 2: prefix=a,2 => prefix={decode(a),decode(b) => prefix={"a","b"}
        TreeMap<String, List<String>> pieces = new TreeMap<>(collator);

        StorageImplUtils.parseQueryParameters(query).forEachRemaining(kvp -> {
            String key = urlDecode(kvp.getKey()).toLowerCase(Locale.ROOT);

            pieces.compute(key, (k, values) -> {
                if (values == null) {
                    values = new ArrayList<>();
                }

                for (String value : kvp.getValue().split(",")) {
                    values.add(urlDecode(value));
                }

                return values;
            });
        });

        stringBuilderSize += pieces.size();

        StringBuilder canonicalizedResource = new StringBuilder(stringBuilderSize)
            .append('/').append(resourcePath).append(absolutePath);

        for (Map.Entry<String, List<String>> queryParam : pieces.entrySet()) {
            List<String> queryParamValues = queryParam.getValue();
            queryParamValues.sort(collator);
            canonicalizedResource.append('\n').append(queryParam.getKey()).append(':');

            int size = queryParamValues.size();
            for (int i = 0; i < size; i++) {
                String queryParamValue = queryParamValues.get(i);
                if (i > 0) {
                    canonicalizedResource.append(',');
                }

                canonicalizedResource.append(queryParamValue);
            }
        }

        // append to main string builder the join of completed params with new line
        return canonicalizedResource.toString();
    }

    /**
     * Searches for a {@link StorageSharedKeyCredential} in the {@link HttpPipeline}.
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
