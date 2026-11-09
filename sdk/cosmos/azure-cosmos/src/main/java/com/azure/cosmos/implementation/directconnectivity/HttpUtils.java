// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Constants.UrlEncodingInfo;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.http.HttpHeader;
import com.azure.cosmos.implementation.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpUtils {

    private static Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static final Pattern PLUS_SYMBOL_ESCAPE_PATTERN = Pattern.compile(UrlEncodingInfo.PLUS_SYMBOL_ESCAPED);

    /**
     * Returns the initial capacity for a HashMap that will hold {@code expectedSize} entries
     * without resizing, accounting for the default load factor of 0.75.
     */
    public static int mapCapacityForSize(int expectedSize) {
        if (expectedSize <= 0) {
            return 1;
        }
        long capacity = (long) expectedSize * 4 / 3 + 1;
        return capacity >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    public static String urlEncode(String url) {
        try {
            return PLUS_SYMBOL_ESCAPE_PATTERN.matcher(URLEncoder.encode(url, UrlEncodingInfo.UTF_8))
                .replaceAll(UrlEncodingInfo.SINGLE_SPACE_URI_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to encode {}", url, e);
            throw new IllegalArgumentException("failed to encode url " + url, e);
        }
    }

    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(PLUS_SYMBOL_ESCAPE_PATTERN.matcher(url).replaceAll(UrlEncodingInfo.PLUS_SYMBOL_URI_ENCODING),
                UrlEncodingInfo.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to decode {}", url, e);
            throw new IllegalArgumentException("failed to decode url " + url, e);
        }
    }

    public static Map<String, String> asMap(HttpHeaders headers) {
        if (headers == null) {
            return new HashMap<>(4);
        }
        HashMap<String, String> map = new HashMap<>(mapCapacityForSize(headers.size()));
        for (HttpHeader header : headers) {
            if (header.name().equals(HttpConstants.HttpHeaders.OWNER_FULL_NAME)) {
                map.put(header.name(), HttpUtils.urlDecode(header.value()));
            } else {
                map.put(header.name(), header.value());
            }
        }
        return map;
    }

    public static String getDateHeader(Map<String, String> headerValues) {
        if (headerValues == null) {
            return StringUtils.EMPTY;
        }

        // Since Date header is overridden by some proxies/http client libraries, we support
        // an additional date header 'x-ms-date' and prefer that to the regular 'date' header.
        String date = headerValues.get(HttpConstants.HttpHeaders.X_DATE);
        if (Strings.isNullOrEmpty(date)) {
            date = headerValues.get(HttpConstants.HttpHeaders.HTTP_DATE);
        }

        return date != null ? date : StringUtils.EMPTY;
    }
}
