// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class to collect all throttling info from response header.
 * Some service has different rate limit but not visible in response header, like network/storage.
 */
public class ResourceManagerThrottlingInfo {
    // refer https://docs.microsoft.com/azure/azure-resource-manager/management/request-limits-and-throttling
    private static final List<HttpHeaderName> COMMON_RATE_LIMIT_HEADERS = Arrays.asList(
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-subscription-reads"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-subscription-writes"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-tenant-reads"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-tenant-writes"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-subscription-resource-requests"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-subscription-resource-entities-read"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-tenant-resource-requests"),
        HttpHeaderName.fromString("x-ms-ratelimit-remaining-tenant-resource-entities-read")
    );

    // refer https://docs.microsoft.com/azure/virtual-machines/troubleshooting/troubleshooting-throttling-errors
    private static final String RESOURCE_RATE_LIMIT_HEADER = "x-ms-ratelimit-remaining-resource";
    private static final HttpHeaderName RESOURCE_RATE_LIMIT_HEADER_NAME
        = HttpHeaderName.fromString(RESOURCE_RATE_LIMIT_HEADER);
    private static final Pattern RESOURCE_RATE_LIMIT_HEADER_PATTERN = Pattern.compile("\\w+\\.\\w+/([^;]+);(\\d+)");

    private final Map<String, String> commonRateLimits;
    private final String resourceRateLimit;

    /**
     * Creates the throttling info class from response headers
     * @param headers the response headers
     */
    public ResourceManagerThrottlingInfo(HttpHeaders headers) {
        commonRateLimits = new HashMap<>();
        for (HttpHeaderName header : COMMON_RATE_LIMIT_HEADERS) {
            String value = headers.getValue(header);
            if (!CoreUtils.isNullOrEmpty(value)) {
                commonRateLimits.put(header.getCaseInsensitiveName(), value);
            }
        }

        resourceRateLimit = headers.getValue(RESOURCE_RATE_LIMIT_HEADER_NAME);
        if (resourceRateLimit != null) {
            Matcher matcher = RESOURCE_RATE_LIMIT_HEADER_PATTERN.matcher(resourceRateLimit);
            while (matcher.find()) {
                commonRateLimits.put(RESOURCE_RATE_LIMIT_HEADER + "-" + matcher.group(1), matcher.group(2));
            }
        }
    }

    /**
     * Creates the throttling info class from response headers
     * @param headers the response headers
     * @return the ResourceManagerThrottlingInfo class
     */
    public static ResourceManagerThrottlingInfo fromHeaders(HttpHeaders headers) {
        return new ResourceManagerThrottlingInfo(headers);
    }

    /**
     * @return the smallest rate limit or empty if none of the headers are valid
     */
    public Optional<Integer> getRateLimit() {
        // TODO (alzimmermsft): Couldn't the be done once as 'commonRateLimits' is immutable?
        int result = Integer.MAX_VALUE;
        for (Map.Entry<String, String> limits : commonRateLimits.entrySet()) {
            try {
                int limit = Integer.parseInt(limits.getValue());
                if (result > limit) {
                    result = limit;
                }
            } catch (NumberFormatException ignored) { }
        }

        return result == Integer.MAX_VALUE ? Optional.empty() : Optional.of(result);
    }

    /**
     * refer https://docs.microsoft.com/azure/azure-resource-manager/management/request-limits-and-throttling
     * @return all headers associated with rate limit
     */
    public Map<String, String> getRateLimits() {
        return Collections.unmodifiableMap(commonRateLimits);
    }

    /**
     * refer https://docs.microsoft.com/azure/virtual-machines/troubleshooting/troubleshooting-throttling-errors
     * @return a specific rate limit header value from compute
     */
    public String getResourceRateLimit() {
        return resourceRateLimit;
    }
}
