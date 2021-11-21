// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static com.azure.data.tables.implementation.TableSasUtils.computeHmac256;
import static com.azure.data.tables.implementation.TableUtils.parseQueryStringSplitValues;

/**
 * Policy that adds the SharedKey into the request's Authorization header.
 */
public final class TableAzureNamedKeyCredentialPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER_FORMAT = "SharedKeyLite %s:%s";
    private final AzureNamedKeyCredential credential;

    /**
     * Creates a SharedKey pipeline policy that adds the SharedKey into the request's authorization header.
     *
     * @param credential The SharedKey credential used to create the policy.
     */
    public TableAzureNamedKeyCredentialPolicy(AzureNamedKeyCredential credential) {
        this.credential = credential;
    }

    /**
     * Authorizes a {@link com.azure.core.http.HttpRequest} with the SharedKey credential.
     *
     * @param context The context of the request.
     * @param next The next policy in the pipeline.
     *
     * @return A reactive result containing the HTTP response.
     */
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = generateAuthorizationHeader(context.getHttpRequest().getUrl(),
            context.getHttpRequest().getHeaders().toMap());
        context.getHttpRequest().setHeader("Authorization", authorizationValue);

        return next.process();
    }

    /**
     * Generates the Auth Headers
     *
     * @param requestUrl The URL which the request is going to.
     * @param headers The headers of the request.
     *
     * @return The auth header
     */
    String generateAuthorizationHeader(URL requestUrl, Map<String, String> headers) {
        String signature = computeHmac256(credential.getAzureNamedKey().getKey(), buildStringToSign(requestUrl,
            headers));
        return String.format(AUTHORIZATION_HEADER_FORMAT, credential.getAzureNamedKey().getName(), signature);
    }

    /**
     * Creates the String to Sign.
     *
     * @param requestUrl The Url which the request is going to.
     * @param headers The headers of the request.
     *
     * @return A string to sign for the request.
     */
    private String buildStringToSign(URL requestUrl, Map<String, String> headers) {
        String dateHeader = headers.containsKey("x-ms-date")
            ? ""
            : this.getStandardHeaderValue(headers, "Date");

        String s = String.join("\n",
            dateHeader,  //date
            getCanonicalizedResource(requestUrl)); //Canonicalized resource

        return s;
    }

    /**
     * Gets necessary headers if the request does not already contain them.
     *
     * @param headers A map of the headers which the request has.
     * @param headerName The name of the header to get the standard header for.
     *
     * @return The standard header for the given name.
     */
    private String getStandardHeaderValue(Map<String, String> headers, String headerName) {
        String headerValue = headers.get(headerName);

        return headerValue == null ? "" : headerValue;
    }


    /**
     * Returns the canonicalized resource needed for a request.
     *
     * @param requestUrl The url of the request.
     *
     * @return The string that is the canonicalized resource.
     */
    private String getCanonicalizedResource(URL requestUrl) {
        StringBuilder canonicalizedResource = new StringBuilder("/").append(credential.getAzureNamedKey().getName());

        if (requestUrl.getPath().length() > 0) {
            canonicalizedResource.append(requestUrl.getPath());
        } else {
            canonicalizedResource.append('/');
        }

        if (requestUrl.getQuery() != null) {
            Map<String, String[]> queryParams = parseQueryStringSplitValues(requestUrl.getQuery());
            ArrayList<String> queryParamNames = new ArrayList<>(queryParams.keySet());

            Collections.sort(queryParamNames);

            for (String queryParamName : queryParamNames) {
                String[] queryParamValues = queryParams.get(queryParamName);

                Arrays.sort(queryParamValues);

                String queryParamValuesStr = String.join(",", queryParamValues);

                if (queryParamName.equalsIgnoreCase("comp")) {
                    canonicalizedResource.append("?").append(queryParamName.toLowerCase(Locale.ROOT)).append("=")
                        .append(queryParamValuesStr);
                }
            }
        }

        return canonicalizedResource.toString();
    }

    /**
     * Get the {@link AzureNamedKeyCredential} linked to the policy.
     *
     * @return The {@link AzureNamedKeyCredential}.
     */
    public AzureNamedKeyCredential getCredential() {
        return credential;
    }
}
