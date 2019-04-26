// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import com.azure.common.http.HttpHeader;
import com.azure.common.http.HttpResponse;
import com.azure.common.test.TestMode;
import com.azure.common.test.models.RecordedData;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static com.azure.common.test.utils.TestConstant.DEFAULT_BUFFER_LENGTH;

/**
 * The class to contain the common test methods for testing SDK.
 */
public final class SdkContext {

    /**
     * Gets a random name.
     *
     * @param namer the test resource random namer which is related to recorded data
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public static String randomTestResourceName(TestResourceNamer namer, String prefix, int maxLen) {
        return namer.randomName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random test resource names with the same prefix.
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return random names
     */
    public static String[] randomTestResourceNames(TestResourceNamer namer, String prefix, int maxLen, int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = namer.randomName(prefix, maxLen);
        }
        return names;
    }

    /**
     * Puts current thread on sleep for passed milliseconds.
     * @param milliseconds time to sleep for
     */
    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Extract information from a resource ID string with the resource type
     * as the identifier.
     *
     * @param id the resource ID
     * @param identifier the identifier to match, e.g. "resourceGroups", "storageAccounts"
     * @return the information extracted from the identifier
     */
    public static String extractFromResourceId(String id, String identifier) {
        if (id == null || identifier == null) {
            return id;
        }
        Pattern pattern = Pattern.compile(identifier + "/[-\\w._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("/")[1];
        } else {
            return null;
        }
    }


    /**
     * Replace the text to the one defined in textReplacementRules.
     *
     * @param text the text which will apply the text replacement rules
     * @param textReplacementRules the rule of text replacement
     * @return the updated text
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
     * Remove the host info from url
     *
     * @param url the url needs to remove host
     * @return the url without host
     */
    public static String removeHost(String url) {
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }

    /**
     * Async API for extract and rebuild response data.
     *
     * @param response the response to extract and rebuild
     * @param textReplacementRule rules of rebuilding the response data
     * @return
     */
    public static Mono<Map<String, String>> extractResponseData(final HttpResponse response, final Map<String, String> textReplacementRule) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put("StatusCode", Integer.toString(response.statusCode()));

        boolean addedRetryAfter = false;

        for (HttpHeader header : response.headers()) {
            String headerValueToStore = header.value();

            if (header.name().equalsIgnoreCase("location") || header.name().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore, textReplacementRule);
            }

            if (header.name().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.name().toLowerCase(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String contentType = response.headerValue("content-type");

        if (contentType == null) {
            return Mono.just(responseData);
        } else if (contentType.contains("json") || response.headerValue("content-encoding") == null) {
            return response.bodyAsString().map(content -> {
                content = applyReplacementRule(content, textReplacementRule);
                responseData.put("Body", content);
                return responseData;
            });
        } else {
            return response.bodyAsByteArray().map(bytes -> {
                String content;
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
                    int position = 0;
                    int bytesRead = gis.read(buffer, position, buffer.length);

                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead);
                        position += bytesRead;
                        bytesRead = gis.read(buffer, position, buffer.length);
                    }
                    content = new String(output.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
                responseData.remove("content-encoding");
                responseData.put("content-length", Integer.toString(content.length()));

                content = applyReplacementRule(content, textReplacementRule);
                responseData.put("body", content);
                return responseData;
            });

        }

    }
}
