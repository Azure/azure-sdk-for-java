// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.reactivex.Single;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SharedKeyCredentials are a means of signing and authenticating storage requests. The key can be obtained from the
 * Azure portal. This factory will create policies which take care of all the details of creating strings to sign,
 * signing them, and setting the Authentication header. While this is a common way of authenticating with the service,
 * recommended practice is using {@link TokenCredentials}. Pass this as the credentials in the construction of a new
 * {@link HttpPipeline} via the {@link StorageURL} type.
 */
public final class SharedKeyCredentials implements ICredentials {

    private final String accountName;

    private final byte[] accountKey;

    /**
     * Initializes a new instance of SharedKeyCredentials contains an account's name and its primary or secondary
     * accountKey.
     *
     * @param accountName
     *         The account name associated with the request.
     * @param accountKey
     *         The account access key used to authenticate the request.
     *
     * @throws InvalidKeyException
     *         Thrown when the accountKey is ill-formatted.
     */
    public SharedKeyCredentials(String accountName, String accountKey) throws InvalidKeyException {
        this.accountName = accountName;
        this.accountKey = Base64.getDecoder().decode(accountKey);
    }

    /**
     * Gets the account name associated with the request.
     *
     * @return The account name.
     */
    public String getAccountName() {
        return accountName;
    }

    @Override
    public RequestPolicy create(RequestPolicy nextRequestPolicy, RequestPolicyOptions options) {
        return new SharedKeyCredentialsPolicy(this, nextRequestPolicy, options);
    }

    /**
     * Constructs a canonicalized string for signing a request.
     *
     * @param request
     *         The request to canonicalize.
     *
     * @return A canonicalized string.
     */
    private String buildStringToSign(final HttpRequest request) {
        final HttpHeaders httpHeaders = request.headers();
        String contentLength = getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.CONTENT_LENGTH);
        contentLength = contentLength.equals("0") ? Constants.EMPTY_STRING : contentLength;

