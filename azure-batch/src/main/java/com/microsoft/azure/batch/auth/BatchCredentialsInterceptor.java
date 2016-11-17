/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.auth;

import com.microsoft.rest.DateTimeRfc1123;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The interceptor class to insert Shared Key credential information to request HEADER.
 */
class BatchCredentialsInterceptor implements Interceptor {

    private BatchSharedKeyCredentials credentials;

    /**
     * Constructor for BatchCredentialsInterceptor
     *
     * @param batchCredentials The account name/key credential
     */
    public BatchCredentialsInterceptor(BatchSharedKeyCredentials batchCredentials) {
        this.credentials = batchCredentials;
    }

    /**
     * Inject the new authentication HEADER
     *
     * @param chain The interceptor chain
     * @return Response of the request
     * @throws IOException Exception thrown from serialization
     */
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request newRequest = this.signHeader(chain.request());
        return chain.proceed(newRequest);
    }

    private String headerValue(Request request, String headerName) {
        String headerValue = request.header(headerName);
        if (headerValue == null) {
            return "";
        }

        return headerValue;
    }

    private String sign(String accessKey, String stringToSign) {
        try {
            // Encoding the Signature
            // Signature=Base64(HMAC-SHA256(UTF8(StringToSign)))
            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.decodeBase64(accessKey),
                    "hmacSHA256"));
            byte[] digest = hmac.doFinal(stringToSign.getBytes("UTF-8"));
            return Base64.encodeBase64String(digest);
        } catch (Exception e) {
            throw new IllegalArgumentException("accessKey", e);
        }
    }

    private Request signHeader(Request request) throws IOException {

        Request.Builder builder = request.newBuilder();

        // Set Headers
        if (request.headers().get("ocp-date") == null) {
            DateTimeRfc1123 rfcDate = new DateTimeRfc1123(new DateTime());
            builder.header("ocp-date", rfcDate.toString());
            request = builder.build();
        }

        String signature = request.method() + "\n";
        signature = signature + headerValue(request, "Content-Encoding")
                + "\n";
        signature = signature + headerValue(request, "Content-Language")
                + "\n";

        // Special handle content length
        long length = -1;
        if (request.body() != null) {
            length = request.body().contentLength();
        }
        signature = signature + (length >= 0 ? Long.valueOf(length) : "")
                + "\n";

        signature = signature + headerValue(request, "Content-MD5") + "\n";

        // Special handle content type header
        String contentType = request.header("Content-Type");
        if (contentType == null) {
            contentType = "";
            if (request.body() != null) {
                MediaType mediaType = request.body().contentType();
                if (mediaType != null) {
                    contentType = mediaType.toString();
                }
            }
        }
        signature = signature + contentType + "\n";

        signature = signature + headerValue(request, "Date") + "\n";
        signature = signature + headerValue(request, "If-Modified-Since")
                + "\n";
        signature = signature + headerValue(request, "If-Match") + "\n";
        signature = signature + headerValue(request, "If-None-Match") + "\n";
        signature = signature + headerValue(request, "If-Unmodified-Since")
                + "\n";
        signature = signature + headerValue(request, "Range") + "\n";

        ArrayList<String> customHeaders = new ArrayList<String>();
        for (String name : request.headers().names()) {
            if (name.toLowerCase().startsWith("ocp-")) {
                customHeaders.add(name.toLowerCase());
            }
        }
        Collections.sort(customHeaders);
        for (String canonicalHeader : customHeaders) {
            String value = request.header(canonicalHeader);
            value = value.replace('\n', ' ').replace('\r', ' ')
                    .replaceAll("^[ ]+", "");
            signature = signature + canonicalHeader + ":" + value + "\n";
        }

        signature = signature + "/"
                + credentials.accountName().toLowerCase() + "/"
                + request.url().uri().getPath().replaceAll("^[/]+", "");
        // We temporary change client side auth code generator to bypass server
        // bug 4092533
        signature = signature.replace("%5C", "/").replace("%2F", "/");

        String query = request.url().query();
        if (query != null) {
            Map<String, String> queryComponents = new TreeMap<String, String>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                        .toLowerCase(Locale.US);
                queryComponents.put(
                        key,
                        key
                                + ":"
                                + URLDecoder.decode(pair.substring(idx + 1),
                                        "UTF-8"));
            }

            for (Map.Entry<String, String> entry : queryComponents.entrySet()) {
                signature = signature + "\n" + entry.getValue();
            }
        }
        String signedSignature = sign(credentials.keyValue(), signature);
        String authorization = "SharedKey " + credentials.accountName()
                + ":" + signedSignature;
        builder.header("Authorization", authorization);

        return builder.build();
    }

}
