// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.implementation.Constants.UrlEncodingInfo;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

public class HttpUtils {

    private static Logger log = LoggerFactory.getLogger(HttpUtils.class);

    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, UrlEncodingInfo.UTF_8).replaceAll(UrlEncodingInfo.PLUS_SYMBOL_ESCAPED, UrlEncodingInfo.SINGLE_SPACE_URI_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to encode {}", url, e);
            throw new IllegalArgumentException("failed to encode url " + url, e);
        }
    }

    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url.replaceAll(UrlEncodingInfo.PLUS_SYMBOL_ESCAPED, UrlEncodingInfo.PLUS_SYMBOL_URI_ENCODING), UrlEncodingInfo.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to decode {}", url, e);
            throw new IllegalArgumentException("failed to decode url " + url, e);
        }
    }

    public static String getDateHeader(HttpHeaders headerValues) {
        if (headerValues == null) {
            return StringUtils.EMPTY;
        }

        // Since Date header is overridden by some proxies/http client libraries, we support
        // an additional date header 'x-ms-date' and prefer that to the regular 'date' header.
        String date = headerValues.getValue(HttpConstants.Headers.X_DATE);
        if (Strings.isNullOrEmpty(date)) {
            date = headerValues.getValue(HttpConstants.Headers.HTTP_DATE);
        }

        return date != null ? date : StringUtils.EMPTY;
    }

    public static void unescapeOwnerFullName(HttpHeaders httpHeaders) {
        String ownerValue = httpHeaders.getValue(HttpConstants.Headers.OWNER_FULL_NAME);

        if (StringUtils.isNotEmpty(ownerValue)) {
            String unescapedUrl = HttpUtils.urlDecode(ownerValue);
            httpHeaders.put(HttpConstants.Headers.OWNER_FULL_NAME, unescapedUrl);
        }
    }
}