        return String.join("\n",
                request.httpMethod().toString(),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.CONTENT_ENCODING),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.CONTENT_LANGUAGE),
                contentLength,
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.CONTENT_MD5),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.CONTENT_TYPE),
                // x-ms-date header exists, so don't sign date header
                Constants.EMPTY_STRING,
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.IF_MODIFIED_SINCE),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.IF_MATCH),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.IF_NONE_MATCH),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.IF_UNMODIFIED_SINCE),
                getStandardHeaderValue(httpHeaders, Constants.HeaderConstants.RANGE),
                getAdditionalXmsHeaders(httpHeaders),
                getCanonicalizedResource(request.url())
        );
    }

    private String getAdditionalXmsHeaders(final HttpHeaders headers) {
        // Add only headers that begin with 'x-ms-'
        final ArrayList<String> xmsHeaderNameArray = new ArrayList<>();
        for (HttpHeader header : headers) {
            String lowerCaseHeader = header.name().toLowerCase(Locale.ROOT);
            if (lowerCaseHeader.startsWith(Constants.PREFIX_FOR_STORAGE_HEADER)) {
                xmsHeaderNameArray.add(lowerCaseHeader);
            }
        }

        if (xmsHeaderNameArray.isEmpty()) {
            return Constants.EMPTY_STRING;
        }

        Collections.sort(xmsHeaderNameArray);

        final StringBuilder canonicalizedHeaders = new StringBuilder();
        for (final String key : xmsHeaderNameArray) {
            if (canonicalizedHeaders.length() > 0) {
                canonicalizedHeaders.append('\n');
            }

            canonicalizedHeaders.append(key);
            canonicalizedHeaders.append(':');
            canonicalizedHeaders.append(headers.value(key));
        }

        return canonicalizedHeaders.toString();
    }

    /**
     * Canonicalized the resource to sign.
     *
     * @param requestURL
     *         A {@code java.net.URL} of the request.
     *
     * @return The canonicalized resource to sign.
     */
    private String getCanonicalizedResource(URL requestURL) {

        // Resource path
        final StringBuilder canonicalizedResource = new StringBuilder("/");
        canonicalizedResource.append(this.accountName);

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
        QueryStringDecoder queryDecoder = new QueryStringDecoder("?" + requestURL.getQuery());
        Map<String, List<String>> queryParams = queryDecoder.parameters();

        ArrayList<String> queryParamNames = new ArrayList<>(queryParams.keySet());
        Collections.sort(queryParamNames);

        for (String queryParamName : queryParamNames) {
            final List<String> queryParamValues = queryParams.get(queryParamName);
            Collections.sort(queryParamValues);
            String queryParamValuesStr = String.join(",", queryParamValues.toArray(new String[]{}));
            canonicalizedResource.append("\n").append(queryParamName.toLowerCase(Locale.ROOT)).append(":")
                    .append(queryParamValuesStr);
        }

        // append to main string builder the join of completed params with new line
        return canonicalizedResource.toString();
    }

    /**
     * Returns the standard header value from the specified connection request, or an empty string if no header value
     * has been specified for the request.
     *
     * @param httpHeaders
     *         A {@code HttpHeaders} object that represents the headers for the request.
     * @param headerName
     *         A {@code String} that represents the name of the header being requested.
     *
     * @return A {@code String} that represents the header value, or {@code null} if there is no corresponding
     * header value for {@code headerName}.
     */
    private String getStandardHeaderValue(final HttpHeaders httpHeaders, final String headerName) {
        final String headerValue = httpHeaders.value(headerName);

        return headerValue == null ? Constants.EMPTY_STRING : headerValue;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     * Package-private because it is used to generate SAS signatures.
     *
     * @param stringToSign
     *         The UTF-8-encoded string to sign.
     *
     * @return A {@code String} that contains the HMAC-SHA256-encoded signature.
     *
     * @throws InvalidKeyException
     *         If the accountKey is not a valid Base64-encoded string.
     */
    String computeHmac256(final String stringToSign) throws InvalidKeyException {
        try {
            /*
            We must get a new instance of the Mac calculator for each signature calculated because the instances are
            not threadsafe and there is some suggestion online that they may not even be safe for reuse, so we use a
            new one each time to be sure.
             */
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            hmacSha256.init(new SecretKeySpec(this.accountKey, "HmacSHA256"));
            byte[] utf8Bytes = stringToSign.getBytes(Constants.UTF8_CHARSET);
            return Base64.getEncoder().encodeToString(hmacSha256.doFinal(utf8Bytes));
        } catch (final UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private static final class SharedKeyCredentialsPolicy implements RequestPolicy {

        private final SharedKeyCredentials factory;

        private final RequestPolicy nextPolicy;

        private final RequestPolicyOptions options;

        SharedKeyCredentialsPolicy(SharedKeyCredentials factory, RequestPolicy nextPolicy,
                RequestPolicyOptions options) {
            this.factory = factory;
            this.nextPolicy = nextPolicy;
            this.options = options;
        }

        /**
         * Sign the request.
         *
         * @param request
         *         The request to sign.
         *
         * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
         */
        @Override
        public Single<HttpResponse> sendAsync(final HttpRequest request) {
            if (request.headers().value(Constants.HeaderConstants.DATE) == null) {
                request.headers().set(Constants.HeaderConstants.DATE,
                        Utility.RFC_1123_GMT_DATE_FORMATTER.format(OffsetDateTime.now()));
            }
            final String stringToSign = this.factory.buildStringToSign(request);
            try {
                final String computedBase64Signature = this.factory.computeHmac256(stringToSign);
                request.headers().set(Constants.HeaderConstants.AUTHORIZATION,
                        "SharedKey " + this.factory.accountName + ":" + computedBase64Signature);
            } catch (Exception e) {
                return Single.error(e);
            }

            Single<HttpResponse> response = nextPolicy.sendAsync(request);
            return response.doOnSuccess(response1 -> {
                if (response1.statusCode() == HttpResponseStatus.FORBIDDEN.code()) {
                    if (options.shouldLog(HttpPipelineLogLevel.ERROR)) {
                        options.log(HttpPipelineLogLevel.ERROR,
                                "===== HTTP Forbidden status, String-to-Sign:%n'%s'%n==================%n",
                                stringToSign);
                    }
                }
            });
        }
    }
}

