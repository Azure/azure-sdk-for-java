/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.implementation;

import java.util.List;

import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.blob.implementation.SharedKeyUtils;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyUtils.QueryParam;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.sun.jersey.api.client.ClientRequest;

public class SharedKeyFilter extends com.microsoft.windowsazure.services.blob.implementation.SharedKeyFilter {
    private static Log log = LogFactory.getLog(SharedKeyFilter.class);

    public SharedKeyFilter(@Named(TableConfiguration.ACCOUNT_NAME) String accountName,
            @Named(TableConfiguration.ACCOUNT_KEY) String accountKey) {
        super(accountName, accountKey);
    }

    /*
     * StringToSign = VERB + "\n" + 
     *           Content-MD5 + "\n" + 
     *           Content-Type + "\n" +
     *           Date + "\n" +
     *           CanonicalizedResource;
     */
    @Override
    public void sign(ClientRequest cr) {
        // gather signed material
        addOptionalDateHeader(cr);

        // build signed string
        String stringToSign = cr.getMethod() + "\n" + getHeader(cr, "Content-MD5") + "\n"
                + getHeader(cr, "Content-Type") + "\n" + getHeader(cr, "Date") + "\n";

        stringToSign += getCanonicalizedResource(cr);

        if (log.isDebugEnabled()) {
            log.debug(String.format("String to sign: \"%s\"", stringToSign));
        }
        System.out.println(String.format("String to sign: \"%s\"", stringToSign));

        String signature = this.getSigner().sign(stringToSign);
        cr.getHeaders().putSingle("Authorization", "SharedKey " + this.getAccountName() + ":" + signature);
    }

    /**
     * This format supports Shared Key and Shared Key Lite for all versions of the Table service, and Shared Key Lite
     * for the 2009-09-19 version of the Blob and Queue services. This format is identical to that used with previous
     * versions of the storage services. Construct the CanonicalizedResource string in this format as follows:
     * 
     * 1. Beginning with an empty string (""), append a forward slash (/), followed by the name of the account that owns
     * the resource being accessed.
     * 
     * 2. Append the resource's encoded URI path. If the request URI addresses a component of the resource, append the
     * appropriate query string. The query string should include the question mark and the comp parameter (for example,
     * ?comp=metadata). No other parameters should be included on the query string.
     */
    private String getCanonicalizedResource(ClientRequest cr) {
        String result = "/" + this.getAccountName();

        result += cr.getURI().getPath();

        List<QueryParam> queryParams = SharedKeyUtils.getQueryParams(cr.getURI().getQuery());
        for (QueryParam p : queryParams) {
            if ("comp".equals(p.getName())) {
                result += "?" + p.getName() + "=" + p.getValues().get(0);
            }
        }
        return result;
    }
}
