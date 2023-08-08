/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.services.blob.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.sun.jersey.api.client.ClientRequest;

public abstract class SharedKeyUtils {
    public static final String AUTHORIZATION_FILTER_MARKER = SharedKeyUtils.class
            .getName();

    /*
     * Constructing the Canonicalized Headers String
     * 
     * To construct the CanonicalizedHeaders portion of the signature string,
     * follow these steps: 1. Retrieve all headers for the resource that begin
     * with x-ms-, including the x-ms-date header. 2. Convert each HTTP header
     * name to lowercase. 3. Sort the headers lexicographically by header name,
     * in ascending order. Note that each header may appear only once in the
     * string. 4. Unfold the string by replacing any breaking white space with a
     * single space. 5. Trim any white space around the colon in the header. 6.
     * Finally, append a new line character to each canonicalized header in the
     * resulting list. Construct the CanonicalizedHeaders string by
     * concatenating all headers in this list into a single string.
     */
    public static String getCanonicalizedHeaders(ClientRequest cr) {
        ArrayList<String> msHeaders = new ArrayList<String>();
        for (String key : cr.getHeaders().keySet()) {
            if (key.toLowerCase(Locale.US).startsWith("x-ms-")) {
                msHeaders.add(key.toLowerCase(Locale.US));
            }
        }
        Collections.sort(msHeaders);

        String result = "";
        for (String msHeader : msHeaders) {
            result += msHeader + ":" + cr.getHeaders().getFirst(msHeader)
                    + "\n";
        }
        return result;
    }

    public static String getHeader(ClientRequest cr, String headerKey) {
        List<Object> values = cr.getHeaders().get(headerKey);
        if (values == null || values.size() != 1) {
            return nullEmpty(null);
        }

        return nullEmpty(values.get(0).toString());
    }

    private static String nullEmpty(String value) {
        return value != null ? value : "";
    }

    public static List<QueryParam> getQueryParams(String queryString) {
        ArrayList<QueryParam> result = new ArrayList<QueryParam>();

        if (queryString != null) {
            String[] params = queryString.split("&");
            for (String param : params) {
                result.add(getQueryParam(param));
            }
        }

        return result;
    }

    private static QueryParam getQueryParam(String param) {
        QueryParam result = new QueryParam();

        int index = param.indexOf("=");
        if (index < 0) {
            result.setName(param);
        } else {
            result.setName(param.substring(0, index));

            String value = param.substring(index + 1);
            int commaIndex = value.indexOf(',');
            if (commaIndex < 0) {
                result.addValue(value);
            } else {
                for (String v : value.split(",")) {
                    result.addValue(v);
                }
            }
        }

        return result;
    }

    public static class QueryParam implements Comparable<QueryParam> {
        private String name;
        private final List<String> values = new ArrayList<String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getValues() {
            return values;
        }

        public void addValue(String value) {
            values.add(value);
        }

        public int compareTo(QueryParam o) {
            return this.name.compareTo(o.name);
        }
    }
}
