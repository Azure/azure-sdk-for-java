package com.microsoft.windowsazure.services.blob.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.sun.jersey.api.client.ClientRequest;

public class SharedKeyUtils {
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
            result += msHeader + ":" + cr.getHeaders().getFirst(msHeader) + "\n";
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
}
