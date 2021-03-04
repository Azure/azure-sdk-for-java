// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Util methods for batch requests/response.
 */
public final class BatchExecUtils {

    private final static Logger logger = LoggerFactory.getLogger(BatchExecUtils.class);

    public static Duration getRetryAfterDuration(Map<String, String> responseHeaders) {
        long retryIntervalInMilliseconds = 0;

        if (responseHeaders != null) {
            String header = responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);

            if (StringUtils.isNotEmpty(header)) {
                try {
                    retryIntervalInMilliseconds = Long.parseLong(header);
                } catch (NumberFormatException e) {
                    // If the value cannot be parsed as long, return 0.
                }
            }
        }

        return Duration.ofMillis(retryIntervalInMilliseconds);
    }

    public static String getSessionToken(Map<String, String> responseHeaders) {
        if (responseHeaders != null) {
            return responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        }

        return null;
    }

    public static String getActivityId(Map<String, String> responseHeaders) {
        if (responseHeaders != null) {
            return responseHeaders.get(HttpConstants.HttpHeaders.ACTIVITY_ID);
        }

        return null;
    }

    public static int getResponseLength(Map<String, String> responseHeaders) {
        int responseLength = 0;
        if (responseHeaders != null) {

            String contentLength = responseHeaders.get(HttpConstants.HttpHeaders.CONTENT_LENGTH);
            if(contentLength == null || StringUtils.isEmpty(contentLength))
            {
                return 0;
            }

            try {
                responseLength = Integer.parseInt(contentLength);
            } catch(NumberFormatException ex) {
                logger.warn("INVALID Content-Length value {}.", contentLength);
                return 0;
            }
        }

        return responseLength;
    }

    public static double getRequestCharge(Map<String, String> responseHeaders) {
        if (responseHeaders == null) {
            return 0;
        }

        final String value = responseHeaders.get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("INVALID x-ms-request-charge value {}.", value);
            return 0;
        }
    }

    public static int getSubStatusCode(Map<String, String> responseHeaders) {
        int code = HttpConstants.SubStatusCodes.UNKNOWN;

        if (responseHeaders != null) {
            String subStatusString = responseHeaders.get(HttpConstants.HttpHeaders.SUB_STATUS);
            if (StringUtils.isNotEmpty(subStatusString)) {
                try {
                    code = Integer.parseInt(subStatusString);
                } catch (NumberFormatException e) {
                    // If value cannot be parsed as Integer, return Unknown.
                }
            }
        }

        return code;
    }
}
