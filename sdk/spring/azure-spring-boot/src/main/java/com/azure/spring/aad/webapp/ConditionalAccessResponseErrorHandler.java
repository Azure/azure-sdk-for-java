// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.nimbusds.oauth2.sdk.token.BearerTokenError;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Handle conditional access.
 */
public class ConditionalAccessResponseErrorHandler implements ResponseErrorHandler {

    private final OAuth2ErrorHttpMessageConverter oauth2ErrorConverter = new OAuth2ErrorHttpMessageConverter();

    private final ResponseErrorHandler defaultErrorHandler = new DefaultResponseErrorHandler();

    protected ConditionalAccessResponseErrorHandler() {
        this.oauth2ErrorConverter.setErrorConverter(new AADOAuth2ErrorConverter());
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return this.defaultErrorHandler.hasError(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        if (!HttpStatus.BAD_REQUEST.equals(response.getStatusCode())) {
            this.defaultErrorHandler.handleError(response);
        }

        // A Bearer Token Error may be in the WWW-Authenticate response header
        OAuth2Error oauth2Error = this.readErrorFromWwwAuthenticate(response.getHeaders());
        if (oauth2Error == null) {
            oauth2Error = this.oauth2ErrorConverter.read(OAuth2Error.class, response);
        }
        /**
         *  Handle conditional access policy, step 1.
         *  https://docs.microsoft.com/en-us/azure/active-directory/conditional-access/howto-conditional-access-policy-all-users-mfa
         */
        throw new OAuth2AuthorizationException(oauth2Error);
    }

    private OAuth2Error readErrorFromWwwAuthenticate(HttpHeaders headers) {
        String wwwAuthenticateHeader = headers.getFirst(HttpHeaders.WWW_AUTHENTICATE);
        if (!StringUtils.hasText(wwwAuthenticateHeader)) {
            return null;
        }

        BearerTokenError bearerTokenError;
        try {
            bearerTokenError = BearerTokenError.parse(wwwAuthenticateHeader);
        } catch (Exception ex) {
            return null;
        }

        String errorCode = bearerTokenError.getCode() != null
            ? bearerTokenError.getCode() : OAuth2ErrorCodes.SERVER_ERROR;
        String errorDescription = bearerTokenError.getDescription();

        String errorUri = bearerTokenError.getURI() != null
            ? bearerTokenError.getURI().toString() : null;

        return new OAuth2Error(errorCode, errorDescription, errorUri);
    }


    private static class AADOAuth2ErrorConverter implements Converter<Map<String, String>, OAuth2Error> {
        @Override
        public OAuth2Error convert(Map<String, String> parameters) {
            String errorCode = parameters.get("error");
            String description = parameters.get("error_description");
            String errorCodes = parameters.get("error_codes");
            String timestamp = parameters.get("timestamp");
            String traceId = parameters.get("trace_id");
            String correlationId = parameters.get("correlation_id");
            String uri = parameters.get("error_uri");
            String subError = parameters.get("suberror");
            String claims = parameters.get("claims");

            return new AADOAuth2Error(errorCode, description, errorCodes, timestamp, traceId, correlationId,
                uri, subError, claims);
        }
    }
}
