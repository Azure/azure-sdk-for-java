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

public class AzureOAuthResponseErrorHandler implements ResponseErrorHandler {

    private final OAuth2ErrorHttpMessageConverter oauth2ErrorConverter = new OAuth2ErrorHttpMessageConverter();

    private Converter<Map<String, String>, OAuth2Error> errorConverter = new AADOAuth2ErrorConverter();

    private final ResponseErrorHandler defaultErrorHandler = new DefaultResponseErrorHandler();

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
            this.oauth2ErrorConverter.setErrorConverter(errorConverter);
            oauth2Error = this.oauth2ErrorConverter.read(OAuth2Error.class, response);
        }
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

        String errorCode = bearerTokenError.getCode() != null ?
            bearerTokenError.getCode() : OAuth2ErrorCodes.SERVER_ERROR;
        String errorDescription = bearerTokenError.getDescription();
        String errorUri = bearerTokenError.getURI() != null ?
            bearerTokenError.getURI().toString() : null;

        return new OAuth2Error(errorCode, errorDescription, errorUri);
    }


    private static class AADOAuth2ErrorConverter implements Converter<Map<String, String>, OAuth2Error> {
        private final String ERROR = "error";
        private final String ERROR_DESCRIPTION = "error_description";
        private final String ERROR_CODES = "error_codes";
        private final String TIMESTAMP = "timestamp";
        private final String TRACE_ID = "trace_id";
        private final String CORRELATION_ID = "correlation_id";
        private final String ERROR_URI = "error_uri";
        private final String SUB_ERROR = "suberror";
        private final String CLAIMS = "claims";

        @Override
        public OAuth2Error convert(Map<String, String> parameters) {
            String errorCode = parameters.get(ERROR);
            String description =  parameters.get(ERROR_DESCRIPTION);
            String error_codes = parameters.get(ERROR_CODES);
            String timestamp = parameters.get(TIMESTAMP);
            String trace_id = parameters.get(TRACE_ID);
            String correlation_id = parameters.get(CORRELATION_ID);
            String uri = parameters.get(ERROR_URI);
            String sub_error = parameters.get(SUB_ERROR);
            String claims = parameters.get(CLAIMS);

            return new AzureOAuthError(errorCode,description,error_codes,timestamp,trace_id,correlation_id,uri,sub_error,claims);
        }
    }
}
