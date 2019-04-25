// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import okhttp3.Response;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This is a helper class for data format manipulation.
 */
public final class DataTransformer {
    private static final String BODY_LOGGING = "x-ms-body-logging";

    private DataTransformer() {
        // Private constructor to avoid instantiation.
    }

    /**
     * Apply text replacement rule to text take in.
     *
     * @param text The text needs to apply replacement.
     * @param textReplacementRules DataTransformer which text needs to apply.
     * @return The text after replacement.
     */
    public static String applyReplacementRule(String text, Map<String, String> textReplacementRules) {
        for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    /**
     * Extract response data from response, and format to a map.
     * @param responseData The response data map which needs to fill in
     * @param response Raw response needs to extract.
     * @param textReplacementRules Text rules which response data needs to apply.
     * @throws IOException Throw IOException if buffer bytes checking failed to execute.
     */
    public static void extractResponseData(Map<String, String> responseData, Response response, Map<String, String> textReplacementRules) throws IOException {
        Map<String, List<String>> headers = response.headers().toMultimap();
        boolean addedRetryAfter = false;
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String headerValueToStore = header.getValue().get(0);

            if (header.getKey().equalsIgnoreCase("location") || header.getKey().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore, textReplacementRules);
            }
            if (header.getKey().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.getKey().toLowerCase(Locale.ROOT), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String bodyLoggingHeader = response.request().header(BODY_LOGGING);
        boolean bodyLogging = bodyLoggingHeader == null || Boolean.parseBoolean(bodyLoggingHeader);
        if (bodyLogging) {
            BufferedSource bufferedSource = response.body().source();
            bufferedSource.request(9223372036854775807L);
            Buffer buffer = bufferedSource.buffer().clone();
            String content = null;

            if (response.header("Content-Encoding") == null) {
                content = buffer.readString(Util.UTF_8);
            } else if (response.header("Content-Encoding").equalsIgnoreCase("gzip")) {
                GZIPInputStream gis = new GZIPInputStream(buffer.inputStream());
                content = IOUtils.toString(gis);
                responseData.remove("Content-Encoding".toLowerCase(Locale.ROOT));
                responseData.put("Content-Length".toLowerCase(Locale.ROOT), Integer.toString(content.length()));
            }

            if (content != null) {
                content = applyReplacementRule(content, textReplacementRules);
                responseData.put("Body", content);
            }
        }
    }

    /**
     * Remove host info from url.
     *
     * @param url The URL contains host info.
     * @return The URL without host info.
     */
    public static String removeHost(String url) {
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }

}
