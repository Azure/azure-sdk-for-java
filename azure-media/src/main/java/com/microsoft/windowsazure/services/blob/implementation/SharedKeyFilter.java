/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.implementation;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.core.RFC1123DateConverter;
import com.microsoft.windowsazure.core.pipeline.jersey.EntityStreamingListener;
import com.microsoft.windowsazure.services.blob.BlobConfiguration;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyUtils.QueryParam;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class SharedKeyFilter extends ClientFilter implements
        EntityStreamingListener {
    private static Log log = LogFactory.getLog(SharedKeyFilter.class);

    private final String accountName;
    private final HmacSHA256Sign signer;

    public SharedKeyFilter(
            @Named(BlobConfiguration.ACCOUNT_NAME) String accountName,
            @Named(BlobConfiguration.ACCOUNT_KEY) String accountKey) {
        this.accountName = accountName;
        this.signer = new HmacSHA256Sign(accountKey);
    }

    protected String getHeader(ClientRequest cr, String headerKey) {
        return SharedKeyUtils.getHeader(cr, headerKey);
    }

    protected HmacSHA256Sign getSigner() {
        return signer;
    }

    protected String getAccountName() {
        return accountName;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) {
        // Only sign if no other filter is handling authorization
        if (cr.getProperties().get(SharedKeyUtils.AUTHORIZATION_FILTER_MARKER) == null) {
            cr.getProperties().put(SharedKeyUtils.AUTHORIZATION_FILTER_MARKER,
                    null);

            // Register ourselves as listener so we are called back when the
            // entity is
            // written to the output stream by the next filter in line.
            if (cr.getProperties().get(EntityStreamingListener.class.getName()) == null) {
                cr.getProperties().put(EntityStreamingListener.class.getName(),
                        this);
            }

        }
        return this.getNext().handle(cr);
    }

    @Override
    public void onBeforeStreamingEntity(ClientRequest clientRequest) {
        // All headers should be known at this point, time to sign!
        sign(clientRequest);
    }

    /*
     * StringToSign = VERB + "\n" + Content-Encoding + "\n" Content-Language +
     * "\n" Content-Length + "\n" Content-MD5 + "\n" + Content-Type + "\n" +
     * Date + "\n" + If-Modified-Since + "\n" If-Match + "\n" If-None-Match +
     * "\n" If-Unmodified-Since + "\n" Range + "\n" CanonicalizedHeaders +
     * CanonicalizedResource;
     */
    public void sign(ClientRequest cr) {
        // gather signed material
        addOptionalDateHeader(cr);

        // build signed string
        String stringToSign = cr.getMethod() + "\n"
                + getHeader(cr, "Content-Encoding") + "\n"
                + getHeader(cr, "Content-Language") + "\n"
                + getHeader(cr, "Content-Length") + "\n"
                + getHeader(cr, "Content-MD5") + "\n"
                + getHeader(cr, "Content-Type") + "\n" + getHeader(cr, "Date")
                + "\n" + getHeader(cr, "If-Modified-Since") + "\n"
                + getHeader(cr, "If-Match") + "\n"
                + getHeader(cr, "If-None-Match") + "\n"
                + getHeader(cr, "If-Unmodified-Since") + "\n"
                + getHeader(cr, "Range") + "\n";

        stringToSign += getCanonicalizedHeaders(cr);
        stringToSign += getCanonicalizedResource(cr);

        if (log.isDebugEnabled()) {
            log.debug(String.format("String to sign: \"%s\"", stringToSign));
        }
        // System.out.println(String.format("String to sign: \"%s\"",
        // stringToSign));

        String signature = this.signer.sign(stringToSign);
        cr.getHeaders().putSingle("Authorization",
                "SharedKey " + this.accountName + ":" + signature);
    }

    protected void addOptionalDateHeader(ClientRequest cr) {
        String date = getHeader(cr, "Date");
        if (date != null && date.isEmpty()) {
            date = new RFC1123DateConverter().format(new Date());
            cr.getHeaders().putSingle("Date", date);
        }
    }

    /**
     * Constructing the Canonicalized Headers String
     * 
     * To construct the CanonicalizedHeaders portion of the signature string,
     * follow these steps:
     * 
     * 1. Retrieve all headers for the resource that begin with x-ms-, including
     * the x-ms-date header.
     * 
     * 2. Convert each HTTP header name to lowercase.
     * 
     * 3. Sort the headers lexicographically by header name, in ascending order.
     * Note that each header may appear only once in the string.
     * 
     * 4. Unfold the string by replacing any breaking white space with a single
     * space.
     * 
     * 5. Trim any white space around the colon in the header.
     * 
     * 6. Finally, append a new line character to each canonicalized header in
     * the resulting list. Construct the CanonicalizedHeaders string by
     * concatenating all headers in this list into a single string.
     */
    private String getCanonicalizedHeaders(ClientRequest cr) {
        return SharedKeyUtils.getCanonicalizedHeaders(cr);
    }

    /**
     * This format supports Shared Key authentication for the 2009-09-19 version
     * of the Blob and Queue services. Construct the CanonicalizedResource
     * string in this format as follows:
     * 
     * 1. Beginning with an empty string (""), append a forward slash (/),
     * followed by the name of the account that owns the resource being
     * accessed.
     * 
     * 2. Append the resource's encoded URI path, without any query parameters.
     * 
     * 3. Retrieve all query parameters on the resource URI, including the comp
     * parameter if it exists.
     * 
     * 4. Convert all parameter names to lowercase.
     * 
     * 5. Sort the query parameters lexicographically by parameter name, in
     * ascending order.
     * 
     * 6. URL-decode each query parameter name and value.
     * 
     * 7. Append each query parameter name and value to the string in the
     * following format, making sure to include the colon (:) between the name
     * and the value:
     * 
     * parameter-name:parameter-value
     * 
     * 8. If a query parameter has more than one value, sort all values
     * lexicographically, then include them in a comma-separated list:
     * 
     * parameter-name:parameter-value-1,parameter-value-2,parameter-value-n
     * 
     * 9. Append a new line character (\n) after each name-value pair.
     */
    private String getCanonicalizedResource(ClientRequest cr) {
        // 1. Beginning with an empty string (""), append a forward slash (/),
        // followed by the name of the account that owns
        // the resource being accessed.
        String result = "/" + this.accountName;

        // 2. Append the resource's encoded URI path, without any query
        // parameters.
        result += cr.getURI().getPath();

        // 3. Retrieve all query parameters on the resource URI, including the
        // comp parameter if it exists.
        // 6. URL-decode each query parameter name and value.
        List<QueryParam> queryParams = SharedKeyUtils.getQueryParams(cr
                .getURI().getQuery());

        // 4. Convert all parameter names to lowercase.
        for (QueryParam param : queryParams) {
            param.setName(param.getName().toLowerCase(Locale.US));
        }

        // 5. Sort the query parameters lexicographically by parameter name, in
        // ascending order.
        Collections.sort(queryParams);

        // 7. Append each query parameter name and value to the string
        // 8. If a query parameter has more than one value, sort all values
        // lexicographically, then include them in a comma-separated list
        for (int i = 0; i < queryParams.size(); i++) {
            QueryParam param = queryParams.get(i);

            List<String> values = param.getValues();
            // Collections.sort(values);

            // 9. Append a new line character (\n) after each name-value pair.
            result += "\n";
            result += param.getName();
            result += ":";
            for (int j = 0; j < values.size(); j++) {
                if (j > 0) {
                    result += ",";
                }
                result += values.get(j);
            }
        }

        return result;
    }
}
