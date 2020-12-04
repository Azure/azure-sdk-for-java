package com.azure.spring.aad.webapp;

import org.springframework.security.oauth2.core.OAuth2Error;

public class AzureOAuthError extends OAuth2Error {

    private final String error_codes;

    private final String timestamp;

    private final String trace_id;

    private final String correlation_id;

    private final String sub_error;

    private final String claims;

    public AzureOAuthError(String error, String error_description, String error_codes, String timestamp
        , String trace_id, String correlation_id, String error_uri, String sub_error, String claims) {
        super(error,error_description,error_uri);
        this.error_codes = error_codes;
        this.timestamp = timestamp;
        this.trace_id = trace_id;
        this.correlation_id = correlation_id;
        this.sub_error = sub_error;
        this.claims = claims;
    }


    public final String getError_codes() {
        return error_codes;
    }

    public final String getTimestamp() {
        return timestamp;
    }

    public final String getTrace_id() {
        return trace_id;
    }

    public final String getCorrelation_id() {
        return correlation_id;
    }

    public final String getSub_error() {
        return sub_error;
    }

    public final String getClaims() {
        return claims;
    }


    @Override
    public String toString() {
        return "AADAuthenticationException{" +
            ", error_codes='" + error_codes + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", trace_id='" + trace_id + '\'' +
            ", correlation_id='" + correlation_id + '\'' +
            ", suberror='" + sub_error + '\'' +
            ", claims='" + claims + '\'' +
            '}';
    }
}
