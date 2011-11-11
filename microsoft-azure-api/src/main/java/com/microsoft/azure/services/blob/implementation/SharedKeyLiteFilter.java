package com.microsoft.azure.services.blob.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.services.blob.BlobConfiguration;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/*
 * TODO: Should the "full" shared key signing?
 */
public class SharedKeyLiteFilter extends ClientFilter {
    private static Log log = LogFactory.getLog(SharedKeyLiteFilter.class);

    private final String accountName;
    private final HmacSHA256Sign signer;

    public SharedKeyLiteFilter(@Named(BlobConfiguration.ACCOUNT_NAME) String accountName, @Named(BlobConfiguration.ACCOUNT_KEY) String accountKey) {

        this.accountName = accountName;
        // TODO: How to make this configurable?
        this.signer = new HmacSHA256Sign(accountKey);
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {

        // Only sign if no other filter has done it yet
        if (cr.getHeaders().getFirst("Authorization") == null) {
            sign(cr);
        }

        return this.getNext().handle(cr);
    }

    private String nullEmpty(String value) {
        return value != null ? value : "";
    }

    /*
     * StringToSign = VERB + "\n" + Content-MD5 + "\n" + Content-Type + "\n" +
     * Date + "\n" + CanonicalizedHeaders + CanonicalizedResource;
     */
    private void sign(ClientRequest cr) {
        // gather signed material
        String requestMethod = cr.getMethod();
        String contentMD5 = getHeader(cr, "Content-MD5");
        String contentType = getHeader(cr, "Content-Type");
        String date = getHeader(cr, "Date");

        if (date == "") {
            date = new RFC1123DateConverter().format(new Date());
            cr.getHeaders().add("Date", date);
        }

        // build signed string
        String stringToSign = requestMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n";

        stringToSign += addCanonicalizedHeaders(cr);
        stringToSign += addCanonicalizedResource(cr);

        if (log.isDebugEnabled()) {
            log.debug(String.format("String to sign: \"%s\"", stringToSign));
        }

        String signature = this.signer.sign(stringToSign);
        cr.getHeaders().putSingle("Authorization", "SharedKeyLite " + this.accountName + ":" + signature);
    }

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
    private String addCanonicalizedHeaders(ClientRequest cr) {
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

    private String addCanonicalizedResource(ClientRequest cr) {
        String canonicalizedResource = (String) cr.getProperties().get("canonicalizedResource");
        return nullEmpty(canonicalizedResource);
    }

    private String getHeader(ClientRequest cr, String headerKey) {
        List<Object> values = cr.getHeaders().get(headerKey);
        if (values == null || values.size() != 1) {
            return nullEmpty(null);
        }

        return nullEmpty(values.get(0).toString());
    }
}
