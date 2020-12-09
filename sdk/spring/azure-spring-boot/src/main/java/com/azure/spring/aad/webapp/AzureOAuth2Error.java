package com.azure.spring.aad.webapp;

import org.springframework.security.oauth2.core.OAuth2Error;

public class AzureOAuth2Error extends OAuth2Error {

    private final String errorCodes;

    private final String timestamp;

    private final String traceId;

    private final String correlationId;

    private final String subError;

    private final String claims;

    public AzureOAuth2Error(String error, String error_description, String error_codes, String timestamp
        , String trace_id, String correlation_id, String error_uri, String sub_error, String claims) {
        super(error,error_description,error_uri);
        this.errorCodes = error_codes;
        this.timestamp = timestamp;
        this.traceId = trace_id;
        this.correlationId = correlation_id;
        this.subError = sub_error;
        this.claims = claims;
    }


    public final String getErroCrodes() {
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
        return "AADAuthenticationException{" +
            ", error_codes='" + errorCodes + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", trace_id='" + traceId + '\'' +
            ", correlation_id='" + correlationId + '\'' +
            ", suberror='" + subError + '\'' +
            ", claims='" + claims + '\'' +
            '}';
    }
}
