// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handle the {@link WebClientResponseException} in On-Behalf-Of flow.
 *
 * <p>
 * When the resource-server needs re-acquire token(The request requires higher privileges than provided by the access
 * token in On-Behalf-Of flow.), it can sent a 403 with information in the WWW-Authenticate header to web client ,web
 * client will throw {@link WebClientResponseException}, web-application can handle this exception to challenge the
 * user.
 */
public class AADHandleConditionalAccessFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADHandleConditionalAccessFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            Map<String, String> authParameters =
                Optional.of(exception)
                        .map(Throwable::getCause)
                        .filter(e -> e instanceof WebClientResponseException)
                        .map(e -> (WebClientResponseException) e)
                        .map(WebClientResponseException::getHeaders)
                        .map(httpHeaders -> httpHeaders.get(HttpHeaders.WWW_AUTHENTICATE))
                        .map(list -> list.get(0))
                        .map(this::parseAuthParameters)
                        .orElse(null);
            if (authParameters != null && authParameters.containsKey(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS)) {
                request.getSession().setAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS,
                    authParameters.get(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS));
                response.setStatus(302);
                try {
                    response.sendRedirect(Constants.DEFAULT_AUTHORITY_ENDPOINT_URI);
                } catch (IOException e) {
                    LOGGER.error("Failed to redirect at this response.", exception);
                }
                return;
            }
            throw exception;
        }
    }

    private Map<String, String> parseAuthParameters(String wwwAuthenticateHeader) {
        return Stream.of(wwwAuthenticateHeader)
                     .filter(header -> !StringUtils.isEmpty(header))
                     .filter(header -> header.startsWith(Constants.BEARER_PREFIX))
                     .map(str -> str.substring(Constants.BEARER_PREFIX.length() + 1, str.length() - 1))
                     .map(str -> str.split(", "))
                     .flatMap(Stream::of)
                     .map(parameter -> parameter.split("="))
                     .filter(parameter -> parameter.length > 1)
                     .collect(Collectors.toMap(
                         parameters -> parameters[0],
                         parameters -> parameters[1]));
    }
}
