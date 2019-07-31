// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.Constants.UrlEncodingInfo;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    public static URI toURI(String uri) {
        try {
            return new URI(uri);
        } catch (Exception e) {
            log.error("failed to parse {}", uri, e);
            throw new IllegalArgumentException("failed to parse uri " + uri, e);
        }
    }

    public static Map<String, String> asMap(HttpHeaders headers) {
        if (headers == null) {
            return new HashMap<>();
        }
        HashMap<String, String> map = new HashMap<>(headers.size());
        for (Entry<String, String> entry : headers.toMap().entrySet()) {
            if (entry.getKey().equals(HttpConstants.HttpHeaders.OWNER_FULL_NAME)) {
                map.put(entry.getKey(), HttpUtils.urlDecode(entry.getValue()));
            } else {
                map.put(entry.getKey(), entry.getValue());
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

    public static List<Entry<String, String>> unescape(Set<Entry<String, String>> headers) {
        List<Entry<String, String>> result = new ArrayList<>(headers.size());
        for (Entry<String, String> entry : headers) {
            if (entry.getKey().equals(HttpConstants.HttpHeaders.OWNER_FULL_NAME)) {
                String unescapedUrl = HttpUtils.urlDecode(entry.getValue());
                entry = new AbstractMap.SimpleEntry<>(entry.getKey(), unescapedUrl);
            }
            result.add(entry);
        }
        return result;
    }
}
