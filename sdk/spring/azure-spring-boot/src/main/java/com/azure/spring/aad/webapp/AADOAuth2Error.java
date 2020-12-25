// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * Custom error with the error code returned by aad
 */
public class AADOAuth2Error extends OAuth2Error {
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final String errorCodes;

    private final String timestamp;

    private final String traceId;

    private final String correlationId;

    private final String subError;

    private final String claims;

    public AADOAuth2Error(String error, String errorDescription, String errorCodes, String timestamp,
                          String traceId, String correlationId, String errorUri, String subError, String claims) {
        super(error, errorDescription, errorUri);
        this.errorCodes = errorCodes;
        this.timestamp = timestamp;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.subError = subError;
        this.claims = claims;
    }


    public final String getErrorCodes() {
        return errorCodes;
    }

    public final String getTimestamp() {
        return timestamp;
    }

    public final String getTraceId() {
        return traceId;
    }

    public final String getCorrelationId() {
        return correlationId;
    }

    public final String getSubError() {
        return subError;
    }

    public final String getClaims() {
        return claims;
    }


    @Override
    public String toString() {

        return "AADAuthenticationException{"
            + ", error_codes='" + errorCodes + '\''
            + ", timestamp='" + timestamp + '\''
            + ", trace_id='" + traceId + '\''
            + ", correlation_id='" + correlationId + '\''
            + ", suberror='" + subError + '\''
            + ", claims='" + claims + '\''
            + '}';
    }
}
