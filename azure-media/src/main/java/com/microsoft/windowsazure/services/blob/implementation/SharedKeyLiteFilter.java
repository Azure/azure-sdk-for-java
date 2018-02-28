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

import java.util.Date;
import java.util.List;

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

public class SharedKeyLiteFilter extends ClientFilter implements
        EntityStreamingListener {
    private static Log log = LogFactory.getLog(SharedKeyLiteFilter.class);

    private final String accountName;
    private final HmacSHA256Sign signer;

    public SharedKeyLiteFilter(
            @Named(BlobConfiguration.ACCOUNT_NAME) String accountName,
            @Named(BlobConfiguration.ACCOUNT_KEY) String accountKey) {

        this.accountName = accountName;
        this.signer = new HmacSHA256Sign(accountKey);
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
     * StringToSign = VERB + "\n" + Content-MD5 + "\n" + Content-Type + "\n" +
     * Date + "\n" + CanonicalizedHeaders + CanonicalizedResource;
     */
    public void sign(ClientRequest cr) {
        // gather signed material
        String requestMethod = cr.getMethod();
        String contentMD5 = getHeader(cr, "Content-MD5");
        String contentType = getHeader(cr, "Content-Type");
        String date = getHeader(cr, "Date");

        if (date != null && date.isEmpty()) {
            date = new RFC1123DateConverter().format(new Date());
            cr.getHeaders().add("Date", date);
        }

        // build signed string
        String stringToSign = requestMethod + "\n" + contentMD5 + "\n"
                + contentType + "\n" + date + "\n";

        stringToSign += addCanonicalizedHeaders(cr);
        stringToSign += addCanonicalizedResource(cr);

        if (log.isDebugEnabled()) {
            log.debug(String.format("String to sign: \"%s\"", stringToSign));
        }

        String signature = this.signer.sign(stringToSign);
        cr.getHeaders().putSingle("Authorization",
                "SharedKeyLite " + this.accountName + ":" + signature);
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
     * 3. Sort the headers lexicographically by header name in ascending order.
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
    private String addCanonicalizedHeaders(ClientRequest cr) {
        return SharedKeyUtils.getCanonicalizedHeaders(cr);
    }

    /**
     * This format supports Shared Key and Shared Key Lite for all versions of
     * the Table service, and Shared Key Lite for the 2009-09-19 version of the
     * Blob and Queue services. This format is identical to that used with
     * previous versions of the storage services. Construct the
     * CanonicalizedResource string in this format as follows:
     * 
     * 1. Beginning with an empty string (""), append a forward slash (/),
     * followed by the name of the account that owns the resource being
     * accessed.
     * 
     * 2. Append the resource's encoded URI path. If the request URI addresses a
     * component of the resource, append the appropriate query string. The query
     * string should include the question mark and the comp parameter (for
     * example, ?comp=metadata). No other parameters should be included on the
     * query string.
     */
    private String addCanonicalizedResource(ClientRequest cr) {
        String result = "/" + this.accountName;

        result += cr.getURI().getPath();

        List<QueryParam> queryParams = SharedKeyUtils.getQueryParams(cr
                .getURI().getQuery());
        for (QueryParam p : queryParams) {
            if ("comp".equals(p.getName())) {
                result += "?" + p.getName() + "=" + p.getValues().get(0);
            }
        }
        return result;
    }

    private String getHeader(ClientRequest cr, String headerKey) {
        return SharedKeyUtils.getHeader(cr, headerKey);
    }
}
