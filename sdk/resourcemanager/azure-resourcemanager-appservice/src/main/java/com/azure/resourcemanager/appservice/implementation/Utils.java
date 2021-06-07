// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/** Utilities for AppService implementation. */
class Utils {

    /**
     * Encodes byte array to Base16 string.
     *
     * @param bytes byte array to be encoded.
     * @return Base16 string
     */
    static String base16Encode(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    /**
     * Completes docker image and tag with registry server.
     *
     * @param imageAndTag docker image and tag
     * @param serverUrl private registry server URL
     * @return docker image and tag completed with registry server
     */
    static String smartCompletionPrivateRegistryImage(String imageAndTag, String serverUrl) {
        try {
            URL url = new URL(serverUrl);
            String registryServer = url.getAuthority();
            String path = url.getPath();
            if (!registryServer.isEmpty() && !imageAndTag.trim().startsWith(registryServer)) {
                String[] segments = imageAndTag.split(Pattern.quote("/"));
                if (segments.length == 1) {
                    // it appears that imageAndTag does not contain registry server, add registry server before it.
                    imageAndTag = completePrivateRegistryImage(imageAndTag, registryServer, path);
                }
                if (segments.length > 1) {
                    String segment = segments[0];
                    if (!segment.isEmpty()
                        && !segment.contains(".")
                        && !segment.contains(":")
                        && !segment.equals(registryServer)) {
                        // it appears that first segment of imageAndTag is not registry server, add registry server
                        // before it.
                        imageAndTag = completePrivateRegistryImage(imageAndTag, registryServer, path);
                    }
                }
            }
        } catch (MalformedURLException e) {
            // serverUrl is probably incorrect, abort
        }
        return imageAndTag;
    }

    private static String completePrivateRegistryImage(String imageAndTag, String registryServer, String path) {
        path = removeLeadingChar(removeTrailingChar(path, '/'), '/');
        if (path.isEmpty()) {
            imageAndTag = String.format("%s/%s", registryServer, imageAndTag.trim());
        } else {
            imageAndTag = String.format("%s/%s/%s", registryServer, path, imageAndTag.trim());
        }
        return imageAndTag;
    }

    private static String removeLeadingChar(String s, char c) {
        int index;
        for (index = 0; index < s.length(); index++) {
            if (s.charAt(index) != c) {
                break;
            }
        }
        return s.substring(index);
    }

    private static String removeTrailingChar(String s, char c) {
        int index;
        for (index = s.length() - 1; index >= 0; index--) {
            if (s.charAt(index) != c) {
                break;
            }
        }
        return s.substring(0, index + 1);
    }

    /**
     * Download a file asynchronously.
     *
     * @param url the URL pointing to the file
     * @param httpPipeline the http pipeline
     * @return an Observable pointing to the content of the file
     */
    static Mono<byte[]> downloadFileAsync(String url, HttpPipeline httpPipeline) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        return httpPipeline.send(request)
            .flatMap(response1 -> {
                int code = response1.getStatusCode();
                if (code == 200) {
                    return Mono.just(response1);
                } else {
                    return Mono.error(new HttpResponseException(response1));
                }
            }).flatMap(HttpResponse::getBodyAsByteArray);
    }
}
